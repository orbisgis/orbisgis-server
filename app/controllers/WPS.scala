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
import wps.{WPS => WPSMain, WPSProcess}

object WPS extends Controller {

  var wpsMain: WPSMain = _

  def init() {
    wpsMain = new WPSMain
  }
  
  /**
  * Main end point
  */
  def wpsGet = Action { implicit request =>
    if (request.queryString.get("service").map(s => !"wps".equalsIgnoreCase(s.head)).getOrElse(true)) {
      BadRequest("Wrong service. Excepted 'wps'")
    } else if (request.queryString.get("AcceptVersions").map(_!="1.0.0").getOrElse(false)) {
      BadRequest("Only accepted version is '1.0.0'.")
    } else if (request.queryString.get("Request").map(_.isEmpty).getOrElse(true)) {
      BadRequest("No request provided.")
    } else {
      request.queryString.get("Request").get.head match {
        case "GetCapabilities" =>
          Ok(wps.xml.getcapabilities(wpsMain.processes.values))
        case "DescribeProcess" =>
          if (request.queryString.get("Identifier").map(_.isEmpty).getOrElse(true)) {
            BadRequest("No identifier specified")
          } else {
            val idsStr = request.queryString.get("Identifier").get
            val ids = idsStr map (wpsMain.processes.get)
            if (ids.exists(_.isEmpty)) {
              BadRequest("Unknown id(s): " + idsStr.filter(wpsMain.processes.get(_).isEmpty).mkString(","))
            } else {
              Ok(wps.xml.describeprocess(ids.flatten))
            }
          }
        case a => BadRequest("Unsupported request: " + a)
      }
    }
  }

  def apiAddProcess = Action { implicit request =>
    val textContent = request.body.asText

    textContent.map { text =>
      wpsMain.addScript(text)
      NoContent
    }.getOrElse {
      BadRequest("Expected text/plain body")
    }
  }

  def apiRemoveProcess(name: String) = Action { implicit request =>
    if (!wpsMain.processes.contains(name)) {
      BadRequest("There is no process with name " + name)
    } else {
      wpsMain.removeScript(name)
      NoContent
    }
  }
}
