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

    /**
     * Getter of current session of MapCatalog
     * @return The current MapCatalog
     */
    public static MapCatalog getMapCatalog(){
        return MC;
    }

    /**
     * Renders the MapCatalog Public page (the first ten of all workspaces)
     * @return The index page with the list of first 10 results of all workspaces
     */
    public static Result index() {
        try {
            List<Workspace> list = Workspace.page(MC,0);
            int pages = (Workspace.pageCount(MC)-1)/10+1;
            return ok(mapCatalog.render(list,1,pages));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Renders the MapCatalog MyWorkspace page (the first ten of each created and monitored workspaces)
     * @return The page myworkspace with a list of created workspaces, and monitored workspaces (first 10 results of each)
     */
    @Security.Authenticated(Secured.class)
    public static Result myWorkspaces(){
        try {
            String[] attributes = {"id_creator"};
            String id = session("id_user");
            String[] values = {id};
            int pagesCreated = (Workspace.pageCount(MC,attributes,values)-1)/10+1;
            int pagesMonitored = (UserWorkspace.pageWithWorkspaceCount(MC, id)-1)/10+1;
            List<Workspace> list = Workspace.page(MC, attributes,values,0);
            HashMap<UserWorkspace,Workspace> hm = UserWorkspace.pageWithWorkspace(MC,id,0);
            return ok(myWorkspaces.render(list,hm,pagesCreated,pagesMonitored));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
            e.printStackTrace();
        }
        return General.home();
    }

    /**
     * Renders the view that represent the inside of a workspace
     * @param id_workspace the id of the workspace
     * @return The view corresponding to the selected workspace
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
                boolean hasDeleteRights = UserWorkspace.hasManageRight(MC, id_workspace, id_user)||(wor.getAll_manage().equals("1"));
                return ok(workspace.render(listF,listC,wor,hasDeleteRights));
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
     * @return The view corresponding to the selected folder
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
                boolean hasDeleteRights = UserWorkspace.hasManageRight(MC, id_workspace, id_user)||wor.getAll_manage().equals("1");
                return ok(folder.render(listF,listC,path,wor,hasDeleteRights));
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
     * @return The workspace page of the created workspace
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
     * @param id_root The root of the folder to be created
     * @return The folder page of the created folder
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
     * @param id_root The root of the folder to be created
     * @param id_parent the parent of the folder to be created
     * @return The folder page of the created folder
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
     * @param id_workspace the workspace to be monitored
     * @return The myWorkspace page with a message info if success, or home if error
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
                return myWorkspaces();
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Renders the management page of workspaces
     * @return The page with the list of workspaces manageable, or home if error
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
     * Displays the management page for a particular workspace
     * @param id_workspace the id of the workspace to be managed
     * @return The management page of the workspace, or home if error
     */
    @Security.Authenticated(Secured.class)
    public static Result manageAWorkspace(String id_workspace){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_user = session().get("id_user");
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
     * Changes the right of a user for a specific workspace
     * @param id_workspace The workspace where the rights are valid
     * @param id_user The user concerned by the new rights
     * @return The management page, or home page if error
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
     * Delete a workspace
     * @param id_workspace The workspace to be deleted
     * @return The mapCatalog index if success, or the home page if error
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
     * Update information about a workspace
     * @param id_workspace The workspace to be updated
     * @return The management page, or the home page if errors
     */
    @Security.Authenticated(Secured.class)
    public static Result updateWorkspace(String id_workspace){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_workspace};
            Workspace wor2 = Workspace.page(MC, attributes2, values2).get(0);
            String id_logged = session().get("id_user");
            if(wor2.getAll_manage().equals("1") || UserWorkspace.hasManageRight(MC, id_workspace,id_logged) || Workspace.isCreator(MC, id_workspace,id_logged)){
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
     * Casts out a user from a workspace, all_manage parameter is not taken into account here for access rights (only
     * @param id_workspace The workspace that is currently monitored by the chosen user
     * @param id_user The chosen user to be casted out of the workspace
     * @return The management page, or home page if error
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
     * @return The workspace page with info message if success, or error message if error
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
     * @param id_root The workspace chosen as root for the context
     * @return The workspace page with a info message if success, or error message if error
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
     * @param id_root The workspace chosen as root for the context
     * @param id_parent The folder chosen as parent for the context
     * @return The workspace page, with a info message if success, or error message if error.
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
     * Search and return a specific list of workspaces from all workspaces
     * @param offset The pagination of the searched workspaces
     * @return The mapCatalog index page with a list of workspaces between offset and offset+10, corresponding to the search query
     */
    public static Result searchPublicWorkspaces(int offset){
        try {
            DynamicForm form = Form.form().bindFromRequest();
            String search = form.get("search");
            List<Workspace> list = Workspace.search(MC,search, offset);
            int pages = ((Workspace.searchCount(MC, search)-1)/10)+1;
            int page = offset/10+1;
            flash("search",search);
            return ok(mapCatalog.render(list,page,pages));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Search and return a specific list of folders and ows from the root of the workspace
     * @param id_root The workspace where the search query is asked
     * @return The workspace page, with a list of folder and context contained in this workspace, corresponding to the query
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
                boolean hasDeleteRights = UserWorkspace.hasManageRight(MC, id_root, id_user)||wor.getAll_manage().equals("1");
                return ok(workspace.render(listF,listC,wor,hasDeleteRights));
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
     * @param id_root the root of the folder
     * @param id_folder the folder in which the user is
     * @return The root workspace page
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
                boolean hasDeleteRights = UserWorkspace.hasManageRight(MC, id_root, id_user)||wor.getAll_manage().equals("1");
                return ok(folder.render(listF,listC,path,wor,hasDeleteRights));
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
     * Search and return a specific list of workspaces for the page myWorkspaces with pagination(offset)
     * @param choice the page that is displayed, if none where chosen, both are displayed with 0 offset
     * @param offset the number of the page that needs to be displayed
     * @return MyWorkspace page with lists of workspaces Created and Monitored conrresponding to the pagination, and the search query
     */
    public static Result searchMyWorkspaces(String choice, int offset){
        try {
            DynamicForm form = Form.form().bindFromRequest();
            String search = form.get("search");
            String id_user = session("id_user");
            int pagesCreated = (Workspace.searchMyWorkspacesCreatedCount(MC,search,id_user)-1)/10+1;
            int pagesMonitored = (UserWorkspace.searchMyWorkspacesMonitoredCount(MC,search,id_user)-1)/10+1;
            int currentpage = offset/10+1;
            flash("search",search);
            switch (choice) {
                case "created": {
                    List<Workspace> list = Workspace.searchMyWorkspacesCreated(MC, search, id_user, offset);
                    HashMap<UserWorkspace, Workspace> hm = UserWorkspace.searchMyWorkspacesMonitored(MC, search, id_user, 0);
                    flash("created", Integer.toString(currentpage));
                    return ok(myWorkspaces.render(list, hm, pagesCreated, pagesMonitored));
                }
                case "monitored": {
                    List<Workspace> list = Workspace.searchMyWorkspacesCreated(MC, search, id_user, 0);
                    HashMap<UserWorkspace, Workspace> hm = UserWorkspace.searchMyWorkspacesMonitored(MC, search, id_user, offset);
                    flash("monitored", Integer.toString(currentpage));
                    return ok(myWorkspaces.render(list, hm, pagesCreated, pagesMonitored));
                }
                default: {
                    List<Workspace> list = Workspace.searchMyWorkspacesCreated(MC, search, id_user, 0);
                    HashMap<UserWorkspace, Workspace> hm = UserWorkspace.searchMyWorkspacesMonitored(MC, search, id_user, 0);
                    return ok(myWorkspaces.render(list, hm, pagesCreated, pagesMonitored));
                }
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Displays the page for OWS context from a folder
     * @param id_workspace the root of the context
     * @param id_folder the parent of the context
     * @param id_owscontext the context
     * @return A page that displays the preview and comments about a context
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
                for (OWSContext aListC : listC) {
                    if (aListC.getId_owscontext().equals(id_owscontext)) {
                        theContext = aListC;
                        break;
                    }
                }
                if(theContext!=null){

                    boolean hasDeleteRights = UserWorkspace.hasManageRight(MC, id_workspace, id_user)||wor.getAll_manage().equals("1");
                    return ok(contextFolder.render(listF,listC,path,wor,theContext,hasDeleteRights));
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
     * @param id_workspace The root workspace of the ows context
     * @param id_owscontext The id of the ows to display
     * @return The page where a preview and comments are displayed about the context
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
                for (OWSContext aListC : listC) {
                    if (aListC.getId_owscontext().equals(id_owscontext)) {
                        theContext = aListC;
                        break;
                    }
                }
                if(theContext!=null){

                    boolean hasDeleteRights = UserWorkspace.hasManageRight(MC, id_workspace, id_user)||wor.getAll_manage().equals("1");
                    return ok(contextWorkspace.render(listF,listC,wor,theContext,hasDeleteRights));
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
     * Sends the OWScontext to the user
     * @param id the id of the context
     * @return The OWScontext to download
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
                response().setHeader("Content-Disposition", "attachment; filename="+list.get(0).getTitle()+".ows");
                return ok(list.get(0).getContent(MC));
            }
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * deletes a context from database
     * @param id_root the root of the context
     * @param id_owscontext the id of the context to be deleted
     * @return The view of the root workspace, or the home page if errors.
     */
    @Security.Authenticated(Secured.class)
    public static Result deleteContext(String id_root, String id_owscontext){
        try {
            //verification of rights
            String[] attributes2 = {"id_workspace"};
            String[] values2 = {id_root};
            Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
            String id_logged = session().get("id_user");
            if(wor.getAll_manage().equals("1") || UserWorkspace.hasManageRight(MC, id_root,id_logged) || Workspace.isCreator(MC, id_root,id_logged)){
                OWSContext.delete(MC, Long.valueOf(id_owscontext));
                flash("info",Message.INFO_OWS_DELETED);
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
     * delete the relation UserWorkspace corresponding to the monitoring action of a user in a workspace
     * @param id_workspace the workspace which will not be monitored anymore
     * @return the page MyWorkspaces with either an info of success, or an error message
     */
    @Security.Authenticated(Secured.class)
    public static Result stopMonitoring(String id_workspace){
        String id_user = session().get("id_user");
        try{
            UserWorkspace.delete(MC, Long.valueOf(id_user), Long.valueOf(id_workspace));
            flash("info", Message.INFO_STOP_MONITORING);
        }catch (SQLException e){
            flash("error", Message.ERROR_GENERAL);
        }
        return myWorkspaces();
    }

    /**
     * Renders the MapCatalog Public page with result between offset and offset +10
     * @param offset The beginning of workspaces to display
     * @return The mapCatalog index page with workspaces beginning at an offset
     */
    @Security.Authenticated(Secured.class)
    public static Result indexOffset(int offset) {
        try {
            List<Workspace> list = Workspace.page(MC, offset);
            int pages = ((Workspace.pageCount(MC)-1)/10)+1;
            int page = offset/10+1;
            return ok(mapCatalog.render(list,page,pages));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Renders the MapCatalog Myworkspace page with result between offset and offset +10
     * @param offset The beginning of workspaces to display
     * @return The MyWorkspace page with Created workspaces beginning at an offset (pagination)
     */
    @Security.Authenticated(Secured.class)
    public static Result myWorkspacesCreatedOffset(int offset) {
        try {
            String[] attributes = {"id_creator"};
            String id = session("id_user");
            String[] values = {id};
            List<Workspace> list = Workspace.page(MC, attributes,values,offset);
            HashMap<UserWorkspace,Workspace> hm = UserWorkspace.pageWithWorkspace(MC,id,0);
            int pagesCreated = (Workspace.pageCount(MC,attributes,values)-1)/10+1;
            int pagesMonitored = (UserWorkspace.pageWithWorkspaceCount(MC, id)-1)/10+1;
            int currentpage = offset/10+1;
            flash("created",Integer.toString(currentpage));
            return ok(myWorkspaces.render(list,hm,pagesCreated,pagesMonitored));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Renders the MapCatalog Myworkspace page with result between offset and offset +10
     * @param offset The beginning of workspaces to display
     * @return The MyWorkspace page with Monitored workspaces beginning at an offset (pagination)
     */
    @Security.Authenticated(Secured.class)
    public static Result myWorkspacesMonitoredOffset(int offset) {
        try {
            String[] attributes = {"id_creator"};
            String id = session("id_user");
            String[] values = {id};
            List<Workspace> list = Workspace.page(MC, attributes,values,0);
            HashMap<UserWorkspace,Workspace> hm = UserWorkspace.pageWithWorkspace(MC,id,offset);
            int pagesCreated = (Workspace.pageCount(MC,attributes,values)-1)/10+1;
            int pagesMonitored = (UserWorkspace.pageWithWorkspaceCount(MC, id)-1)/10+1;
            int currentpage = offset/10+1;
            flash("monitored",Integer.toString(currentpage));
            return ok(myWorkspaces.render(list,hm,pagesCreated,pagesMonitored));
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }

    /**
     * Gives the creator status to another user. The previous creator is given all rights in this workspace
     * @param id_workspace The workspace that needs its creator changed
     * @param id_user The new creator
     * @return The management page, or home if errors
     */
    @Security.Authenticated(Secured.class)
    public static Result handOverCreation(String id_workspace, String id_user) {
        try{
            String id_logged = session("id_user");
            if(Workspace.isCreator(MC, id_workspace, id_logged)){
                //Giving creator status
                String[] attributes = {"id_workspace"};
                String[] values = {id_workspace};
                Workspace wor = Workspace.page(MC, attributes,values).get(0);
                Workspace updated = new Workspace(id_workspace, id_user, wor.getName(), wor.getAll_read(), wor.getAll_write(), wor.getAll_manage(), wor.getDescription());
                updated.update(MC);
                //Making the previous creator as monitoring with all rights (if already monitoring,just change the rights
                UserWorkspace usewor = new UserWorkspace(id_logged, id_workspace, "1", "1", "1");
                if(UserWorkspace.isMonitoring(MC, id_workspace, id_logged)){
                    usewor.update(MC);
                }else{
                    usewor.save(MC);
                }
                return manageAWorkspace(id_workspace);
            }else{flash("error", Message.ERROR_UNAUTHORIZED_WORKSPACE);}
        } catch (SQLException e) {
            flash("error", Message.ERROR_GENERAL);
        }
        return General.home();
    }
}