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

import scala.xml._
import java.util.Date

/**
 * MapContext information
 * @param id Identifier of this map context, unique for a workspace
 */
class MapContext (var id: Int) {
  var titleStr : String = "none"
  var titleLng : String = ""
  var abstractStr : String = ""
  var abstractLng : String = ""
  var mapDate : String = ""     // Date extracted from the original document

  /**
   * Extract the description from the XML parameter
   */
  def fromXML(context: Node) {
    id = (context \ "@id").text.toInt
    mapDate = (context \ "@date").text
    titleStr = (context \ "title").text
    titleLng = ((context \ "title") \ "@xml:lang").text
    abstractStr = (context \ "abstract").text
    abstractLng = ((context \ "abstract") \ "@xml:lang").text
  }
}