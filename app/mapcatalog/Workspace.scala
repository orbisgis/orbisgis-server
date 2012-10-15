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

import collection.mutable

/**
 * Workspace contain a collection of Maps
 */
class Workspace (var name: String) {
  var contexts = mutable.MutableList[MapContext]()
  /**
   * Extract the description and map context from the XML parameter
   */
  def fromXML(workspace: scala.xml.Node) {
    name = (workspace \ "@name").text.trim
    // Load maps
    workspace \ "context" foreach{ context =>
      val newContext = new MapContext(-1)
      newContext.fromXML(context)
      contexts += newContext
    }
  }

  /**
   * @return The maximum map context id
   */
  def getMaxId: Int = {
    if(!contexts.isEmpty) {
      contexts.map( context => context.id).max
    } else {
      -1
    }
  }

  /**
   * Add a new Map Context
   * @param context MapContext instance
   */
  def addContext(context : MapContext) {
    contexts+=context
  }

  /**
   *
   * @param id Context id
   */
  def removeContext(id : Int) {
    contexts = contexts.filterNot(context => context.id==id)
  }
  /**
   *
   * @return Content of this workspace
   */
  def getContextList = mapcatalog.xml.getContextList(contexts)
  /**
   * Return the description of this map context in XML
   */  
  def toXML = 
    <workspace name={name} count={contexts.size.toString}>
      {contexts.map{context => context.toXML}}
    </workspace>
}
