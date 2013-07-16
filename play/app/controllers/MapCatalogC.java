package controllers;

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

import config.Global;
import constant.Message;
import play.data.*;
import views.html.*;
import play.mvc.*;
import org.orbisgis.server.mapcatalog.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import csp.ContentSecurityPolicy;

@ContentSecurityPolicy
public class MapCatalogC extends Controller{

    private static MapCatalog MC = Global.mc();

    public static MapCatalog getMapCatalog(){
        return MC;
    }

    /**
     * Renders the MapCatalog Public page
     * @return
     */
    public static Result index() {
        try {
            List<Workspace> list = Workspace.page(MC);
            return ok(mapCatalog.render(list));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Renders the MapCatalog MyWorkspace page
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result myWorkspaces(){
        try {
            String[] attributes = {"id_creator"};
            String id = session("id_user");
            String[] values = {id};
            List<Workspace> list = Workspace.page(MC, attributes,values);
            return ok(myWorkspaces.render(list, UserWorkspace.pageWithWorkspace(MC,id)));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Renders the view that represent the inside of a workspace
     * @param id_workspace the id of the workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result viewWorkspace(String id_workspace){
        try {
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            if(wor.getAll_read().equals("1") || Workspace.isCreator(MC,id_workspace,id_user) || UserWorkspace.hasReadRight(MC,id_workspace,id_user)){
                String[] attributes = {"id_root", "id_parent"};
                String[] values = {id_workspace, null};
                List<Folder> listF = Folder.page(MC,attributes,values);
                List<OWSContext> listC = OWSContext.page(MC, attributes, values);
                return ok(workspace.render(listF,listC,wor));
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return index();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Renders the view that represents the inside of a folder
     * @param id_workspace the root of the folder
     * @param id_folder the id of the folder
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result viewFolder(String id_workspace, String id_folder){
        try {
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            if(wor.getAll_read().equals("1") || Workspace.isCreator(MC,id_workspace,id_user) || UserWorkspace.hasReadRight(MC,id_workspace,id_user)){
                String[] attributes = {"id_parent"};
                String[] values = {id_folder};
                List<Folder> listF = Folder.page(MC,attributes,values);
                List<OWSContext> listC = OWSContext.page(MC, attributes, values);
                List<Folder> path = Folder.getPath(MC, id_folder);
                return ok(folder.render(listF,listC,path,wor));
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return index();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Creates a workspace in the database from a form
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createWorkspace(){
        try {
            DynamicForm form = Form.form().bindFromRequest();
            String name = form.get("name");
            String all_read = (form.get("all_read")!=null) ? "1":"0";
            String all_write  = (form.get("all_write")!=null) ? "1":"0";
            String all_manage  = (form.get("all_manage")!=null) ? "1":"0";
            String description = form.get("description");
            Workspace work = new Workspace(session("id_user"),name,all_read,all_write,all_manage,description);
            Long id = work.save(MC);
            flash("info", Message.INFO_WORKSPACE_CREATED);
            return viewWorkspace(id.toString());
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Create a folder in the database from a form when inside the Workspace.html page
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createFolderFromRoot(String id_root){
        try {
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_root};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            if(wor.getAll_write().equals("1") || Workspace.isCreator(MC,id_root,id_user) || UserWorkspace.hasWriteRight(MC, id_root, id_user)){
                DynamicForm form = Form.form().bindFromRequest();
                String name = form.get("name");
                Folder fol = new Folder(id_root,null,name);
                Long id = fol.save(MC);
                flash("info", Message.INFO_FOLDER_CREATED);
                return viewFolder(id_root, id.toString());
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return index();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Create a folder in the database from a form when inside the Folder.html page
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createFolderFromParent(String id_root, String id_parent){
        try {
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_root};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            if(wor.getAll_write().equals("1") || Workspace.isCreator(MC,id_root,id_user) || UserWorkspace.hasWriteRight(MC, id_root, id_user)){
                DynamicForm form = Form.form().bindFromRequest();
                String name = form.get("name");
                Folder fol = new Folder(id_root,id_parent,name);
                Long id = fol.save(MC);
                flash("info", Message.INFO_FOLDER_CREATED);
                return viewFolder(id_root, id.toString());
            }else{
                flash("error", Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return index();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * allow user to monitor a workspace
     * @param id_workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result monitor(String id_workspace){
        try {
            String id_user = session().get("id_user");
            if(UserWorkspace.isMonitoring(MC, id_workspace,id_user) || Workspace.isCreator(MC, id_workspace,id_user)){
                flash("error", Message.ERROR_ALREADY_MONITORING);
                return index();
            }else{
                UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,"0","0","0");
                usewor.save(MC);
                flash("info", Message.INFO_WORKSPACE_MONITORED);
                return noContent();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Renders the management page of workspaces
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result manageWorkspaces(){
        try {
            String id_user = session().get("id_user");
            //List of workspaces created by the user
            String[] attributes = {"id_creator"};
            String[] values = {id_user};
            List<Workspace> listCreated = Workspace.page(MC, attributes,values);
            //List of workspaces monitored and with right access
            HashMap<UserWorkspace,Workspace> listMonitored = UserWorkspace.pageWithWorkspaceManage(MC, id_user);
            return ok(manageWorkspace.render(listCreated, listMonitored));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Displays the management page for a particular workspace, checks if the sender of the http request has rights
     * @param id_workspace the id of the workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result manageAWorkspace(String id_workspace){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            System.out.println(wor.getAll_manage());
            if(wor.getAll_manage().equals("1") || UserWorkspace.hasManageRight(MC, id_workspace,id_user) || Workspace.isCreator(MC, id_workspace,id_user)){
                HashMap list = UserWorkspace.pageWithUser(MC,id_workspace);
                return ok(userManagement.render(list,wor));
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return forbidden(home.render());
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Changes the right of a user for a specific workspace, checks if the sender of the http request has rights
     * @param id_workspace
     * @param id_user
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result changeRights(String id_workspace, String id_user){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_logged = session().get("id_user");
            if(wor.getAll_manage().equals("1") || UserWorkspace.hasManageRight(MC, id_workspace,id_logged) || Workspace.isCreator(MC, id_workspace,id_logged)){
                DynamicForm form = Form.form().bindFromRequest();
                String read = form.get("Read");
                String write = form.get("Write");
                String manage = form.get("Manage");
                UserWorkspace uw = new UserWorkspace(id_user, id_workspace, read, write, manage);
                uw.update(MC);
                return manageAWorkspace(id_workspace);
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return forbidden(home.render());
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Delete a workspace, checks if the sender of the http request has rights
     * @param id_workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result deleteWorkspace(String id_workspace){
        //verification of rights
        try {
            String id_logged = session().get("id_user");
            if(Workspace.isCreator(MC, id_workspace, id_logged)){
                Workspace.delete(MC, Long.valueOf(id_workspace));
                flash("info", Message.INFO_WORKSPACE_DELETED);
                return index();
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return forbidden(home.render());
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Update information about a workspace, checks if the sender of the http request has rights
     * @param id_workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result updateWorkspace(String id_workspace){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor2 = Workspace.page(MC, attributes2, values2).get(0);
            String id_logged = session().get("id_user");
            if(wor2.getAll_manage().equals("0") || UserWorkspace.hasManageRight(MC, id_workspace,id_logged) || Workspace.isCreator(MC, id_workspace,id_logged)){
                DynamicForm form = Form.form().bindFromRequest();
                String name = form.get("name");
                String all_read = (form.get("all_read")!=null) ? "1":"0";
                String all_write  = (form.get("all_write")!=null) ? "1":"0";
                String all_manage  = (form.get("all_manage")!=null) ? "1":"0";
                String description = form.get("description");
                String id_creator = form.get("id_creator");
                Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
                wor.update(MC);
                return manageAWorkspace(id_workspace);
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return forbidden(home.render());
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Casts out a user from a workspace, all_manage parameter is not taken into account here for access rights
     * @param id_workspace
     * @param id_user
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result castOut(String id_workspace, String id_user){
        try {
            //verification of rights
            String id_logged = session().get("id_user");
            if(UserWorkspace.hasManageRight(MC, id_workspace,id_logged) || Workspace.isCreator(MC, id_workspace,id_logged)){
                UserWorkspace.delete(MC, Long.valueOf(id_user), Long.valueOf(id_workspace));
                return manageAWorkspace(id_workspace);
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return forbidden(home.render());
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Deletes a folder from a workspace
     * @param id_root   The workspace parent
     * @param id_folder The folder to delete
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result deleteFolder(String id_root, String id_folder){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_root};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_logged = session().get("id_user");
            if(wor.getAll_manage().equals("1") || UserWorkspace.hasManageRight(MC, id_root,id_logged) || Workspace.isCreator(MC, id_root,id_logged)){
                Folder.delete(MC, Long.valueOf(id_folder));
                flash("info",Message.INFO_FOLDER_DELETED);
                return viewWorkspace(id_root);
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return forbidden(home.render());
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Adds a map context in database with current workspace as root
     * @param id_root
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result addMapContextFromRoot(String id_root){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_root};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_logged = session().get("id_user");
            if(wor.getAll_write().equals("1") || UserWorkspace.hasWriteRight(MC, id_root, id_logged) || Workspace.isCreator(MC, id_root,id_logged)){
                MultipartFormData body = request().body().asMultipartFormData();
                FilePart file = body.getFile("mapcontext");
                if(file!=null){
                    String title = file.getFilename();
                    try{
                        FileInputStream context = new FileInputStream(file.getFile());
                        OWSContext ows = new OWSContext(id_root,null,id_logged,title);
                        ows.save(MC,context);
                    }catch(FileNotFoundException e){
                        e.printStackTrace(); //unreachable code
                    }
                    flash("info", Message.INFO_OWS_CREATED);
                    return viewWorkspace(id_root);
                }else{
                    flash("error",Message.ERROR_FILE_INVALID);
                    return viewWorkspace(id_root);
                }
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return forbidden(home.render());
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Adds a map context with current folder as parent
     * @param id_root
     * @param id_parent
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result addMapContextFromParent(String id_root, String id_parent){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_root};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_logged = session().get("id_user");
            if(wor.getAll_write().equals("1") || UserWorkspace.hasWriteRight(MC, id_root, id_logged) || Workspace.isCreator(MC, id_root,id_logged)){
                MultipartFormData body = request().body().asMultipartFormData();
                FilePart file = body.getFile("mapcontext");
                if(file!=null){
                    String title = file.getFilename();
                    try{
                        FileInputStream context = new FileInputStream(file.getFile());
                        OWSContext ows = new OWSContext(id_root,id_parent,id_logged,title);
                        ows.save(MC,context);
                    }catch(FileNotFoundException e){
                        e.printStackTrace(); //unreachable code
                    }
                    flash("info", Message.INFO_OWS_CREATED);
                    return viewWorkspace(id_root);
                }else{
                    flash("error",Message.ERROR_FILE_INVALID);
                    return viewWorkspace(id_root);
                }
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return forbidden(home.render());
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Search and return a specific list of workspaces
     * @return
     */
    public static Result searchPublicWorkspaces(){
        try {
            DynamicForm form = Form.form().bindFromRequest();
            String search = form.get("search");
            List<Workspace> list = Workspace.search(MC,search);
            return ok(mapCatalog.render(list));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Search and return a specific list of folders and ows from the root of the worksapce
     * @param id_root
     * @return
     */
    public static Result searchFromRoot(String id_root){
        try {
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_root};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            if(wor.getAll_read().equals("1") || Workspace.isCreator(MC,id_root,id_user) || UserWorkspace.hasReadRight(MC,id_root,id_user)){
                DynamicForm form = Form.form().bindFromRequest();
                String search = form.get("search");
                List<Folder> listF = Folder.search(MC,id_root,search);
                List<OWSContext> listC = OWSContext.search(MC,id_root,search);
                return ok(workspace.render(listF,listC,wor));
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return index();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Search and return a specific list of folders and ows from the root of the workspace
     * @param id_root
     * @param id_folder
     * @return
     */
    public static Result searchFromParent(String id_root, String id_folder){
        try {
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_root};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            if(wor.getAll_read().equals("1") || Workspace.isCreator(MC,id_root,id_user) || UserWorkspace.hasReadRight(MC,id_root,id_user)){
                DynamicForm form = Form.form().bindFromRequest();
                String search = form.get("search");
                List<Folder> listF = Folder.search(MC,id_root,search);
                List<OWSContext> listC = OWSContext.search(MC,id_root,search);
                List<Folder> path = Folder.getPath(MC, id_folder);
                return ok(folder.render(listF,listC,path,wor));
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return index();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Search and return a specific list of workspaces for the page myWorkspaces
     * @return
     */
    public static Result searchMyWorkspaces(){
        try {
            DynamicForm form = Form.form().bindFromRequest();
            String search = form.get("search");
            String id_user = session("id_user");
            List<Workspace> list = Workspace.searchMyWorkspacesCreated(MC,search,id_user);
            HashMap<UserWorkspace,Workspace> hm = UserWorkspace.searchMyWorkspacesMonitored(MC,search,id_user);
            return ok(myWorkspaces.render(list, hm));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Displays the page for OWS context from a folder
     * @param id_workspace
     * @param id_folder
     * @param id_owscontext
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result viewOWSFromParent(String id_workspace, String id_folder, String id_owscontext){
        try {
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            if(wor.getAll_read().equals("1") || Workspace.isCreator(MC,id_workspace,id_user) || UserWorkspace.hasReadRight(MC,id_workspace,id_user)){
                String[] attributes = {"id_parent"};
                String[] values = {id_folder};
                List<Folder> listF = Folder.page(MC,attributes,values);
                List<OWSContext> listC = OWSContext.page(MC, attributes, values);
                List<Folder> path = Folder.getPath(MC, id_folder);
                OWSContext theContext = null;
                for(int i = 0; i<listC.size(); i++){
                    if(listC.get(i).getId_owscontext().equals(id_owscontext)){
                        theContext = listC.get(i);
                        break;
                    }
                }
                if(theContext!=null){
                    return ok(contextFolder.render(listF,listC,path,wor,theContext));
                }else{
                    flash("error",Message.ERROR_GENERAL);
                    return viewFolder(id_workspace,id_folder);
                }
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return index();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Display the page for OWS context from a workspace
     * @param id_workspace
     * @param id_owscontext
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result viewOWSFromRoot(String id_workspace, String id_owscontext){
        try {
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
            if(wor.getAll_read().equals("1") || Workspace.isCreator(MC,id_workspace,id_user) || UserWorkspace.hasReadRight(MC,id_workspace,id_user)){
                String[] attributes = {"id_root", "id_parent"};
                String[] values = {id_workspace, null};
                List<Folder> listF = Folder.page(MC,attributes,values);
                List<OWSContext> listC = OWSContext.page(MC, attributes, values);
                OWSContext theContext = null;
                for(int i = 0; i<listC.size(); i++){
                    if(listC.get(i).getId_owscontext().equals(id_owscontext)){
                        theContext = listC.get(i);
                        break;
                    }
                }
                if(theContext!=null){
                    return ok(contextWorkspace.render(listF,listC,wor,theContext));
                }else{
                    flash("error",Message.ERROR_GENERAL);
                    return viewWorkspace(id_workspace);
                }
            }else{
                flash("error",Message.ERROR_UNAUTHORIZED_WORKSPACE);
                return index();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Sends the ows context
     * @param id
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result downloadContext(String id){
        try {
            String[] attributes = {"id_owscontext"};
            String[] values = {id};
            List<OWSContext> list = OWSContext.page(MC, attributes, values);
            if(list.isEmpty()){
                return badRequest();
            }else{
                return ok(list.get(0).getContent(MC));
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }
}