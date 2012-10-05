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

package mapcatalog


import java.io.{File, FileOutputStream}
import org.apache.log4j.Logger;

/**
 * Host collections of ows map context
 * @author Nicolas Fortin
 */
class MapCatalog {
  private val LOGGER  = Logger.getLogger("mapcatalog.MapCatalog");
  private val catalogFolder  = new File("map-catalogs/")
  private val workspaceFile = new File(catalogFolder,"mapcatalog.xml")
  private var workspaces = Map[String,Workspace]()
  init()
  
  /**
   * Create folders if not exists and load configuration file
   */
  private def init() {
    catalogFolder.mkdirs    
    if(workspaceFile.exists) {
      fromXML(xml.XML.loadFile(workspaceFile))
    } else {
      saveState
    }
  }
  def getWorkspaceList = 
    <workspaces>
      {workspaces.foreach{case (name,workspace) => <workspace>{name}</workspace>}}
    </workspaces>
  
  /**
   * Extract the description from the XML parameter
   */
  def fromXML(node: scala.xml.Node) {
    workspaces = Map[String,Workspace]() //Clear the workspace
    // Iterate over workspace nodes and fetch for the name
    node\"workspace" foreach{(workspace)=>
      val newWorkspace = new Workspace("none")
      newWorkspace.fromXML(workspace)
      workspaces += (newWorkspace.name -> newWorkspace)
    }
  }
  
  def toXML =
    <workspaces>
      {workspaces.foreach{case (name,workspace) => workspace.toXML}}
    </workspaces>
    
  /**
   * Save the state of the loaded map catalog
   */
  def saveState() {
    LOGGER.info("Save map catalog to "+workspaceFile.getAbsolutePath)
    scala.xml.XML.save(workspaceFile getAbsolutePath, toXML)
  }
  
}