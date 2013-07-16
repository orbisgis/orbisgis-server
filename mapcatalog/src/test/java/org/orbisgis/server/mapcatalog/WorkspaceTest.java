package org.orbisgis.server.mapcatalog;

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

import org.junit.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class WorkspaceTest {
    private static MapCatalog MC;

    @BeforeClass
    public static void init() throws SQLException{
        MC = new MapCatalog(MapCatalog.URL_TEST, MapCatalog.USER_TEST, MapCatalog.PASSWORD_TEST);
        MC.executeSQL("down.sql");
        MC.executeSQL("ups.sql");
        MC.executeSQL("populate.sql");
    }

    @Test
    public void creation() throws SQLException {

        //Creation of the workspace
        Workspace wor = new Workspace("1","name","0","0","0","description");
        Long id_workspace = wor.save(MC);
        //Obtaining of the workspace
        String[] attributes = {"id_workspace"};
        String[] values = {id_workspace.toString()};
        List<Workspace> list= Workspace.page(MC, attributes,values);
        Assert.assertTrue(
                        list.get(0).getId_creator().equals("1")
                &&      list.get(0).getName().equals("name")
                &&      list.get(0).getAll_read().equals("0")
                &&      list.get(0).getAll_write().equals("0")
                &&      list.get(0).getAll_manage().equals("0")
                &&      list.get(0).getDescription().equals("description")
        );
    }

    @Test
    public void creator() throws SQLException {
        Assert.assertTrue(Workspace.isCreator(MC, "1", "1"));
    }

    @Test
    public void update() throws SQLException {
        Workspace wor = new Workspace("1","1", "name", "1","1","1","description2");
        wor.update(MC);
        //Obtaining of the workspace
        String[] attributes = {"id_workspace"};
        String[] values = {"1"};
        List<Workspace> list= Workspace.page(MC, attributes,values);
        Assert.assertTrue(
                                list.get(0).getId_creator().equals("1")
                        &&      list.get(0).getName().equals("name")
                        &&      list.get(0).getAll_read().equals("1")
                        &&      list.get(0).getAll_write().equals("1")
                        &&      list.get(0).getAll_manage().equals("1")
                        &&      list.get(0).getDescription().equals("description2")
        );
    }

    @Test
    public void deletion() throws SQLException {
        Workspace.delete(MC,Long.valueOf("3"));
        String[] attributes = {"id_workspace"};
        String[] values = {"3"};
        List<Workspace> list= Workspace.page(MC, attributes,values);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void searching() throws SQLException {
        List<Workspace> list = Workspace.search(MC, "namesrc");
        Assert.assertTrue(
                                list.get(0).getId_creator().equals("1")
                        &&      list.get(0).getName().equals("namesrc")
                        &&      list.get(0).getAll_read().equals("1")
                        &&      list.get(0).getAll_write().equals("1")
                        &&      list.get(0).getAll_manage().equals("1")
                        &&      list.get(0).getDescription().equals("description")
        );
    }

    @Test
    public void searchingCreated() throws SQLException {
        List<Workspace> list = Workspace.searchMyWorkspacesCreated(MC, "namesrc", "1");
        Assert.assertTrue(
                                list.get(0).getId_creator().equals("1")
                        &&      list.get(0).getName().equals("namesrc")
                        &&      list.get(0).getAll_read().equals("1")
                        &&      list.get(0).getAll_write().equals("1")
                        &&      list.get(0).getAll_manage().equals("1")
                        &&      list.get(0).getDescription().equals("description")
        );
    }

    @AfterClass
    public static void end() throws SQLException{
        MC.executeSQL("down.sql");
    }
}
