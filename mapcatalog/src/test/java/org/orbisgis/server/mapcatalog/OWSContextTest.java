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


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class OWSContextTest {
    private static MapCatalog MC;

    @BeforeClass
    public static void init() throws SQLException {
        MC = new MapCatalog("jdbc:h2:./target/testdb","sa","");
        MC.executeSQL("down.sql");
        MC.executeSQL("ups.sql");
        MC.executeSQL("populate.sql");
    }

    @Test
    public void creation() throws SQLException {

        //Creation of the ows
        OWSContext ows = new OWSContext("1",null,"1","title");
        InputStream is = getClass().getResourceAsStream("MaCarte.ows");
        Long id_owscontext = ows.save(MC, is);
        //Obtaining of the ows
        String[] attributes = {"id_owscontext"};
        String[] values = {id_owscontext.toString()};
        List<OWSContext> list= OWSContext.page(MC, attributes,values);
        Assert.assertTrue(
                           list.get(0).getId_root().equals("1")
                        && list.get(0).getId_parent() == null
                        && list.get(0).getId_uploader().equals("1")
                        && list.get(0).getTitle().equals("title")
        );
    }

    @Test
    public void update() throws SQLException {
        Folder fol = new Folder("1","1",null,"name2");
        fol.update(MC);
        //Obtaining of the ows
        String[] attributes = {"id_folder"};
        String[] values = {"1"};
        List<Folder> list= Folder.page(MC, attributes,values);
        Assert.assertTrue(
                list.get(0).getId_root().equals("1")
                        && list.get(0).getId_parent()==null
                        && list.get(0).getName().equals("name2")
        );
    }

    @Test
    public void deletion() throws SQLException {
        Folder.delete(MC,Long.valueOf("1"));
        String[] attributes = {"id_folder"};
        String[] values = {"1"};
        List<Folder> list= Folder.page(MC, attributes,values);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void searching() throws SQLException {
        List<Folder> list = Folder.search(MC, "1","namesrc");
        Assert.assertTrue(
                list.get(0).getId_root().equals("1")
                        && list.get(0).getId_parent()==null
                        && list.get(0).getName().equals("namesrc")
        );
    }

    @AfterClass
    public static void end() throws SQLException{
        MC.executeSQL("down.sql");
    }
}
