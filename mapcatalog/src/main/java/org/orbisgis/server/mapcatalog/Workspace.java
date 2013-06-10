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
import java.util.ArrayList;

/**
 * The class of the workspace model representation
 * @author Mario Jothy
 */
public class Workspace {
    private static MapCatalog MC = new MapCatalog();
    private String id_creator = null;
    private String name = "default";
    private String isPublic = "0"; // 0 or 1

    /**
     * Constructor of the workspace
     * @param id_creator
     * @param name
     * @param aPublic
     */
    public Workspace(String id_creator, String name, String aPublic) {
        this.id_creator = id_creator;
        this.name = name;
        this.isPublic = aPublic;
    }

    /**
     * Method that saves a instantiated workspace into database. Handles SQL injections.
     * @return The ID of the workspace just created (primary key)
     */
    public  Long save() {
        Long last = null;
        try{
            String query = "INSERT INTO workspace (id_creator,name,isPublic) VALUES (? , ? , ? );";
            PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, id_creator);
            pstmt.setString(2, name);
            pstmt.setString(3, isPublic);
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
     * Deletes a workspace from database
     * @param id_workspace The primary key of the workspace
     */
    public static void delete(Long id_workspace) {
        String query = "DELETE FROM workspace WHERE id_workspace = ? ;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_workspace);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
