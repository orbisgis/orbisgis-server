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


import java.io.File
import org.apache.log4j.Logger
import scala.xml.XML
import scala.collection.mutable.HashMap

/**
 * Host collections of ows map context
 * @author Nicolas Fortin
 */
class MapCatalog {
  private val LOGGER  = Logger.getLogger("mapcatalog.MapCatalog")
  private val catalogFolder  = new File("map-catalogs/")
  private val workspaceFile = new File(catalogFolder,"mapcatalog.xml")
  //Todo use data base instead of storing all context description in memory
  private var workspaces = HashMap[String,Workspace]()
  private var lastContextId = 0
  init()
  /**
   * Create folders if not exists and load configuration file
   */
  private def init() {
    catalogFolder.mkdirs    
    if(workspaceFile.exists) {
      LOGGER.info("Loading map catalog configuration file..")
      fromXML(XML.loadFile(workspaceFile))
    } else {
      // First run
      // Create the default workspace
      addWorkspace(new Workspace("default"))
      saveState()
    }
  }

  /**
    * @param workspaceName Name of the workspace
    * @throws IllegalArgumentException workspace does not exist
    * @return
    */
  def getContextList(workspaceName: String) : play.api.templates.Xml = {
    if (!workspaces.keySet.contains(workspaceName)) {
      //Workspace name does not exists
      throw new IllegalArgumentException("<error>The workspace "+workspaceName+" does not exists</error>")
    }
    workspaces(workspaceName).getContextList
  }

  /**
   * Add a workspace in the workspace list.
   * @param newWorkspace
   */
  private def addWorkspace(newWorkspace : Workspace) {
    workspaces+=(newWorkspace.name -> newWorkspace)
  }
  /**
   *
   * @param workspaceName The name of the workspace
   * @param node The content of the workspace
   * @throws IllegalArgumentException workspace does not exist
   * @return The short description of the MapContext in XML form
   */
  def addContext(workspaceName: String, node: scala.xml.Node ) : play.api.templates.Xml = {
    // Three steps,
    // first extract Title, Description, compute Time and unique ID
    // Then save the entire context in a file
    // Return the short description of the context
    if (!workspaces.keySet.contains(workspaceName)) {
      //Workspace name does not exists
      throw new IllegalArgumentException("<error>The workspace "+workspaceName+" does not exists</error>")
    }
    // Save the context
    scala.xml.XML.save(new File(catalogFolder, lastContextId + ".xml").getAbsolutePath,node)

    // Create the context object
    val newContext = new MapContext(lastContextId)
    newContext.fromFullContextXML(node)
    workspaces.get(workspaceName).get.addContext(newContext)
    //Increment last context id
    lastContextId+=1
    // Return the new content information
    mapcatalog.xml.getShortContext(newContext)
  }

  /**
   *
   * @return XML content of the map catalog
   */
  def getWorkspaceList = mapcatalog.xml.listWorkspaces(workspaces.values)
  
  /**
   * Extract the description from the XML parameter
   */
  def fromXML(node: scala.xml.Node) {
    workspaces = HashMap[String,Workspace]() //Clear the workspace
    lastContextId=(-1)
    // Iterate over workspace nodes and fetch for the name
    node\"workspace" foreach{(workspace)=>
      val newWorkspace = new Workspace("none")
      newWorkspace.fromXML(workspace)
      workspaces += (newWorkspace.name -> newWorkspace)
    }
    // Find the next unused context id
    if (!workspaces.isEmpty) {
      lastContextId = workspaces.values.map(workspace => workspace.getMaxId).max
      lastContextId += 1
    } else {
      lastContextId = 0
    }
    LOGGER.info("there are "+workspaces.size+" loaded workspaces")
  }
  
  def toXML =
    <workspaces>
      {workspaces.values.map { workspace => workspace.toXML}}
    </workspaces>
    
  /**
   * Save the state of the loaded map catalog
   */
  def saveState() {
    LOGGER.info("Save map catalog to "+workspaceFile.getAbsolutePath)
    scala.xml.XML.save(workspaceFile getAbsolutePath, toXML)
  }
  
}