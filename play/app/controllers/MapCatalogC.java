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
import java.util.List;

import csp.ContentSecurityPolicy;

@ContentSecurityPolicy
public class MapCatalogC extends Controller{

    private static MapCatalog MC = Global.mc();

    public static MapCatalog getMapCatalog(){
        return MC;
    }

    public static Result index() {
        String[] attributes = {"isPublic"};
        String[] values = {"1"};
        List<Workspace> list = Workspace.page(MC, attributes,values);
        return ok(mapCatalog.render(list));
    }

    @Security.Authenticated(Secured.class)
    public static Result myWorkspaces(){
        String[] attributes = {"isPublic","id_creator"};
        String id = session("id_user");
        String[] values = {"0",id};
        List<Workspace> list = Workspace.page(MC, attributes,values);
        flash("section","private");
        return ok(mapCatalog.render(list));
    }



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

    @Security.Authenticated(Secured.class)
    public static Result createWorkspace(){
        DynamicForm form = Form.form().bindFromRequest();
        String name = form.get("name");
        String isPublic ;
        if(form.get("public").equals("true")){
            isPublic="1";
        }else{isPublic="0";}
        Workspace work = new Workspace(session("id_user"),name,isPublic,"");
        Long id = work.save(MC);
        System.out.println("WORKSPACE CREATED");
        return viewWorkspace(id.toString());
    }

    @Security.Authenticated(Secured.class)
    public static Result createFolderFromRoot(){
        DynamicForm form = Form.form().bindFromRequest();
        String name = form.get("name");
        String id_root = form.get("id_root");
        Folder fol = new Folder(id_root,null,name);
        Long id = fol.save(MC);
        System.out.println("FOLDER CREATED");
        return viewFolder(id_root, id.toString());
    }

    @Security.Authenticated(Secured.class)
    public static Result createFolderFromParent(){
        DynamicForm form = Form.form().bindFromRequest();
        String name = form.get("name");
        String id_root = form.get("id_root");
        String id_parent = form.get("id_parent");
        Folder fol = new Folder(id_root,id_parent,name);
        Long id = fol.save(MC);
        System.out.println("FOLDER CREATED");
        return viewFolder(id_root, id.toString());
    }

    //@Security.Authenticated(Secured.class)
    //public static Result monitor(String id_workspace){

    //    return index();
    //}
}
