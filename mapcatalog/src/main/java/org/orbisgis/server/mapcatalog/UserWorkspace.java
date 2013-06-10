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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Java model of the table UserWorkspace
 */
public class UserWorkspace {
    private static MapCatalog MC = new MapCatalog();
    private String id_user;
    private String id_workspace;
    private String read = "0";
    private String write = "0";
    private String manageUser = "0";

    /**
     * Constructor
     * @param id_user
     * @param id_workspace
     * @param read
     * @param write
     * @param manageUser
     */
    public UserWorkspace(String id_user, String id_workspace, String read, String write, String manageUser) {
        this.id_user = id_user;
        this.id_workspace = id_workspace;
        this.read = read;
        this.write = write;
        this.manageUser = manageUser;
    }

    /**
     * Method that saves a instantiated User_Workspace relation into database. Handles SQL injections.
     * @return The ID of the User just created (primary key)
     */
    public  Long save() {
        Long last = null;
        try{
            String query = "INSERT INTO user_workspace (id_user,id_workspace,read,write,manage_user) VALUES (? , ? , ? , ? , ?);";
            PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, id_user);
            pstmt.setString(2, id_workspace);
            pstmt.setString(3, read);
            pstmt.setString(4, write);
            pstmt.setString(4, manageUser);
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
     * Deletes a user_workspace relation from database
     * @param id_user The primary key of the user
     * @param id_workspace The primary key of the workspace
     */
    public static void delete(Long id_user, Long id_workspace) {
        String query = "DELETE FROM user_workspace WHERE id_user = ? AND id_workspace = ?;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_user);
            stmt.setLong(1, id_workspace);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}