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
import java.text.SimpleDateFormat
import XmlTools.getAttributes

/**
 * MapContext information
 * @param id Identifier of this map context, unique for a workspace
 */
class MapContext (var id: Int) {
  var titleStr = "none"
  var titleLng = ""
  var abstractStr = ""
  var abstractLng = ""
  var mapDate = ""     // When the original document has been uploaded

  private def readTitleAbstractXML(context : Node) {
    if(!(context \ "Title" isEmpty)) {
      titleStr = (context \ "Title").text.trim
      titleLng = getAttributes((context \ "Title").head)("lang")
    }
    if(!(context \ "Abstract" isEmpty)) {
      abstractStr = (context \ "Abstract").text.trim
      abstractLng = getAttributes((context \ "Abstract").head)("lang")
    }
  }

  def toXML =
  <context id={id.toString} date={mapDate}>
    <Title xml:lang={titleLng}>{titleStr}</Title>
    { if (!abstractStr.isEmpty) { <Abstract xml:lang={abstractLng}>{abstractStr}</Abstract> } }
  </context>
  /**
   * Extract the description from the XML parameter
   */
  def fromXML(context: Node) {
    id = (context \ "@id").text.toInt
    mapDate = (context \ "@date").text
    readTitleAbstractXML(context)
  }

  /**
   * Extract from the full Ows Map Context file the General Description
   * @param context
   */
  def fromFullContextXML(context: Node) {
    mapDate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date())
    if(!(context \ "General" isEmpty)) {
      readTitleAbstractXML((context \ "General").head)
    }
  }
}