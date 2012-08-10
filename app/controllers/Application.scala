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
