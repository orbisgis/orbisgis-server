package controllers;

/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 * <p/>
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * <p/>
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 * <p/>
 * This file is part of OrbisGIS.
 * <p/>
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * For more information, please consult: <http://www.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */

import play.data.*;
import play.mvc.*;
import views.html.*;
import org.mindrot.jbcrypt.*;

import org.orbisgis.server.mapcatalog.MapCatalog;
import java.util.ArrayList;

public class General extends Controller{

    public static Result home() {
        return ok(home.render());
    }

    public static Result login() {
        System.out.println("aaaaaaaaa");
        return ok(login.render());
    }

    public static Result authenticate(){
        DynamicForm form = Form.form().bindFromRequest();
        String email = form.get("inputEmail");
        String password = form.get("Password");
        System.out.println(email + password);
        if(email!=null || password!=null){
            ArrayList<ArrayList<String>> user = MapCatalog.selectWhere("user","email="+email+", password="+password);
            if(!user.isEmpty()){
                session("email", email);
                return ok(home.render());
            }
        }
        System.out.println("aaaaaaaaa");
        return (badRequest(login.render()));
    }

    public static Result logout(){
        session().clear();
        return ok(home.render());
    }
}
