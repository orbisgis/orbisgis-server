package controllers

import play.api._
import play.api.mvc._
import scala.collection.JavaConversions._
import org.orbisgis.server.wms.WMS
import java.io.File
import java.io.ByteArrayOutputStream
import org.orbisgis.server.wms.WMSResponse
import play.api.libs.iteratee.Enumerator
import org.orbisgis.core._
import play.api.data._
import play.api.data.Forms._
import org.orbisgis.utils.FileUtils
import org.orbisgis.core.renderer.se.Style
import play.api.libs.concurrent.Akka
import play.api.Play.current

object Application extends Controller {

  // local style directory
  val styleDir = new File("../styles")
  // WMS entry point
  val wmsCt = new WMS
  // current SourceManager
  lazy val sm = Services.getService(classOf[DataManager]).getSourceManager()

  // gets the style map from the local fileSystem
  def loadStyles = {
    Map(styleDir.listFiles.toSeq.filter(_.getName.endsWith(".se")).map { f =>
      (f.getName.substring(0, f.getName.length - 3),
        new Style(null, new File(styleDir, f.getName).getAbsolutePath))
    }:_*)
  }

  /**
  * Manage page action.
  */
  def index = Action {
    val sm = Services.getService(classOf[DataManager]).getSourceManager()
    val ss = sm.getSourceNames().toSeq

    val layers = ss.map(s ⇒ sm.getSource(s)).collect{ case s if !s.isSystemTableSource && s.isFileSource() ⇒ (s.getName(), s.getFile()) }
    if (!styleDir.exists()) styleDir.mkdirs()
    val styles = styleDir.listFiles().toSeq

    Ok(views.html.index(styles, layers, fileForm, styleForm))
  }

  val fileForm = Form(
    "file" -> nonEmptyText)

  val styleForm = Form(
    "style" -> nonEmptyText)

  /**
  * Add file action.
  */
  def addFile = Action { implicit request ⇒
    fileForm.bindFromRequest.fold(h ⇒
      Redirect(routes.Application.index),
      inFile ⇒
        try {
          val l = new File(inFile)
          if (l.exists()) {
            val name = FileUtils.getFileNameWithoutExtensionU(l)
            sm.register(name, l)
          }
          Redirect(routes.Application.index)
        } catch {
          case e: Exception ⇒ BadRequest(e.getMessage())
        })
  }

  /**
  * Add style action.
  */
  def addStyle = Action { implicit request ⇒
    styleForm.bindFromRequest.fold(h ⇒
      Redirect(routes.Application.index),
      style ⇒ {
        val source = new File(style)
        if (source.exists()) {
          val target = new File(styleDir, source.getName())
          FileUtils.copy(source, target)
        }
        Redirect(routes.Application.index)
      })
  }

  /**
  * Remove style action.
  */
  def removeStyle(name: String) = Action { request ⇒
    val source = new File(styleDir, name)
    if (source.exists()) {
      source.delete()
    }

    Redirect(routes.Application.index)
  }

  val renameForm = Form("newname" -> nonEmptyText)

  /**
  * Rename style action.
  */
  def renameStyle(oldname: String) = Action { implicit request ⇒
    renameForm.bindFromRequest.fold(h ⇒ BadRequest, name ⇒ {
      val old = new File(styleDir, oldname)
      old.renameTo(new File(styleDir, name))
      Redirect(routes.Application.index)
    })
  }

  /**
  * Remove file action.
  */
  def removeFile(name: String) = Action { request ⇒
    sm.remove(name)

    Redirect(routes.Application.index)
  }

  /**
  * WMS entry point action.
  */
  def wms = Action { request ⇒
    var ct: String = "" // contentType
    var code: Int = 200 // responseCode

    class res extends WMSResponse {
      def setContentType(t: String) = ct = t
      // maybe there is a cleaner way to get this URL
      def getRequestUrl = "http://" + request.host + controllers.routes.Application.wms
      def setResponseCode(i: Int) = code = i
    }

    def getImage: Array[Byte] = {
      // ByteArray for now.
      val out = new ByteArrayOutputStream
      wmsCt.processRequests(request.queryString.map(a => (a._1, a._2.toArray)), out, new res)
      val b = out.toByteArray()
      out.close()
      b
    }

    // get the image
    // this should get refactored into a proper actor
    val p = Akka.future { getImage }

    Async {
      p.map { o ⇒
        SimpleResult(
          header = ResponseHeader(code, Map(CONTENT_LENGTH -> o.length.toString())),
          body = Enumerator(o))
      }
    }
  }

  /**
  * Clear all files action.
  */
  def clearFiles = Action {
    sm.removeAll()

    Redirect(routes.Application.index)
  }

  /**
  * Clear all styles action.
  */
  def clearStyles = Action {
    styleDir.listFiles() foreach (f ⇒ if (f.isFile()) f.delete())

    Redirect(routes.Application.index)
  }
}
