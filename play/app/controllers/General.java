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
import org.orbisgis.server.mapcatalog.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import csp.ContentSecurityPolicy;

@ContentSecurityPolicy
public class General extends Controller{
    private static MapCatalog MC = MapCatalogC.getMapCatalog();

    public static Result home() {
        return ok(home.render());
    }

    public static class Login {

        public String email;
        public String password;

    }

    public static class Signin {
        public String name;
        public String email;
        public String password;
        public String location;
    }

    public static Result login() {
        return ok(login.render(Form.form(Login.class),""));
    }

    public static Result authenticate() throws Exception{
        Form<Login> form = Form.form(Login.class).bindFromRequest();
        Login log = form.get();
        String email = log.email;
        String password = log.password;
        String error ="";
        if(email != null && password != null){
            ArrayList<ArrayList<String>> user = MC.selectWhere("user","email="+email+", password="+MC.hasher(password));
            if(!user.isEmpty()){
                session("email", email);
                return ok(home.render());
            }else{error="Error: Email or password invalid";}
        }
        return (badRequest(login.render(form,error)));
    }

    public static Result logout(){
        session().clear();
        return redirect(routes.General.login());
    }

    public static Result signin(){
        return ok(signin.render(Form.form(Signin.class),""));
    }

    public static Result signedin() throws NoSuchAlgorithmException {
        Form<Signin> form = Form.form(Signin.class).bindFromRequest();
        Signin sign = form.get();
        ArrayList<ArrayList<String>> user = MC.selectWhere("user","email="+sign.email);
        String error="";
        if(sign.email!=null && sign.password.length()>=6){
            if(user.isEmpty()){
                User usr = new User(sign.name, sign.email, sign.password, sign.location);
                usr.save(MC);
                return ok(home.render());
            }else{error="Error: Email already used";}
        }else{error="Error: Email or password invalid";}
        return (badRequest(signin.render(form,error)));
    }
}