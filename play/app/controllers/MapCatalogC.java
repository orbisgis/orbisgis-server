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

import java.util.HashMap;
import java.util.List;

import csp.ContentSecurityPolicy;

@ContentSecurityPolicy
public class MapCatalogC extends Controller{

    private static MapCatalog MC = Global.mc();

    /**
     * Verify if a user is creator of the workspace
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     * @return
     */
    public static boolean isCreator(String id_workspace, String id_user){
        String[] attributes = {"id_creator","id_workspace"};
        String[] values = {id_user, id_workspace};
        List<Workspace> workspaceList = Workspace.page(MC, attributes, values);
        if(!workspaceList.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Verify if a user is following the workspace
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     * @return
     */
    public static boolean isMonitoring(String id_workspace, String id_user){
        String[] attributes = {"id_user","id_workspace"};
        String[] values = {id_user, id_workspace};
        List<UserWorkspace> useworList = UserWorkspace.page(MC, attributes, values);
        if(!useworList.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Verify if a user has read right
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     * @return
     */
    public static boolean hasReadRight(String id_workspace, String id_user){
        String[] attributes = {"id_user","id_workspace","READ"};
        String[] values = {id_user, id_workspace,"1"};
        List<UserWorkspace> useworList = UserWorkspace.page(MC, attributes, values);
        if(!useworList.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Verify if a user has read right
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     * @return
     */
    public static boolean hasWriteRight(String id_workspace, String id_user){
        String[] attributes = {"id_user","id_workspace","WRITE"};
        String[] values = {id_user, id_workspace,"1"};
        List<UserWorkspace> useworList = UserWorkspace.page(MC, attributes, values);
        if(!useworList.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Verify if a user has management right
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     * @return
     */
    public static boolean hasManageRight(String id_workspace, String id_user){
        String[] attributes = {"id_user","id_workspace","MANAGE_USER"};
        String[] values = {id_user, id_workspace,"1"};
        List<UserWorkspace> useworList = UserWorkspace.page(MC, attributes, values);
        if(!useworList.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    public static MapCatalog getMapCatalog(){
        return MC;
    }

    /**
     * Renders the MapCatalog Public page
     * @return
     */
    public static Result index() {
        String[] attributes = {"isPublic"};
        String[] values = {"1"};
        List<Workspace> list = Workspace.page(MC, attributes,values);
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
        flash("section","private");
        return ok(mapCatalog.render(list));
    }

    /**
     * Renders the view that represent the inside of a workspace
     * @param id_workspace the id of the workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result viewWorkspace(String id_workspace){
        String[] attributes = {"id_root", "id_parent"};
        String[] values = {id_workspace, null};
        List<Folder> listF = Folder.page(MC,attributes,values);
        List<OWSContext> listC = OWSContext.page(MC, attributes, values);
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_workspace};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        return ok(workspace.render(listF,listC,wor));
    }

    /**
     * Renders theview that represents the inside of a folder
     * @param id_workspace the root of the folder
     * @param id_folder the id of the folder
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result viewFolder(String id_workspace, String id_folder){
        String[] attributes = {"id_parent"};
        String[] values = {id_folder};
        List<Folder> listF = Folder.page(MC,attributes,values);
        List<OWSContext> listC = OWSContext.page(MC, attributes, values);
        List<Folder> path = Folder.getPath(MC, id_folder);
        String[] attributes2 = {"id_workspace"};
        String[] values2 = {id_workspace};
        Workspace wor = Workspace.page(MC, attributes2, values2).get(0);
        return ok(folder.render(listF,listC,path,wor));
    }

    /**
     * Creates a workspace in the database from a form
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createWorkspace(){
        DynamicForm form = Form.form().bindFromRequest();
        String name = form.get("name");
        String isPublic  =form.get("public");
        String description = form.get("description");
        Workspace work = new Workspace(session("id_user"),name,isPublic,description);
        Long id = work.save(MC);
        return viewWorkspace(id.toString());
    }

    /**
     * Create a folder in the database from a form when inside the Workspace.html page
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createFolderFromRoot(){
        DynamicForm form = Form.form().bindFromRequest();
        String name = form.get("name");
        String id_root = form.get("id_root");
        Folder fol = new Folder(id_root,null,name);
        Long id = fol.save(MC);
        return viewFolder(id_root, id.toString());
    }

    /**
     * Create a folder in the database from a form when inside the Folder.html page
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result createFolderFromParent(){
        DynamicForm form = Form.form().bindFromRequest();
        String name = form.get("name");
        String id_root = form.get("id_root");
        String id_parent = form.get("id_parent");
        Folder fol = new Folder(id_root,id_parent,name);
        Long id = fol.save(MC);
        return viewFolder(id_root, id.toString());
    }

    /**
     * allow user to monitor a workspace
     * @param id_workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result monitor(String id_workspace){
        String id_user = session().get("id_user");
        if(isMonitoring(id_workspace,id_user) || isCreator(id_workspace,id_user)){
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
        String[] attributes = {"id_creator"};
        String[] values = {session().get("id_user")};
        List<Workspace> list = Workspace.page(MC, attributes,values);
        return ok(manageWorkspace.render(list));
    }

    /**
     * Displays the management page for a particular workspace, checks if the sender of the http request has rights
     * @param id_workspace the id of the workspace
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result manageAWorkspace(String id_workspace){
        //verification of rights
        String id_user = session().get("id_user");
        if(hasManageRight(id_workspace,id_user) || isCreator(id_workspace,id_user)){
            HashMap list = UserWorkspace.pageWithUser(MC,id_workspace);
            String[] attributes = {"id_workspace"};
            String[] values = {id_workspace};
            Workspace wor = Workspace.page(MC, attributes, values).get(0);
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
        String id_logged = session().get("id_user");
        if(hasManageRight(id_workspace,id_logged) || isCreator(id_workspace,id_logged)){
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
        if(isCreator(id_workspace, id_logged)){
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
        String id_logged = session().get("id_user");
        if(hasManageRight(id_workspace,id_logged) || isCreator(id_workspace,id_logged)){
            DynamicForm form = Form.form().bindFromRequest();
            String name = form.get("name");
            String isPublic = form.get("public");
            String description = form.get("description");
            String id_creator = form.get("id_creator");
            Workspace wor = new Workspace(id_workspace,id_creator,name,isPublic,description);
            wor.update(MC);
            return manageAWorkspace(id_workspace);
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    /**
     * Casts out a user from a workspace
     * @param id_workspace
     * @param id_user
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result castOut(String id_workspace, String id_user){
        //verification of rights
        String id_logged = session().get("id_user");
        if(hasManageRight(id_workspace,id_logged) || isCreator(id_workspace,id_logged)){
            UserWorkspace.delete(MC, Long.valueOf(id_user), Long.valueOf(id_workspace));
            return manageAWorkspace(id_workspace);
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteFolder(String id_root, String id_folder){
        //verification of rights
        String id_logged = session().get("id_user");
        if(hasManageRight(id_root,id_logged) || isCreator(id_root,id_logged)){
            Folder.delete(MC, Long.valueOf(id_folder));
            flash("info", "you successfully deleted the folder.");
            return viewWorkspace(id_root);
        }else{
            flash("error","You don't have the rights to do that");
            return forbidden(home.render());
        }
    }
}