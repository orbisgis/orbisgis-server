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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;


public class CommentTest {
    private static MapCatalog MC;

    public static String fromStream(InputStream in) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        return out.toString();
    }

    @BeforeClass
    public static void init() throws SQLException {
        MC = new MapCatalog(MapCatalog.URL_TEST, MapCatalog.USER_TEST, MapCatalog.PASSWORD_TEST);
        MC.executeSQL("down.sql");
        MC.executeSQL("ups.sql");
        MC.executeSQL("populate.sql");
    }

    @Test
    public void creation() throws SQLException, IOException {
        //Creation of the comment
        Comment com = new Comment("1","1","title");
        InputStream is = getClass().getResourceAsStream("comment.txt");
        Long id_comment = com.save(MC, is);
        //Obtaining of the ows
        String[] attributes = {"id_comment"};
        String[] values = {id_comment.toString()};
        List<Comment> list= Comment.page(MC, attributes,values);
        Assert.assertTrue(
                           list.get(0).getId_writer().equals("1")
                        && list.get(0).getId_map().equals("1")
                        && list.get(0).getTitle().equals("title")
                        && fromStream(list.get(0).getContent(MC)).equals(fromStream(getClass().getResourceAsStream("comment.txt")))
        );
    }

    @Test
    public void update() throws SQLException, IOException {
        Comment com = new Comment("1","1","1","title2",null);
        InputStream is = getClass().getResourceAsStream("commentUpdt.txt");
        com.update(MC, is);
        //Obtaining of the ows
        String[] attributes = {"id_comment"};
        String[] values = {"1"};
        List<Comment> list= com.page(MC, attributes,values);
        Assert.assertTrue(
                            list.get(0).getId_writer().equals("1")
                        && list.get(0).getId_map().equals("1")
                        && list.get(0).getTitle().equals("title2")
                        && fromStream(list.get(0).getContent(MC)).equals(fromStream(getClass().getResourceAsStream("commentUpdt.txt")))
        );
    }

    @Test
    public void deletion() throws SQLException {
        Comment.delete(MC,Long.valueOf("2"));
        String[] attributes = {"id_comment"};
        String[] values = {"2"};
        List<Comment> list= Comment.page(MC, attributes,values);
        Assert.assertTrue(list.isEmpty());
    }

    @AfterClass
    public static void end() throws SQLException{
        MC.executeSQL("down.sql");
    }
}
