package org.orbisgis.server.mapcatalog; /**
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

import org.junit.*;
import org.orbisgis.server.mapcatalog.*;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.List;

/**
 * Test class for org.orbisgis.server.mapcatalog.MapCatalog
 * @author Mario Jothy
 */
public class MapCatalogTest {
    private MapCatalog MC = new MapCatalog("jdbc:h2:./target/testdb","sa","");

    @Before
    public void init(){
        MC.executeSQL("ups.sql");
        MC.executeSQL("populate.sql");
    }

    @Test
    public void workspaceCreation () throws SQLException{

        //Creation of the workspace
        Workspace wor = new Workspace(null, "bbb", "0","description");
        Long id_workspace = wor.save(MC);
        String[] attributes = {"id_workspace"};
        String[] values = {id_workspace.toString()};
        List<Workspace> list= Workspace.page(MC, attributes,values);
        Assert.assertTrue(
                        list.get(0).getId_creator() == null
                &&      list.get(0).getName().equals("bbb")
                &&      list.get(0).getPublic().equals("0")
                &&      list.get(0).getDescription().equals("description")
        );
        //Deletion of the workspace
        Workspace.delete(MC, id_workspace);
        list= Workspace.page(MC, attributes,values);
        Assert.assertTrue(
                        list.isEmpty()
        );
    }

    @Test
    public void folderCreation () throws SQLException{

        //Creation of the folder
        Folder fol = new Folder("1", null, "bbb");
        Long id_folder = fol.save(MC);
        String[] attributes = {"id_folder"};
        String[] values = {id_folder.toString()};
        List<Folder> list= Folder.page(MC, attributes,values);
        Assert.assertTrue(
                                list.get(0).getId_root().equals("1")
                        &&      list.get(0).getId_parent()==null
                        &&      list.get(0).getName().equals("bbb")
        );
        //Deletion of the folder
        Folder.delete(MC, id_folder);
        list= Folder.page(MC, attributes,values);
        Assert.assertTrue(
                list.isEmpty()
        );
    }

    @Test
    public void userCreation () throws SQLException, NoSuchAlgorithmException {

        //Creation of the workspace
        User use = new User("name", "email", "pass" , "loc");
        Long id_user = use.save(MC);
        String[] attributes = {"id_user"};
        String[] values = {id_user.toString()};
        List<User> list= User.page(MC, attributes,values);
        Assert.assertTrue(
                                list.get(0).getName().equals("name")
                        &&      list.get(0).getEmail().equals("email")
                        &&      list.get(0).getPassword().equals(MapCatalog.hasher("pass"))
                        &&      list.get(0).getLocation().equals("loc")
        );
        //Deletion of the workspace
        User.delete(MC, id_user);
        list= User.page(MC, attributes,values);
        Assert.assertTrue(
                list.isEmpty()
        );
    }

    @Test
    public void owsCreation () throws SQLException{

        //Creation of the owscontext
        OWSContext ows = new OWSContext("1", null, null , "acontent", "title");
        Long id_owscontext = ows.save(MC);
        String[] attributes = {"id_owscontext"};
        String[] values = {id_owscontext.toString()};
        List<OWSContext> list= OWSContext.page(MC, attributes,values);
        Assert.assertTrue(
                                list.get(0).getId_root().equals("1")
                        &&      list.get(0).getId_parent()==null
                        &&      list.get(0).getId_uploader()==null
                        &&      list.get(0).getContent().equals("acontent")
                        &&      list.get(0).getTitle().equals("title")
        );
        //Deletion of the workspace
        OWSContext.delete(MC, id_owscontext);
        list= OWSContext.page(MC, attributes,values);
        Assert.assertTrue(
                list.isEmpty()
        );
    }

    @Test
    public void commentCreation () throws SQLException{

        //Creation of the comment
        Comment ows = new Comment(null, null , "acontent", "title");
        Long id_comment = ows.save(MC);
        String[] attributes = {"id_comment"};
        String[] values = {id_comment.toString()};
        List<Comment> list= Comment.page(MC, attributes,values);
        Assert.assertTrue(
                                list.get(0).getId_writer()==null
                        &&      list.get(0).getId_map()==null
                        &&      list.get(0).getContent().equals("acontent")
                        &&      list.get(0).getTitle().equals("title")
        );
        //Deletion of the workspace
        Comment.delete(MC, id_comment);
        list= Comment.page(MC, attributes,values);
        Assert.assertTrue(
                list.isEmpty()
        );
    }

    @After
    public void end(){
        MC.executeSQL("down.sql");
    }
}
