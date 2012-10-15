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


import java.io.{FileReader, File}
import org.apache.log4j.Logger
import scala.xml.{Elem, XML}
import scala.collection.mutable.HashMap
import org.apache.commons.io.{FileUtils => FU}

/**
 * Host collections of ows map context
 * @author Nicolas Fortin
 */
class MapCatalog {
  private val LOGGER  = Logger.getLogger("mapcatalog.MapCatalog")
  private val catalogFolder  = new File("map-catalogs/")
  private val workspaceFile = new File(catalogFolder,"mapcatalog.xml")
  //Todo use data base instead of storing all context description in memory
  private val workspaces = HashMap[String,Workspace]()
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
   * Return the full content of a map context
   * @param workspaceName Name of the workspace
   * @param mapId Identifier of the map
   * @return
   */
  def getContext(workspaceName: String, mapId: Int): File = {
    if (!workspaces.keySet.contains(workspaceName)) {
      //Workspace name does not exists
      throw new IllegalArgumentException("<error>The workspace "+workspaceName+" does not exists</error>")
    }
    new File(catalogFolder,mapId+".xml")
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

  private def getFilePathForContext(workspaceName: String,id : Int) : File =  {
    new File(catalogFolder, id + ".xml")
  }


  /**
   *
   */
  private def processContext(workspaceName: String, tempFile: play.api.libs.Files.TemporaryFile, id : Int ) : MapContext = {
    // Three steps,
    // first extract Title, Description, compute Time and unique ID
    // Then save the entire context in a file
    // Return the short description of the context
    if (!workspaces.keySet.contains(workspaceName)) {
      //Workspace name does not exists
      throw new IllegalArgumentException("<error>The workspace "+workspaceName+" does not exists</error>")
    }
    if(!tempFile.file.exists()) {
      throw new IllegalArgumentException("<error>The provided file name does not exists "+tempFile.file+"</error>")
    }
    // Save the context
    val contextDestFile = getFilePathForContext(workspaceName,id)
    FU.copyFile(tempFile.file,contextDestFile)

    // Parse the content
    val doc = XML.loadFile(tempFile.file)
    // Create the context object
    val newContext = new MapContext(lastContextId)
    newContext.fromFullContextXML(doc)
    workspaces.get(workspaceName).get.addContext(newContext)
    newContext
  }

  /**
   *
   * @param workspaceName The name of the workspace
   * @param tempFile The content of the context
   * @param id map context id
   * @throws IllegalArgumentException workspace does not exist
   * @return The short description of the MapContext in XML form
   */
  def replaceContext(workspaceName: String, tempFile: play.api.libs.Files.TemporaryFile, id : Int ) : play.api.templates.Xml = {
    if (!getFilePathForContext(workspaceName,id).exists()) {
      //Workspace name does not exists
      throw new IllegalArgumentException("<error>The specified context does not exists</error>")
    }
    val newContext = processContext(workspaceName,tempFile,id)
    // Return the new content information
    mapcatalog.xml.getShortContext(newContext)
  }

  /**
   * Delete the specified context
   * @param workspaceName The name of the workspace
   * @param id map context id
   * @throws IllegalArgumentException workspace does not exist
   * @return The short description of the MapContext in XML form
   */
  def removeContext(workspaceName: String, id : Int ) = {
    if (!getFilePathForContext(workspaceName,id).exists()) {
      //Workspace name does not exists
      throw new IllegalArgumentException("<error>The specified context does not exists</error>")
    }
    val contextFile = getFilePathForContext(workspaceName,id)
    if (!contextFile.exists()) {
      //Workspace name does not exists
      throw new IllegalArgumentException("<error>The specified context does not exists</error>")
    }
    workspaces.get(workspaceName).get.removeContext(id)
    contextFile.delete()
  }
  /**
   *
   * @param workspaceName The name of the workspace
   * @param tempFile The content of the context
   * @throws IllegalArgumentException workspace does not exist
   * @return The short description of the MapContext in XML form
   */
  def addContext(workspaceName: String, tempFile: play.api.libs.Files.TemporaryFile ) : play.api.templates.Xml = {
    val newContext = processContext(workspaceName,tempFile,lastContextId)
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
    workspaces.clear() //Clear the workspace
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
    scala.xml.XML.save(workspaceFile getAbsolutePath, toXML, enc = "UTF-8", xmlDecl = true )
  }
  
}