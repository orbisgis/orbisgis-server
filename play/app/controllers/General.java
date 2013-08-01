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

import constant.Message;
import play.Logger;
import play.data.*;
import views.html.*;
import play.mvc.*;
import org.orbisgis.server.mapcatalog.*;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import csp.ContentSecurityPolicy;

@ContentSecurityPolicy
public class General extends Controller{
    private static MapCatalog MC = MapCatalogC.getMapCatalog();

    /**
     * Renders the home page
     * @return the home page rendered
     */
    public static Result home() {
        return ok(home.render());
    }

    /**
     * Renders the login page
     * @param uri The last uri the user used
     * @return the login page rendered
     */
    public static Result login(String uri) {
        return ok(login.render("",uri));
    }

    /**
     * Checks if the login form is correct, and logs in the user
     * @param uri The last uri the user used
     * @return The home page if success, the login page with error if error.
     */
    public static Result authenticate(String uri) {
        String error="";
        try {
            DynamicForm form = Form.form().bindFromRequest();
            String email = form.get("email");
            String password = form.get("password");
            if(email != null && password != null){
                String[] attributes = {"email","password"};
                String[] values = {email,MapCatalog.hasher(password)};
                List<User> list = User.page(MC, attributes, values);
                if(!list.isEmpty()){
                    User user = list.get(0);
                    session().clear();
                    session("email", email);
                    session("id_user", user.getId_user());
                    if(Integer.valueOf(user.getAdmin_mapcatalog())<=10||Integer.valueOf(user.getAdmin_wms())<=10||Integer.valueOf(user.getAdmin_wps())<=10){
                        session("admin","yes");
                    }
                    return redirect(uri);
                }else{error= Message.ERROR_LOGIN;}
            }
        } catch (NoSuchAlgorithmException e) {
            error= Message.ERROR_GENERAL;
            Logger.error("Hashing algorithm failed", e);
        } catch (SQLException e) {
            error= Message.ERROR_GENERAL;
            Logger.error("", e);
        }
        return badRequest(login.render(error, uri));
    }

    /**
     * Clear the cookie session
     * @return The login page
     */
    public static Result logout(){
        session().clear();
        return redirect(routes.General.login("/home"));
    }

    /**
     * Renders the sign in page only if no one is logged in
     * @return Thr signin page rendered
     */
    public static Result signin(){
        if(session().get("email")!=null){
            flash("error", Message.ERROR_ALREADY_LOGGED);
            return forbidden(home.render());
        }
        return ok(signin.render(""));
    }

    /**
     * Saves the user that just signed in
     * @return the home page, of the sign in page with errors
     */
    public static Result signedin() {
        String error;
        try {
            DynamicForm form = Form.form().bindFromRequest();
            String email = form.get("email");
            String location = form.get("location");
            String name = form.get("name");
            String password = form.get("password");
            String password2 = form.get("password2");
            String[] attribute = {"email"};
            String[] values = {email};
            List<User> user = User.page(MC, attribute, values);
            if(email!=null && password.length()>=6){ //check the form
                if(password.equals(password2)){
                    if(user.isEmpty()){ //check if user mail is used
                        User usr = new User(name, email, password, location);
                        usr.save(MC);
                        return ok(home.render());
                    }else{error= Message.ERROR_EMAIL_USED;}
                }else{error= Message.ERROR_PASSWORD_MATCH;}
            }else{error= Message.ERROR_LOGIN;}
        } catch (SQLException e) {
            error= Message.ERROR_GENERAL;
        } catch (NoSuchAlgorithmException e) {
            error= Message.ERROR_GENERAL;
        }
        return (badRequest(signin.render(error)));
    }

    /**
     * Generates the profile page
     * @return the profile page, or home if errors
     */
    @Security.Authenticated(Secured.class)
    public static Result profilePage() {
        try {
            String id_user = session("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User use = User.page(MC, attributes, values).get(0);
            return ok(profile.render(use));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return home();

    }

    /**
     * Update the profile of a user
     * @return The profile page or home if errors
     */
    @Security.Authenticated(Secured.class)
    public static Result changeProfile() {
        try {
            String id_user = session("id_user");
            DynamicForm form = Form.form().bindFromRequest();
            String name = form.get("name");
            String email = form.get("email");
            String location = form.get("location");
            String profession = form.get("profession");
            String additional = form.get("additional");
            session("email",email);
            User use = new User(id_user,name,email,"",location,profession,additional,null,null,null);
            use.update(MC);
            return profilePage();
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return home();
    }

    /**
     * Generates the page not found
     * @return the page not found
     */
    public static Result PageNotFound(){
        return notFound(notFound.render());
    }

    /**
     * Deletes the account of the connected user
     * @return the sign in page, or home page if errors
     */
    public static Result deleteAccount(){
        try {
            String id_user = session("id_user");
            User.delete(MC, Long.valueOf(id_user));
            session().clear();
            return signin();
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return home();
    }

    /**
     * Change the password of a user
     * @return the profile page
     */
    @Security.Authenticated(Secured.class)
    public static Result changePass() {
        try {
            String id_user = session("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User use = User.page(MC, attributes, values).get(0);
            DynamicForm form = Form.form().bindFromRequest();
            String currentpass = form.get("currentpass");
            String newpass = form.get("newpass");
            String newpass2 = form.get("newpass2");
            System.out.println(newpass+" "+newpass2);
            if(newpass.equals(newpass2)){
                if(newpass.length()>=6){
                    if(MapCatalog.hasher(currentpass).equals(use.getPassword())){
                        User newUse = new User(id_user,null,null,MapCatalog.hasher(newpass),null,null,null,null,null,null);
                        newUse.updatePass(MC);
                        flash("info", Message.INFO_PASSWORD_UPDATED);
                    }else{flash("error", Message.ERROR_PASSWORD_INVALID);}
                }else{flash("error", Message.ERROR_PASSWORD_LENGTH);}
            }else{flash("error", Message.ERROR_PASSWORD_MATCH);}
            return profilePage();
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        } catch (NoSuchAlgorithmException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("Hasher algorithm failed to execute", e);
        }
        return home();
    }

    @Security.Authenticated(Secured.class)
    public static Result userView(String id_user){
        try{
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes,values).get(0);
            return ok(userView.render(user));

        }catch (SQLException e){
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }
}