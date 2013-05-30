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
import org.junit.*;
import org.orbisgis.server.mapcatalog.*;
import java.sql.*;
import java.util.ArrayList;

/**
 * Test class for org.orbisgis.server.mapcatalog.MapCatalog
 * @author Mario Jothy
 */
public class MapCatalogTest {


    @Test
    public void workspaceCreation () throws SQLException{

        //Creation of the workspace
        Workspace wor = new Workspace(null, "bbb", "0");
        Long id_workspace = wor.save();
        String query = "SELECT * FROM workspace WHERE id_workspace=" + id_workspace;
        ArrayList<String[]> value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value.get(0)[0].equals(id_workspace.toString()) &&
                        value.get(0)[1] == null &&
                        value.get(0)[2].equals("bbb") &&
                        value.get(0)[3].equals("0")
        );
        //Deletion of the workspace
        Workspace.delete(id_workspace);
        query = "SELECT name FROM workspace WHERE id_workspace="+id_workspace;
        value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value.isEmpty()
        );
    }

    @Test
    public void folderCreation () throws SQLException{
        //Creation of the folder
        Folder fol = new Folder("2", null, "aaa");
        Long id_folder = fol.save();
        String query = "SELECT * FROM folder WHERE id_folder=" + id_folder;
        ArrayList<String[]> value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value.get(0)[0].equals(id_folder.toString())    &&
                        value.get(0)[1].equals("2")    &&
                        value.get(0)[2] == null        &&
                        value.get(0)[3].equals("aaa")
        );
        //Deletion of the folder
        Folder.delete(id_folder);
        query = "SELECT name FROM folder WHERE id_folder="+id_folder;
        value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value.isEmpty()
        );
    }

    @Test
    public void userCreation() throws SQLException{
        //Creation of the user
        User use = new User("moi", "moi@moi.moi", "aaa", "paris");
        Long id_user = use.save();
        String query = "SELECT * FROM user WHERE id_user=" + id_user;
        ArrayList<String[]> value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value.get(0)[0].equals(id_user.toString())           &&
                        value.get(0)[1].equals("moi")           &&
                        value.get(0)[2].equals("moi@moi.moi")   &&
                        value.get(0)[3].equals("aaa")           &&
                        value.get(0)[5].equals("paris")
        );
        //Deletion of the user
        User.delete(id_user);
        query = "SELECT name FROM user WHERE id_user="+id_user;
        value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                value.isEmpty()
        );
    }

    @Test
    public void commentCreation() throws SQLException{
        //Creation of the comment
        Comment com = new Comment(null, null, "a content", "default");
        Long id_comment = com.save();
        String query = "SELECT * FROM comment WHERE id_comment=" + id_comment;
        ArrayList<String[]> value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value.get(0)[0].equals(id_comment.toString())               &&
                        value.get(0)[1]==null               &&
                        value.get(0)[2]==null               &&
                        value.get(0)[3].equals("a content") &&
                        value.get(0)[4].equals("default")
        );
        //Deletion of the comment
        Comment.delete(id_comment);
        query = "SELECT content FROM comment WHERE id_comment="+id_comment;
        value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                value.isEmpty()
        );
    }


    @Test
    public void owsCreation() throws SQLException{
        //Creation of the ows
        OWSContext ows = new OWSContext("2", null, null, "a content", "title");
        Long id_owscontext = ows.save();
        String query = "SELECT * FROM owscontext WHERE id_owscontext=" + id_owscontext;
        ArrayList<String[]> value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value.get(0)[0].equals(id_owscontext.toString())     &&
                        value.get(0)[1].equals("2")     &&
                        value.get(0)[2]==null           &&
                        value.get(0)[3]==null           &&
                        value.get(0)[4].equals("a content") &&
                        value.get(0)[5].equals("title")
        );
        //Deletion of the owscontext
        OWSContext.delete(id_owscontext);
        query = "SELECT content FROM owscontext WHERE id_owscontext="+id_owscontext;
        value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                value.isEmpty()
        );
    }

}
