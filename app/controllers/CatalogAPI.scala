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

import play.api.mvc._
import mapcatalog.MapCatalog
import org.apache.log4j.Logger
import scala.xml.XML

/**
 * Host collections of ows map context
 * @author Nicolas Fortin
 */
object CatalogAPI extends Controller {
  val mapCatalog = new MapCatalog()
  private val LOGGER  = Logger.getLogger(CatalogAPI.getClass)

  def addContext(key: String , name : String, path : String)
  = Action(parse.temporaryFile) { implicit request =>
    Created(mapCatalog.addContext(name,request.body))
  }

  def getContext(key: String ,name : String ,id : String) = Action {
    Ok(scala.xml.XML.loadFile(mapCatalog.getContext(name,id.toInt)))
  }
  /**
   * @param key Client identifier
   * @return The list of workspaces
   */
  def listWorkspace(key : String) = Action {
    Ok(content = mapCatalog.getWorkspaceList)
  }
  def listContexts(key: String , name : String) = Action {
    Ok(content = mapCatalog.getContextList(name))
  }
  /**
   * Save the state of the loaded map catalog
   */
  def onStop() {
    mapCatalog.saveState()
  }
}