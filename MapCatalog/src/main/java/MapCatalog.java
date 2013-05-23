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

import java.sql.*;

/**
 * Manages workspaces and map contexts
 * @author Mario Jothy
 */
public class MapCatalog {

    private static final String DRIVER_NAME = "org.h2.Driver";

    static
    {
        try
        {
            Class.forName(DRIVER_NAME).newInstance();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static final String URL = "jdbc:h2:~/test";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    /**
     * Getter for the connection to database
     * @return The connection
     * @throws SQLException if the connection is invalid
     */
    public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Executes a DELETE or INSERT query in database
     * @param con   The connection to the database
     * @param query The query that you wish to execute
     * @return  In case of an Insert, returns the primarykey id of the inserted element, else returns null
     */
    public static Long executeSQLupdate(Connection con, String query) {
        Long lastId = null;
        Statement stmt;
        try{
            stmt = con.createStatement();
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            //case INSERT, we store the ID of the inserted object
            ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next()) {
                lastId = rs.getLong(1);
            }
            rs.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastId;
    }

    /**
     * Executes a SELECT values FROM table WHERE conditions query into database
     * @param con   The connection to the database
     * @param query The query you wish to execute
     * @return  The Selected data in a string array
     */
    public static String[] executeSQLselect(Connection con, String query) {
        Statement stmt;
        int indexbegin = query.lastIndexOf("SELECT")+6;
        int indexend = query.indexOf("FROM")-1;
        String[] collumns;
        collumns = query.substring(indexbegin, indexend).split(",");
        String[] value = new String[collumns.length];
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                for (int i=0; i<collumns.length; i++){
                    String temp = rs.getString(collumns[i].trim());
                    value[i] = temp;
                }
            }
            rs.close();
            con.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * This method takes an object and turn it into a valid SQL column value
     * @param value The objects to refactor
     * @return The toString value of the argument surrounded by ' ' if not null, else the string null
     */
    public static String refactorToSQL(Object value) {
        String refactored = new String();
        if(!(value == null)) {
            refactored = "\'"+value.toString()+"\'";
        } else {
            refactored = "null";
        }
        return refactored;
    }


    /**
     * Creates a workspace and saves it into database with the right connection
     * @param id_creator The id of the creator (user)
     * @param name       The Name of the workspace
     * @param isPublic   The visibility of the workspace (0 or 1)
     * @return The id_workspace of the workspace created (primary key)
     */
    public static Long createWorkspace(Long id_creator, String name, int isPublic) {
        Long last = null;
        Workspace wor = new Workspace(id_creator, name, isPublic);
        try{
            last = wor.saveWorkspace(getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return last;
    }

    /**
     * Creates a Folder and saves it into  database with right connection
     * @param id_root   The id of the root workspace
     * @param id_parent The id of the parent folder, null if there is none
     * @param name      The name of the folder
     * @return The id_folder of the folder created (primary key)
     */
    public static Long createFolder(Long id_root, Long id_parent, String name) {
        Long last = null;
        Folder fol = new Folder(id_root, id_parent, name);
        try{
            last = fol.saveFolder(getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return last;
    }

    /**
     * Delete a workspace from database
     * @param id_workspace the id of the workspace in database
     */
    public static void deleteWorkspace(Long id_workspace) {
        String query = "DELETE FROM workspace " +
                        "WHERE id_workspace = " + id_workspace +";";
        try{
            executeSQLupdate(getConnection(), query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Delete a folder from database
     * @param id_folder
     */
    public static void deleteFolder(Long id_folder) {
        String query = "DELETE FROM folder " +
                "WHERE id_folder = " + id_folder +";";
        try{
            executeSQLupdate(getConnection(), query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
