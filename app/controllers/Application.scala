package controllers

import play.api._
import play.api.mvc._
import java.io.File
import org.orbisgis.core._
import play.api.data._
import play.api.data.Forms._
import org.orbisgis.utils.FileUtils

object Application extends Controller {

  // current SourceManager
  lazy val sm = Services.getService(classOf[DataManager]).getSourceManager()

  /**
  * Manage page action.
  */
  def index = Action {
    val ss = sm.getSourceNames().toSeq

    val layers = ss.map(s ⇒ sm.getSource(s)).collect{ case s if !s.isSystemTableSource && s.isFileSource() ⇒ (s.getName(), s.getFile()) }
    if (!WMS.styleDir.exists()) WMS.styleDir.mkdirs()
    val styles = WMS.styleDir.listFiles().toSeq

    Ok(views.html.index(styles, layers, fileForm, WMS.styleForm))
  }

  val fileForm = Form(
    "file" -> nonEmptyText)

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
	    sm.saveStatus
          }
          Redirect(routes.Application.index)
        } catch {
          case e: Exception ⇒ BadRequest(e.getMessage())
        })
  }

  /**
  * Remove file action.
  */
  def removeFile(name: String) = Action { request ⇒
    sm.remove(name)
    sm.saveStatus
    Redirect(routes.Application.index)
  }

  /**
  * Clear all files action.
  */
  def clearFiles = Action {
    sm.removeAll()
    sm.saveStatus
    Redirect(routes.Application.index)
  }
}
