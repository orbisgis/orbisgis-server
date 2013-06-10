package org.orbisgis.server.mapcatalog; /**
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
import java.sql.*;

/**
 * Java model of the table OWSContext
 * @author Mario Jothy
 */
public class OWSContext {
    private static MapCatalog MC = new MapCatalog();
    private String id_root = null;
    private String id_parent = null;
    private String id_uploader = null;
    private String content = "";
    private String title = "default";

    public OWSContext(String id_root, String id_parent, String id_uploader, String content, String title) {
        this.id_root = id_root;
        this.id_parent = id_parent;
        this.id_uploader = id_uploader;
        this.content = content;
        this.title = title;
    }

    /**
     * Method that saves a instantiated OWSContext into database. Handles SQL injections.
     * @return The ID of the OWSContext just created (primary key)
     */
    public  Long save() {
        Long last = null;
        try{
            String query = "INSERT INTO owscontext (id_root,id_parent,id_uploader,content, title) VALUES (? , ? , ? , ? , ?);";
            PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, id_root);
            pstmt.setString(2, id_parent);
            pstmt.setString(3, id_uploader);
            pstmt.setString(4, content);
            pstmt.setString(5, title);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next()){
                last = rs.getLong(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return last;
    }

    /**
     * Deletes a owscontext from database
     * @param id_owscontext The primary key of the owscontext
     */
    public static void delete(Long id_owscontext) {
        String query = "DELETE FROM owscontext WHERE id_owscontext = ? ;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_owscontext);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
