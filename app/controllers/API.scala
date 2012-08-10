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
import play.api.libs.json._
import scala.util.control.Exception._
import java.net._
import java.io.File
import org.orbisgis.utils.FileUtils
import org.gdms.source.Source

object API extends Controller {

  private def buildInfo(s: Source) = {
    Map("name" -> Json.toJson(s.getName), "uri" -> Json.toJson(s.getURI.toString),
      "type" -> Json.toJson(s.getTypeName))
  }

  def listSources = Action {

    val maps = Application.sm.getSourceNames.view.map(Application.sm.getSource).
      filter(!_.isSystemTableSource).map(buildInfo)

    val json = Json.toJson(maps.map(Json.toJson(_)))

    Ok(json)
  }

  def registerSource = Action(parse.json) { request ⇒
    val name = (request.body \ "name").asOpt[String]
    val uri = (request.body \ "uri").asOpt[String].flatMap(a ⇒ catching(classOf[URISyntaxException]) opt new URI(a))

    uri.map { u ⇒
      (name.getOrElse(FileUtils.getFileNameWithoutExtensionU(new File(u.getPath))), u)
    }.map {
      case (n, u) ⇒
        if (Application.sm.exists(n)) {
          BadRequest("There already is a source with name " + n + ".")
        } else {
          Application.sm.register(n, u)
          Ok(Json.toJson(buildInfo(Application.sm.getSource(n))))
        }
    }.getOrElse {
      BadRequest("Malformed input json.")
    }
  }
  
  def deleteSource(name: String) = Action {
    if (Application.sm.exists(name)) {
      Application.sm.remove(name)
      NoContent
    } else {
      BadRequest("There is no source with name " + name + ".")
    }
  }
}
