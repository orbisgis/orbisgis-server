/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import org.orbisgis.server.wms.WMSResponse
import java.io.File
import scala.collection.JavaConversions._
import java.io.ByteArrayOutputStream
import scala.collection.mutable.{Map => MutableMap}
import org.orbisgis.server.wms.{WMS => JavaWMS}
import org.orbisgis.core.renderer.se.Style
import org.apache.commons.io.{FileUtils => FU}

object WMS extends Controller {
  
  // local style directory
  val styleDir = new File("styles")

  // WMS entry point
  val wmsCt = new JavaWMS
  
  // current styles
  val styles: MutableMap[String, Style] = MutableMap.empty
  
  // styles per sources
  val sourceStyles: MutableMap[String, Array[String]] = MutableMap.empty
  
  // gets the style map from the local fileSystem
  def loadStyles {
    if (!styleDir.exists) {
      styleDir.mkdirs
    }

    styles.clear
    styles ++= (styleDir.listFiles.toSeq.filter(_.getName.endsWith(".se")).map { f =>
      (f.getName.substring(0, f.getName.length - 3),
        new Style(null, f.getAbsolutePath))
    })
  }
  
  val styleForm = Form(
    "style" -> nonEmptyText)
  
  /**
  * Add style action.
  */
  def addStyle = Action { implicit request ⇒
    styleForm.bindFromRequest.fold(h ⇒
      Redirect(routes.Application.index),
      style ⇒ {
        val source = new File(style)
        if (source.exists() && source.getName.endsWith(".se")) {
          val target = new File(styleDir, source.getName())
          FU.copyFile(source, target)
	  styles.put(target.getName.substring(0, target.getName.length - 3),
            new Style(null, target.getAbsolutePath))
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
      styles.remove(name)
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
      styles.put(name, styles.remove(oldname).get)
      Redirect(routes.Application.index)
    })
  }
  
  /**
  * Clear all styles action.
  */
  def clearStyles = Action {
    styleDir.listFiles() foreach (f ⇒ if (f.isFile()) f.delete())
    styles.clear
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
      def getRequestUrl = "http://" + request.host + controllers.routes.WMS.wms
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
}
