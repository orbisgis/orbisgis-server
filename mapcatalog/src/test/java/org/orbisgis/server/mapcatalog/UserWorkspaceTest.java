package org.orbisgis.server.mapcatalog;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
public class UserWorkspaceTest {
    private static MapCatalog MC;

    @BeforeClass
    public static void init() throws SQLException {
        MC = new MapCatalog(MapCatalog.URL_TEST, MapCatalog.USER_TEST, MapCatalog.PASSWORD_TEST);
        MC.executeSQL("down.sql");
        MC.executeSQL("ups.sql");
        MC.executeSQL("populate.sql");
    }

    @Test
    public void creation() throws SQLException {

        //Creation of the userworkspace
        UserWorkspace usewor = new UserWorkspace("2","1","0","0","0");
        usewor.save(MC);
        //Obtaining of the userworkspace
        String[] attributes = {"id_user","id_workspace"};
        String[] values = {"2","1"};
        List<UserWorkspace> list= UserWorkspace.page(MC, attributes,values);
        Assert.assertTrue(
                        list.get(0).getRead().equals("0")
                        && list.get(0).getWrite().equals("0")
                        && list.get(0).getManageUser().equals("0")
        );
    }

    @Test
    public void update() throws SQLException {
        UserWorkspace usewor = new UserWorkspace("1","1","1","1","1");
        usewor.update(MC);
        //Obtaining of the userworkspace
        String[] attributes = {"id_user","id_workspace"};
        String[] values = {"1","1"};
        List<UserWorkspace> list= UserWorkspace.page(MC, attributes,values);
        Assert.assertTrue(
                        list.get(0).getRead().equals("1")
                        && list.get(0).getWrite().equals("1")
                        && list.get(0).getManageUser().equals("1")
        );
    }

    @Test
    public void deletion() throws SQLException {
        UserWorkspace.delete(MC,Long.valueOf("1"),Long.valueOf("1"));
        String[] attributes = {"id_user","id_workspace"};
        String[] values = {"1","1"};
        List<UserWorkspace> list= UserWorkspace.page(MC, attributes,values);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void rights() throws SQLException {
        Assert.assertTrue(
                UserWorkspace.isMonitoring(MC,"2","2")
                && UserWorkspace.hasReadRight(MC, "2", "2")
                && UserWorkspace.hasWriteRight(MC, "2", "2")
                && UserWorkspace.hasManageRight(MC, "2", "2")
        );
    }

    @AfterClass
    public static void end() throws SQLException{
        MC.executeSQL("down.sql");
    }
}
