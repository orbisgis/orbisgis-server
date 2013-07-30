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
public class Admin extends Controller{
    private static MapCatalog MC = MapCatalogC.getMapCatalog();


    /**
     * renders the admin panel
     * @return The administration panel page
     */
    @Security.Authenticated(Secured.class)
    public static Result admin() {
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            if(Integer.valueOf(user.getAdmin_mapcatalog())<=10 || Integer.valueOf(user.getAdmin_wms())<=10 || Integer.valueOf(user.getAdmin_wps())<=10){
                return ok(admin.render(user));
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * renders the admin panel
     * @return The administration panel page
     */
    @Security.Authenticated(Secured.class)
    public static Result adminWMS(int offset) {
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            List<User> allUsers = User.pageOffset(MC, offset);
            int pageNumber = (User.pageCount(MC)-1)/10+1;
            int currentPage = (offset)/10+1;
            if(Integer.valueOf(user.getAdmin_mapcatalog())<=10 || Integer.valueOf(user.getAdmin_wms())<=10 || Integer.valueOf(user.getAdmin_wps())<=10){
                return ok(adminWMS.render(user,allUsers, currentPage, pageNumber));
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * renders the admin panel
     * @return The administration panel page
     */
    @Security.Authenticated(Secured.class)
    public static Result adminMapCatalog(int offset) {
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            List<User> allUsers = User.pageOffset(MC, offset);
            int pageNumber = (User.pageCount(MC)-1)/10+1;
            int currentPage = (offset)/10+1;
            if(Integer.valueOf(user.getAdmin_mapcatalog())<=10 || Integer.valueOf(user.getAdmin_wms())<=10 || Integer.valueOf(user.getAdmin_wps())<=10){
                return ok(adminMapCatalog.render(user, allUsers, currentPage, pageNumber));
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * renders the admin panel with an offset
     * @return The administration panel page
     */
    @Security.Authenticated(Secured.class)
    public static Result adminWPS(int offset) {
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            List<User> allUsers = User.pageOffset(MC, offset);
            int pageNumber = (User.pageCount(MC)-1)/10+1;
            int currentPage = (offset)/10+1;
            if(Integer.valueOf(user.getAdmin_mapcatalog())<=10 || Integer.valueOf(user.getAdmin_wms())<=10 || Integer.valueOf(user.getAdmin_wps())<=10){
                return ok(adminWPS.render(user,allUsers, currentPage, pageNumber));
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * Changes the admin rights of a user in the MapCatalog, only accessible by an Admin of MapCatalog, and if the right is set to 0, the user becomes a super admin
     * @param id_userToChange the user to change the rights
     * @return The admin page of map catalog, ot the home page if errors
     */
    @Security.Authenticated(Secured.class)
    public static Result changeMapCatalogRight(String id_userToChange) {
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            if(Integer.valueOf(user.getAdmin_mapcatalog())<=10){
                DynamicForm form = Form.form().bindFromRequest();
                String right = form.get("admin");
                values[0] = id_userToChange;
                User toChange = User.page(MC,attributes, values).get(0);
                if(right.equals("0")){
                    toChange.setAdmin_mapcatalog(right);
                    toChange.setAdmin_wms(right);
                    toChange.setAdmin_wps(right);
                    toChange.updateAdminRights(MC);
                }else{
                    toChange.setAdmin_mapcatalog(right);
                    toChange.updateAdminRights(MC);
                }
                return adminMapCatalog(0);
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * Changes the admin rights of a user in the wps, only accessible by an Admin of wps, and if the right is set to 0, the user becomes a super admin
     * @param id_userToChange the user to change the rights
     * @return The admin page of wps, ot the home page if errors
     */
    @Security.Authenticated(Secured.class)
    public static Result changeWPSRight(String id_userToChange) {
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            if(Integer.valueOf(user.getAdmin_wps())<=10){
                DynamicForm form = Form.form().bindFromRequest();
                String right = form.get("admin");
                values[0] = id_userToChange;
                User toChange = User.page(MC,attributes, values).get(0);
                if(right.equals("0")){
                    toChange.setAdmin_mapcatalog(right);
                    toChange.setAdmin_wms(right);
                    toChange.setAdmin_wps(right);
                    toChange.updateAdminRights(MC);
                }else{
                    toChange.setAdmin_wps(right);
                    toChange.updateAdminRights(MC);
                }
                return adminWPS(0);
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * Changes the admin rights of a user in the wms, only accessible by an Admin of wms, and if the right is set to 0, the user becomes a super admin
     * @param id_userToChange the user to change the rights
     * @return The admin page of wms, ot the home page if errors
     */
    @Security.Authenticated(Secured.class)
    public static Result changeWMSRight(String id_userToChange) {
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            if(Integer.valueOf(user.getAdmin_wms())<=10){
                DynamicForm form = Form.form().bindFromRequest();
                String right = form.get("admin");
                values[0] = id_userToChange;
                User toChange = User.page(MC,attributes, values).get(0);
                if(right.equals("0")){
                    toChange.setAdmin_mapcatalog(right);
                    toChange.setAdmin_wms(right);
                    toChange.setAdmin_wps(right);
                    toChange.updateAdminRights(MC);
                }else{
                    toChange.setAdmin_wms(right);
                    toChange.updateAdminRights(MC);
                }
                return adminWMS(0);
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * Deletes a user from database definitely, cannot be cast on a Super Administrator, and can only be cast by an administrator
     * @param id_userToDelete The id of the user to delete
     * @param url The url where the logged admin comes from
     * @return The last page visited by logged admin, or the home page if errors
     */
    @Security.Authenticated(Secured.class)
    public static Result deleteUser(String id_userToDelete, String url) {
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            if(Integer.valueOf(user.getAdmin_mapcatalog())<=10 || Integer.valueOf(user.getAdmin_wms())<=10 || Integer.valueOf(user.getAdmin_wps())<=10){
                values[0] = id_userToDelete;
                User toChange = User.page(MC,attributes, values).get(0);
                if(!toChange.getAdmin_mapcatalog().equals("0")){
                    User.delete(MC,Long.valueOf(id_userToDelete));
                    flash("info", "User deleted");
                    return redirect("/admin/"+url);
                }else{
                    flash("error", Message.ERROR_UNAUTHORIZED_USER);
                }
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * Search and return a specific list of users
     * @return The wms page with searched users
     */
    public static Result searchAdminWMS(int offset){
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            if(Integer.valueOf(user.getAdmin_wms())<=10){
                DynamicForm form = Form.form().bindFromRequest();
                String search = form.get("search");
                List<User> allUsers = User.search(MC,search, offset);
                int pageNumber = (User.searchCount(MC, search)-1)/10+1;
                int currentPage = (offset)/10+1;
                return ok(adminWMS.render(user,allUsers, currentPage, pageNumber));
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * Search and return a specific list of users
     * @return The wps page with searched users
     */
    public static Result searchAdminWPS(int offset){
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            if(Integer.valueOf(user.getAdmin_wms())<=10){
                DynamicForm form = Form.form().bindFromRequest();
                String search = form.get("search");
                List<User> allUsers = User.search(MC,search, offset);
                int pageNumber = (User.searchCount(MC, search)-1)/10+1;
                int currentPage = (offset)/10+1;
                return ok(adminWPS.render(user,allUsers, currentPage, pageNumber));
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }

    /**
     * Search and return a specific list of users
     * @return The wps page with searched users
     */
    public static Result searchAdminMapCatalog(int offset){
        try {
            String id_user = session().get("id_user");
            String[] attributes = {"id_user"};
            String[] values = {id_user};
            User user = User.page(MC, attributes, values).get(0);
            if(Integer.valueOf(user.getAdmin_wms())<=10){
                DynamicForm form = Form.form().bindFromRequest();
                String search = form.get("search");
                List<User> allUsers = User.search(MC,search, offset);
                int pageNumber = (User.searchCount(MC, search)-1)/10+1;
                int currentPage = (offset)/10+1;
                return ok(adminMapCatalog.render(user,allUsers, currentPage, pageNumber));
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_USER);
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            Logger.error("", e);
        }
        return General.home();
    }
}
