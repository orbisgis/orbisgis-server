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

import java.sql.*;

/**
 * The class of the folder model representation
 * @author Mario Jothy
 */
public class Folder {
    private static MapCatalog MC = new MapCatalog();
    private String id_root = "0";
    private String id_parent = null;
    private String name =  "default";

    /**
     * The constructor of the org.orbisgis.server.mapcatalog.Folder
     * @param id_root
     * @param id_parent Null if there is no parent folder (note: A workspace is NOT a folder)
     * @param name
     */
    public Folder(String id_root, String id_parent, String name) {
        this.id_root = id_root;
        this.id_parent = id_parent;
        this.name = name;
    }

    /**
     * Method that saves a instantiated folder into database. Handles SQL injections.
     * @return The ID of the folder just created (primary key)
     */
    public  Long save() {
        Long last = null;
        try{
            String query = "INSERT INTO folder (id_root,id_parent,name) VALUES (? , ? , ?);";
            PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, id_root);
            pstmt.setString(2, id_parent);
            pstmt.setString(3, name);
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
     * Deletes a folder from database
     * @param id_folder The primary key of the folder
     */
    public static void delete(Long id_folder) {
        String query = "DELETE FROM folder WHERE id_folder = ? ;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_folder);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
