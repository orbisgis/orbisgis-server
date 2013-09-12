package controllers

/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * For more information, please consult: <http://www.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */

import play.api.mvc._
import org.orbisgis.server.mapcatalog.MapCatalog
import org.orbisgis.server.mapcatalog.User
import config.Global

/**
 * Trait to verify if a user can access WMS manage
 */
trait SecuredWMS {

  private val MC: MapCatalog = Global.mc

  def username(request: RequestHeader): Option[String] = {
    val id_user : Option[String] = request.session.get("id_user")
    val attributes = Array("id_user")
    if(id_user!=None){
      val values = Array(id_user.get)
      val isAdmin = User.page(MC, attributes, values).get(0).getAdmin_wms
      isAdmin match {
        case "0" =>
          request.session.get("email")
        case "10" =>
          request.session.get("email")
        case _ =>
          None
      }
    }else{
      None
    }
  }

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.General.home()).flashing("error" -> "OrbisData and OrbisProcessing can only be accessed by an administrator")

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
}

/**
 * Trait to verify if a user can access WPS manage
 */
trait SecuredWPS {

  private val MC: MapCatalog = Global.mc

  def username(request: RequestHeader): Option[String] = {
    val id_user : Option[String] = request.session.get("id_user")
    val attributes = Array("id_user")
    if(id_user!=None){
      val values = Array(id_user.get)
      val isAdmin = User.page(MC, attributes, values).get(0).getAdmin_wps
      isAdmin match {
        case "0" =>
          request.session.get("email")
        case "10" =>
          request.session.get("email")
        case _ =>
          None
      }
    }else{
      None
    }
  }

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.General.home()).flashing("error" -> "access denied")

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
}