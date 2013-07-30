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
    private String all_read = "0"; // 0 or 1
    private String all_write = "0"; // 0 or 1
    private String all_manage = "0"; // 0 or 1
    private String description = "";

    /**
     * Constructor of workspace for an insert (without PK)
     * @param id_creator The creator of the workspace
     * @param name The name of the workspace
     * @param all_read The default read access
     * @param all_write The default write access
     * @param all_manage The default manage access
     * @param description The description of the workspace
     */
    public Workspace(String id_creator, String name, String all_read, String all_write, String all_manage, String description) {
        this.id_creator = id_creator;
        this.name = name;
        this.all_read = all_read;
        this.all_write = all_write;
        this.all_manage = all_manage;
        this.description = description;
    }

    /**
     * Constructor of workspace for a select (with PK)
     * @param id_workspace The id of the workspace
     * @param id_creator The id of the creator
     * @param name The name of the workspace
     * @param all_read The default read access
     * @param all_write The default write access
     * @param all_manage The default manage access
     * @param description The description of the workspace
     */
    public Workspace(String id_workspace, String id_creator, String name, String all_read, String all_write, String all_manage, String description) {
        this.id_workspace = id_workspace;
        this.id_creator = id_creator;
        this.name = name;
        this.all_read = all_read;
        this.all_write = all_write;
        this.all_manage = all_manage;
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

    public String getAll_read() {
        return all_read;
    }

    public String getAll_write() {
        return all_write;
    }

    public String getAll_manage() {
        return all_manage;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Method that saves a instantiated workspace into database. Handles SQL injections.
     * @param MC the mapcatalog object for the connection
     * @return The ID of the workspace just created (primary key)
     */
    public  Long save(MapCatalog MC) throws SQLException{
        Long last = null;
        String query = "INSERT INTO workspace (id_creator,name,all_read,all_write,all_manage,description) VALUES (?,?,?,?,?,?);";
        PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, id_creator);
        pstmt.setString(2, name);
        pstmt.setString(3, all_read);
        pstmt.setString(4, all_write);
        pstmt.setString(5, all_manage);
        pstmt.setString(6, description);
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();
        if(rs.next()){
            last = rs.getLong(1);
        }
        rs.close();
        pstmt.close();
        return last;
    }

    /**
     * Deletes a workspace from database
     * @param MC the mapcatalog object for the connection
     * @param id_workspace The primary key of the workspace
     */
    public static void delete(MapCatalog MC, Long id_workspace) throws SQLException{
        String query = "DELETE FROM workspace WHERE id_workspace = ? ;";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setLong(1, id_workspace);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Method that queries the database for workspaces, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of Workspace containing the result of the query
     */
    public static List<Workspace> page(MapCatalog MC, String[] attributes, String[] values) throws SQLException{
        String query = "SELECT * FROM workspace WHERE ";
        List<Workspace> paged = new LinkedList<Workspace>();
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
        for (String value : values) {
            if (value != null) {
                stmt.setString(j, value);
                j++;
            }
        }
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_workspace = rs.getString("id_workspace");
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
            paged.add(wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Method that queries the database for workspaces, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of Workspace containing the result of the query
     */
    public static List<Workspace> page(MapCatalog MC, String[] attributes, String[] values, int offset) throws SQLException{
        String query = "SELECT * FROM workspace WHERE ";
        List<Workspace> paged = new LinkedList<Workspace>();
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
        //Setting the offset and limit
        query+=" LIMIT '10' OFFSET ?";
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        int j=1;
        for (String value : values) {
            if (value != null) {
                stmt.setString(j, value);
                j++;
            }
        }
        stmt.setInt(j, offset);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_workspace = rs.getString("id_workspace");
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
            paged.add(wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Counts the number of workspace that matches the where clause
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return the number of workspaces
     */
    public static int pageCount(MapCatalog MC, String[] attributes, String[] values) throws SQLException{
        String query = "SELECT COUNT(*) FROM workspace WHERE ";
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
        for (String value : values) {
            if (value != null) {
                stmt.setString(j, value);
                j++;
            }
        }
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        int count=0;
        if(rs.next()){
            count = rs.getInt("count(*)");
        }
        rs.close();
        stmt.close();
        return count;
    }

    /**
     * Method that sends a query to database SELECT * FROM Workspace
     * @param MC the mapcatalog object for the connection
     * @return A list of Workspace containing the result of the query
     */
    public static List<Workspace> page(MapCatalog MC) throws SQLException{
        String query = "SELECT * FROM workspace";
        List<Workspace> paged = new LinkedList<Workspace>();
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_workspace = rs.getString("id_workspace");
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
            paged.add(wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Method that sends a query to database SELECT * FROM Workspace
     * @param MC the mapcatalog object for the connection
     * @return A list of Workspace containing the result of the query
     */
    public static List<Workspace> page(MapCatalog MC, int offset) throws SQLException{
        String query = "SELECT * FROM workspace LIMIT '10' OFFSET ?";
        List<Workspace> paged = new LinkedList<Workspace>();
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setInt(1, offset);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_workspace = rs.getString("id_workspace");
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
            paged.add(wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Returns the number of workspace in database
     * @param MC the mapcatalog object for the connection
     * @return The number of workspaces in database
     */
    public static int pageCount(MapCatalog MC) throws SQLException{
        String query = "SELECT COUNT(*) FROM workspace";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        int count=0;
        if(rs.next()){
            count = rs.getInt("count(*)");
        }
        rs.close();
        stmt.close();
        return count;
    }

    /**
     * Execute a query "UPDATE" in the database
     * @param MC the mapcatalog used for database connection
     */
    public void update(MapCatalog MC) throws SQLException{
        String query = "UPDATE workspace SET name = ? , all_read = ? , all_write = ? , all_manage = ? , description = ? , id_creator = ? WHERE id_workspace = ?;";
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, name);
        stmt.setString(2, all_read);
        stmt.setString(3, all_write);
        stmt.setString(4, all_manage);
        stmt.setString(5, description);
        stmt.setString(6, id_creator);
        stmt.setString(7, id_workspace);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Verify if a user is creator of the workspace
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     */
    public static boolean isCreator(MapCatalog MC, String id_workspace, String id_user) throws SQLException{
        String[] attributes = {"id_creator","id_workspace"};
        String[] values = {id_user, id_workspace};
        List<Workspace> workspaceList = Workspace.page(MC, attributes, values);
        return !workspaceList.isEmpty();
    }

    /**
     *Queries database to search for a workspace containing a certain expression in his name, or his description
     * @param expression the String to run the search on, case insensitive
     * @return The list of workspaces corresponding to the search
     */
    public static List<Workspace> search(MapCatalog MC, String expression) throws SQLException{
        String query = "SELECT * FROM WORKSPACE WHERE ((LOWER(name) LIKE ?) OR (LOWER(description)) LIKE ?)";
        List<Workspace> searched = new LinkedList<Workspace>();
        expression = "%" + expression.toLowerCase() + "%";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, expression);
        stmt.setString(2, expression);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_workspace = rs.getString("id_workspace");
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
            searched.add(wor);
        }
        rs.close();
        stmt.close();
        return searched;
    }

    /**
     *Queries database to search for a workspace containing a certain expression in his name, or his description
     * @param expression the String to run the search on, case insensitive
     * @return The list of workspaces corresponding to the search
     */
    public static List<Workspace> search(MapCatalog MC, String expression, int offset) throws SQLException{
        String query = "SELECT * FROM WORKSPACE WHERE ((LOWER(name) LIKE ?) OR (LOWER(description) LIKE ?)) LIMIT '10' OFFSET ?";
        List<Workspace> searched = new LinkedList<Workspace>();
        expression = "%" + expression.toLowerCase() + "%";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, expression);
        stmt.setString(2, expression);
        stmt.setInt(3, offset);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_workspace = rs.getString("id_workspace");
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
            searched.add(wor);
        }
        rs.close();
        stmt.close();
        return searched;
    }

    /**
     * Counts the number of workspaces corresponding with the search
     * @param expression the String to run the search on, case insensitive
     * @return The number of workspaces corresponding to the search
     */
    public static int searchCount(MapCatalog MC, String expression) throws SQLException{
        String query = "SELECT COUNT(*) FROM WORKSPACE WHERE ((LOWER(name) LIKE ?) OR (LOWER(description)) LIKE ?)";
        expression = "%" + expression.toLowerCase() + "%";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, expression);
        stmt.setString(2, expression);
        ResultSet rs = stmt.executeQuery();
        int count=0;
        if(rs.next()){
            count = rs.getInt("COUNT(*)");
        }
        rs.close();
        stmt.close();
        return count;
    }

    /**
     *Queries database to search for a workspace containing a certain expression in his name, or his description, with a specific creator
     * @param expression the String to run the search on, case insensitive
     * @param id_user The creator of the workspace
     * @return The list of workspaces corresponding to the search
     */
    public static List<Workspace> searchMyWorkspacesCreated(MapCatalog MC, String expression, String id_user) throws SQLException{
        String query = "SELECT * FROM WORKSPACE WHERE ((LOWER(name) LIKE ?) OR (LOWER(description) LIKE ?)) AND id_creator= ?";
        List<Workspace> searched = new LinkedList<Workspace>();
        expression = "%" + expression.toLowerCase() + "%";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, expression);
        stmt.setString(2, expression);
        stmt.setString(3, id_user);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_workspace = rs.getString("id_workspace");
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
            searched.add(wor);
        }
        rs.close();
        stmt.close();
        return searched;
    }

    /**
     *Queries database to search for a workspace containing a certain expression in his name, or his description, with a specific creator
     * @param expression the String to run the search on, case insensitive
     * @param id_user The creator of the workspace
     * @return The list of workspaces corresponding to the search
     */
    public static List<Workspace> searchMyWorkspacesCreated(MapCatalog MC, String expression, String id_user, int offset) throws SQLException{
        String query = "SELECT * FROM WORKSPACE WHERE ((LOWER(name) LIKE ?) OR (LOWER(description) LIKE ?)) AND id_creator= ? LIMIT '10' OFFSET ?";
        List<Workspace> searched = new LinkedList<Workspace>();
        expression = "%" + expression.toLowerCase() + "%";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, expression);
        stmt.setString(2, expression);
        stmt.setString(3, id_user);
        stmt.setInt(4, offset);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_workspace = rs.getString("id_workspace");
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_creator,name,all_read,all_write,all_manage,description);
            searched.add(wor);
        }
        rs.close();
        stmt.close();
        return searched;
    }

    /**
     * Counts the number of workspace created by a certain user
     * @param expression the String to run the search on, case insensitive
     * @param id_user The creator of the workspace
     * @return The list of workspaces corresponding to the search
     */
    public static int searchMyWorkspacesCreatedCount(MapCatalog MC, String expression, String id_user) throws SQLException{
        String query = "SELECT COUNT(*) FROM WORKSPACE WHERE ((LOWER(name) LIKE ?) OR (LOWER(description) LIKE ?)) AND id_creator= ?";
        expression = "%" + expression.toLowerCase() + "%";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, expression);
        stmt.setString(2, expression);
        stmt.setString(3, id_user);
        ResultSet rs = stmt.executeQuery();
        int count=0;
        if(rs.next()){
            count = rs.getInt("count(*)");
        }
        rs.close();
        stmt.close();
        return count;
    }
}