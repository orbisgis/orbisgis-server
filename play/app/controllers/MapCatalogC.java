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
import play.*;
import play.data.*;
import play.mvc.*;
import views.html.*;
import org.orbisgis.server.mapcatalog.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
        List<Workspace> list = Workspace.page(MC);
        return ok(mapCatalog.render(list));
    }

    /**
     * Renders the MapCatalog MyWorkspace page
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result myWorkspaces(){
        String[] attributes = {"id_creator"};
        String id = session("id_user");
        String[] values = {id};
        List<Workspace> list = Workspace.page(MC, attributes,values);
        return ok(myWorkspaces.render(list, UserWorkspace.pageWithWorkspace(MC,id)));
    }

    /**
     * Renders the view that represent the inside of a workspace
     * @param id_workspace the id of the workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result viewWorkspace(String id_workspace){
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_workspace};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_user = session().get("id_user");
        if(wor.getAll_read()=="1" || Workspace.isCreator(MC,id_workspace,id_user) || UserWorkspace.hasReadRight(MC,id_workspace,id_user)){
            String[] attributes = {"id_root", "id_parent"};
            String[] values = {id_workspace, null};
            List<Folder> listF = Folder.page(MC,attributes,values);
            List<OWSContext> listC = OWSContext.page(MC, attributes, values);
            return ok(workspace.render(listF,listC,wor));
        }else{
            flash("error","You don't have the right to explore this workspace, monitor it to demand the rights");
            return index();
        }
    }

    /**
     * Renders the view that represents the inside of a folder
     * @param id_workspace the root of the folder
     * @param id_folder the id of the folder
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result viewFolder(String id_workspace, String id_folder){
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_workspace};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_user = session().get("id_user");
        if(wor.getAll_read()=="1" || Workspace.isCreator(MC,id_workspace,id_user) || UserWorkspace.hasReadRight(MC,id_workspace,id_user)){
            String[] attributes = {"id_parent"};
            String[] values = {id_folder};
            List<Folder> listF = Folder.page(MC,attributes,values);
            List<OWSContext> listC = OWSContext.page(MC, attributes, values);
            List<Folder> path = Folder.getPath(MC, id_folder);
            return ok(folder.render(listF,listC,path,wor));
        }else{
            flash("error","You don't have the right to explore this workspace, monitor it to demand the rights");
            return index();
        }
    }

    /**
     * Creates a workspace in the database from a form
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createWorkspace(){
        DynamicForm form = Form.form().bindFromRequest();
        String name = form.get("name");
        String all_read = (form.get("all_read")!=null) ? "1":"0";
        String all_write  = (form.get("all_write")!=null) ? "1":"0";
        String all_manage  = (form.get("all_manage")!=null) ? "1":"0";
        String description = form.get("description");
        Workspace work = new Workspace(session("id_user"),name,all_read,all_write,all_manage,description);
        Long id = work.save(MC);
        return viewWorkspace(id.toString());
    }

    /**
     * Create a folder in the database from a form when inside the Workspace.html page
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createFolderFromRoot(String id_root){
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_root};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_user = session().get("id_user");
        if(wor.getAll_write()=="1" || Workspace.isCreator(MC,id_root,id_user) || UserWorkspace.hasWriteRight(MC, id_root, id_user)){
            DynamicForm form = Form.form().bindFromRequest();
            String name = form.get("name");
            Folder fol = new Folder(id_root,null,name);
            Long id = fol.save(MC);
            return viewFolder(id_root, id.toString());
        }else{
            flash("error", "You don't have writing access in this workspace, monitor it to demand the rights");
            return index();
        }
    }

    /**
     * Create a folder in the database from a form when inside the Folder.html page
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createFolderFromParent(String id_root, String id_parent){
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_root};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_user = session().get("id_user");
        if(wor.getAll_write()=="1" || Workspace.isCreator(MC,id_root,id_user) || UserWorkspace.hasWriteRight(MC, id_root, id_user)){
            DynamicForm form = Form.form().bindFromRequest();
            String name = form.get("name");
            Folder fol = new Folder(id_root,id_parent,name);
            Long id = fol.save(MC);
            return viewFolder(id_root, id.toString());
        }else{
            flash("error", "You don't have writing access in this workspace, monitor it to demand the rights");
            return index();
        }
    }

    /**
     * allow user to monitor a workspace
     * @param id_workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result monitor(String id_workspace){
        String id_user = session().get("id_user");
        if(UserWorkspace.isMonitoring(MC, id_workspace,id_user) || Workspace.isCreator(MC, id_workspace,id_user)){
            flash("error", "You are already monitoring this workspace!");
            return index();
        }else{
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,"0","0","0");
            usewor.save(MC);
            return noContent();
        }
    }

    /**
     * Renders the management page of workspaces
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result manageWorkspaces(){
        //List of workspaces created by the user
        String[] attributes = {"id_creator"};
        String[] values = {session().get("id_user")};
        List<Workspace> listCreated = Workspace.page(MC, attributes,values);
        //List of workspaces monitored and with right access
        //todo
        return ok(manageWorkspace.render(listCreated));
    }

    /**
     * Displays the management page for a particular workspace, checks if the sender of the http request has rights
     * @param id_workspace the id of the workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result manageAWorkspace(String id_workspace){
        //verification of rights
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_workspace};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_user = session().get("id_user");
        if(wor.getAll_manage()=="1" || UserWorkspace.hasManageRight(MC, id_workspace,id_user) || Workspace.isCreator(MC, id_workspace,id_user)){
            HashMap list = UserWorkspace.pageWithUser(MC,id_workspace);
            return ok(userManagement.render(list,wor));
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Changes the right of a user for a specific workspace, checks if the sender of the http request has rights
     * @param id_workspace
     * @param id_user
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result changeRights(String id_workspace, String id_user){
        //verification of rights
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_workspace};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_logged = session().get("id_user");
        if(wor.getAll_manage()=="1" || UserWorkspace.hasManageRight(MC, id_workspace,id_logged) || Workspace.isCreator(MC, id_workspace,id_logged)){
            DynamicForm form = Form.form().bindFromRequest();
            String read = form.get("Read");
            String write = form.get("Write");
            String manage = form.get("Manage");
            UserWorkspace uw = new UserWorkspace(id_user, id_workspace, read, write, manage);
            uw.update(MC);
            return manageAWorkspace(id_workspace);
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Delete a workspace, checks if the sender of the http request has rights
     * @param id_workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result deleteWorkspace(String id_workspace){
        //verification of rights
        String id_logged = session().get("id_user");
        if(Workspace.isCreator(MC, id_workspace, id_logged)){
            Workspace.delete(MC, Long.valueOf(id_workspace));
            return index();
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Update information about a workspace, checks if the sender of the http request has rights
     * @param id_workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result updateWorkspace(String id_workspace){
        //verification of rights
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_workspace};
        Workspace wor2 = Workspace.page(MC, attributes2, values2).get(0);
        String id_logged = session().get("id_user");
        if(wor2.getAll_manage()=="0" || UserWorkspace.hasManageRight(MC, id_workspace,id_logged) || Workspace.isCreator(MC, id_workspace,id_logged)){
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
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Casts out a user from a workspace, all_manage parameter is not taken into account here for access rights
     * @param id_workspace
     * @param id_user
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result castOut(String id_workspace, String id_user){
        //verification of rights
        String id_logged = session().get("id_user");
        if(UserWorkspace.hasManageRight(MC, id_workspace,id_logged) || Workspace.isCreator(MC, id_workspace,id_logged)){
            UserWorkspace.delete(MC, Long.valueOf(id_user), Long.valueOf(id_workspace));
            return manageAWorkspace(id_workspace);
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Deletes a folder from a workspace
     * @param id_root   The workspace parent
     * @param id_folder The folder to delete
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result deleteFolder(String id_root, String id_folder){
        //verification of rights
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_root};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_logged = session().get("id_user");
        if(wor.getAll_manage()=="1" || UserWorkspace.hasManageRight(MC, id_root,id_logged) || Workspace.isCreator(MC, id_root,id_logged)){
            Folder.delete(MC, Long.valueOf(id_folder));
            flash("info", "you successfully deleted the folder.");
            return viewWorkspace(id_root);
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Adds a map context in database with current workspace as root
     * @param id_root
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result addMapContextFromRoot(String id_root){
        //verification of rights
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_root};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_logged = session().get("id_user");
        if(wor.getAll_write()=="1" || UserWorkspace.hasWriteRight(MC, id_root, id_logged) || Workspace.isCreator(MC, id_root,id_logged)){
            MultipartFormData body = request().body().asMultipartFormData();
            FilePart file = body.getFile("mapcontext");
            if(file!=null){
                String title = file.getFilename();
                try{
                    FileInputStream context = new FileInputStream(file.getFile());
                    OWSContext ows = new OWSContext(id_root,null,id_logged,context,title);
                    ows.save(MC);
                }catch(FileNotFoundException e){
                    e.printStackTrace(); //unreachable code
                }
                flash("info", "you successfully added a context");
                return viewWorkspace(id_root);
            }else{
                flash("error","The file is missing");
                return viewWorkspace(id_root);
            }
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Adds a map context with current folder as parent
     * @param id_root
     * @param id_parent
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result addMapContextFromParent(String id_root, String id_parent){
        //verification of rights
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_root};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_logged = session().get("id_user");
        if(wor.getAll_write()=="1" || UserWorkspace.hasWriteRight(MC, id_root, id_logged) || Workspace.isCreator(MC, id_root,id_logged)){
            MultipartFormData body = request().body().asMultipartFormData();
            FilePart file = body.getFile("mapcontext");
            if(file!=null){
                String title = file.getFilename();
                try{
                    FileInputStream context = new FileInputStream(file.getFile());
                    OWSContext ows = new OWSContext(id_root,id_parent,id_logged,context,title);
                    ows.save(MC);
                }catch(FileNotFoundException e){
                    e.printStackTrace(); //unreachable code
                }
                flash("info", "you successfully added a context");
                return viewWorkspace(id_root);
            }else{
                flash("error","The file is missing");
                return viewWorkspace(id_root);
            }
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Search and return a specific list of workspaces
     * @return
     */
    public static Result searchPublicWorkspaces(){
        DynamicForm form = Form.form().bindFromRequest();
        String search = form.get("search");
        List<Workspace> list = Workspace.search(MC,search);
        return ok(mapCatalog.render(list));
    }

    /**
     * Search and return a specific list of folders and ows from the root of the worksapce
     * @param id_root
     * @return
     */
    public static Result searchFromRoot(String id_root){
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_root};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_user = session().get("id_user");
        if(wor.getAll_read()=="1" || Workspace.isCreator(MC,id_root,id_user) || UserWorkspace.hasReadRight(MC,id_root,id_user)){
            DynamicForm form = Form.form().bindFromRequest();
            String search = form.get("search");
            List<Folder> listF = Folder.search(MC,id_root,search);
            List<OWSContext> listC = OWSContext.search(MC,id_root,search);
            return ok(workspace.render(listF,listC,wor));
        }else{
            flash("error","You don't have the right to explore this workspace, monitor it to demand the rights");
            return index();
        }
    }

    /**
     * Search and return a specific list of folders and ows from the root of the worksapce
     * @param id_root
     * @param id_folder
     * @return
     */
    public static Result searchFromParent(String id_root, String id_folder){
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_root};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        String id_user = session().get("id_user");
        if(wor.getAll_read()=="1" || Workspace.isCreator(MC,id_root,id_user) || UserWorkspace.hasReadRight(MC,id_root,id_user)){
            DynamicForm form = Form.form().bindFromRequest();
            String search = form.get("search");
            List<Folder> listF = Folder.search(MC,id_root,search);
            List<OWSContext> listC = OWSContext.search(MC,id_root,search);
            List<Folder> path = Folder.getPath(MC, id_folder);
            return ok(folder.render(listF,listC,path,wor));
        }else{
            flash("error","You don't have the right to explore this workspace, monitor it to demand the rights");
            return index();
        }
    }
}