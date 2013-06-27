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
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The class of the workspace model representation
 * @author Mario Jothy
 */
public class Workspace {
    private String id_workspace = null;
    private String id_creator = null;
    private String name = "default";
    private String isPublic = "0"; // 0 or 1
    private String description = "";

    /**
     * Constructor of the workspace
     * @param id_creator
     * @param name
     * @param aPublic
     * @param description
     */
    public Workspace(String id_creator, String name, String aPublic, String description) {
        this.id_creator = id_creator;
        this.name = name;
        this.isPublic = aPublic;
        this.description = description;
    }

    /**
     * Constructor with primary key
     * @param id_workspace
     * @param id_creator
     * @param name
     * @param aPublic
     * @param description
     */
    public Workspace(String id_workspace, String id_creator, String name, String aPublic, String description) {
        this.id_workspace = id_workspace;
        this.id_creator = id_creator;
        this.name = name;
        this.isPublic = aPublic;
        this.description = description;
    }



    public String getId_workspace() {
        return id_workspace;
    }

    public String getId_creator() {
        return id_creator;
    }

    public String getName() {
        return name;
    }

    public String getPublic() {
        return isPublic;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Method that saves a instantiated workspace into database. Handles SQL injections.
     * @param MC the mapcatalog object for the connection
     * @return The ID of the workspace just created (primary key)
     */
    public  Long save(MapCatalog MC) {
        Long last = null;
        try{
            String query = "INSERT INTO workspace (id_creator,name,isPublic,description) VALUES (? , ? , ? , ?);";
            PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, id_creator);
            pstmt.setString(2, name);
            pstmt.setString(3, isPublic);
            pstmt.setString(4, description);
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
     * @param MC the mapcatalog object for the connection
     * @param id_workspace The primary key of the workspace
     */
    public static void delete(MapCatalog MC, Long id_workspace) {
        String query = "DELETE FROM workspace WHERE id_workspace = ? ;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_workspace);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that queries the database for workspaces, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of Workspace containing the result of the query
     */
    public static List<Workspace> page(MapCatalog MC, String[] attributes, String[] values){
        String query = "SELECT * FROM workspace WHERE ";
        List<Workspace> paged = new LinkedList<Workspace>();
        try {
            //case argument invalid
            if(attributes == null || values == null){
                throw new IllegalArgumentException("Arguments cannot be null");
            }
            if(attributes.length != values.length){
                throw new IllegalArgumentException("String arrays have to be of the same length");
            }
            //preparation of the query
            query+=attributes[0]+" = ?";
            for(int i=1; i<attributes.length; i++){
                if(values[i]==null){
                    query += "AND "+attributes[i]+" IS NULL";
                }else{
                    query += " AND "+attributes[i]+" = ?";
                }
            }
            //preparation of the statement
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            int j=1;
            for(int i=0; i<values.length; i++){
                if(values[i]!=null){
                    stmt.setString(j, values[i]);
                    j++;
                }
            }
            //Retrieving values
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String id_workspace = rs.getString("id_workspace");
                String id_creator = rs.getString("id_creator");
                String name = rs.getString("name");
                String isPublic = rs.getString("isPublic");
                String description = rs.getString("description");
                Workspace wor = new Workspace(id_workspace,id_creator,name,isPublic,description);
                paged.add(wor);
            }
            rs.close();
        }
        catch (SQLException e) {e.printStackTrace();}
        return paged;
    }

    /**
     * Method that sends a query to database SELECT * FROM Workspace
     * @param MC the mapcatalog object for the connection
     * @return A list of Workspace containing the result of the query
     */
    public static List<Workspace> page(MapCatalog MC){
        String query = "SELECT * FROM workspace";
        List<Workspace> paged = new LinkedList<Workspace>();
        try {
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String id_workspace = rs.getString("id_workspace");
                String id_creator = rs.getString("id_creator");
                String name = rs.getString("name");
                String isPublic = rs.getString("isPublic");
                String description = rs.getString("description");
                Workspace wor = new Workspace(id_workspace,id_creator,name,isPublic,description);
                paged.add(wor);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paged;
    }
}
