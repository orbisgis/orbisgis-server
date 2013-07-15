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

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class UserTest {
    private static MapCatalog MC;

    @BeforeClass
    public static void init() throws SQLException {
        MC = new MapCatalog("jdbc:h2:./target/testdb","sa","");
        MC.executeSQL("down.sql");
        MC.executeSQL("ups.sql");
        MC.executeSQL("populate.sql");
    }

    @Test
    public void creation() throws SQLException, NoSuchAlgorithmException {

        //Creation of the user
        User use = new User("name","mail","123456","location");
        Long id_user = use.save(MC);
        //Obtaining of the user
        String[] attributes = {"id_user"};
        String[] values = {id_user.toString()};
        List<User> list= User.page(MC, attributes,values);
        Assert.assertTrue(
                        list.get(0).getName().equals("name")
                        && list.get(0).getEmail().equals("mail")
                        && list.get(0).getPassword().equals(MapCatalog.hasher("123456"))
                        && list.get(0).getLocation().equals("location")
        );
    }

    @Test
    public void update() throws SQLException, NoSuchAlgorithmException {
        User use = new User("1","name","mail","","location2","profession","additionnal");
        use.update(MC);
        //Obtaining of the user
        String[] attributes = {"id_user"};
        String[] values = {"1"};
        List<User> list= User.page(MC, attributes,values);
        Assert.assertTrue(
                        list.get(0).getName().equals("name")
                        && list.get(0).getEmail().equals("mail")
                        && list.get(0).getLocation().equals("location2")
        );
    }

    @Test
    public void deletion() throws SQLException {
        User.delete(MC,Long.valueOf("1"));
        String[] attributes = {"id_user"};
        String[] values = {"1"};
        List<User> list= User.page(MC, attributes,values);
        Assert.assertTrue(list.isEmpty());
    }

    @AfterClass
    public static void end() throws SQLException{
        MC.executeSQL("down.sql");
    }
}
