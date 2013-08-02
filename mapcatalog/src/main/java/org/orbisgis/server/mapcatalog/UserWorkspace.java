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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Java model of the table UserWorkspace
 */
public class UserWorkspace {
    private String id_user;
    private String id_workspace;
    private String read = "0";
    private String write = "0";
    private String manageUser = "0";

    /**
     * Constructor
     * @param id_user the id of the user in the relation
     * @param id_workspace the id of the workspace in the relation
     * @param read The read access of the user in this workspace
     * @param write The write access of the user in this workspace
     * @param manageUser The manage access of the user in this workspace
     */
    public UserWorkspace(String id_user, String id_workspace, String read, String write, String manageUser) {
        this.id_user = id_user;
        this.id_workspace = id_workspace;
        this.read = read;
        this.write = write;
        this.manageUser = manageUser;
    }

    public String getId_user() {
        return id_user;
    }

    public String getId_workspace() {
        return id_workspace;
    }

    public String getRead() {
        return read;
    }

    public String getWrite() {
        return write;
    }

    public String getManageUser() {
        return manageUser;
    }

    /**
     * Method that saves a instantiated User_Workspace relation into database. Handles SQL injections.
     * @param MC the mapcatalog object for the connection
     * @return The ID of the User just created (primary key)
     */
    public Long save(MapCatalog MC) throws SQLException{
        Long last = null;
        String query = "INSERT INTO user_workspace (id_user,id_workspace,read,write,manage_user) VALUES (? , ? , ? , ? , ?);";
        PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, id_user);
        pstmt.setString(2, id_workspace);
        pstmt.setString(3, read);
        pstmt.setString(4, write);
        pstmt.setString(5, manageUser);
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
     * Deletes a user_workspace relation from database
     * @param MC the mapcatalog object for the connection
     * @param id_user The primary key of the user
     * @param id_workspace The primary key of the workspace
     */
    public static void delete(MapCatalog MC, Long id_user, Long id_workspace) throws SQLException{
        String query = "DELETE FROM user_workspace WHERE id_user = ? AND id_workspace = ?;";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setLong(1, id_user);
        stmt.setLong(2, id_workspace);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Method that queries the database for UserWorkspace relations, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of UserWorkspace containing the result of the query
     */
    public static List<UserWorkspace> page(MapCatalog MC, String[] attributes, String[] values) throws SQLException{
        String query = "SELECT * FROM user_workspace WHERE ";
        List<UserWorkspace> paged = new LinkedList<UserWorkspace>();
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
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            paged.add(usewor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Method that queries the database for UserWorkspace relations, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of UserWorkspace containing the result of the query
     */
    public static List<UserWorkspace> page(MapCatalog MC, String[] attributes, String[] values, int offset) throws SQLException{
        String query = "SELECT * FROM user_workspace WHERE ";
        List<UserWorkspace> paged = new LinkedList<UserWorkspace>();
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
        stmt.setInt(j+1, offset);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            paged.add(usewor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Queries for a join from user_workspace and User, to get the information about each user linked to a workspace
     * @param MC the mapcatalog object for the connection
     * @param id The id of the user
     * @return A map containing, for each Workspace, the relation as key, and the workspace as value
     */
    public static Map<UserWorkspace, User> pageWithUser(MapCatalog MC, String id) throws SQLException{
        String query = "SELECT * FROM USER_WORKSPACE JOIN USER ON USER.ID_USER=USER_WORKSPACE.ID_USER WHERE USER_WORKSPACE.ID_WORKSPACE = ?";
        HashMap<UserWorkspace, User> paged = new HashMap<UserWorkspace, User>();
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            String name = rs.getString("name");
            String email = rs.getString("email");
            String password = rs.getString("password");
            String location = rs.getString("location");
            String profession = rs.getString("profession");
            String additional = rs.getString("additional");
            String admin_wms = rs.getString("admin_wms");
            String admin_mapcatalog = rs.getString("admin_mapcatalog");
            String admin_wps = rs.getString("admin_wps");
            User use = new User(id_user, name, email, password, location,profession,additional, admin_wms, admin_mapcatalog, admin_wps);
            paged.put(usewor,use);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Queries for a join from user_workspace and User, to get the information about each user linked to a workspace
     * @param MC the mapcatalog object for the connection
     * @param id The id of the user
     * @param offset The number of workspace to skip
     * @return A hashmap containing, for each Workspace, the relation as key, and the workspace as value
     */
    public static Map<UserWorkspace, User> pageWithUser(MapCatalog MC, String id, int offset) throws SQLException{
        String query = "SELECT * FROM USER_WORKSPACE JOIN USER ON USER.ID_USER=USER_WORKSPACE.ID_USER WHERE USER_WORKSPACE.ID_WORKSPACE = ? LIMIT '10' OFFSET ?";
        HashMap<UserWorkspace, User> paged = new HashMap<UserWorkspace, User>();
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        stmt.setInt(2, offset);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            String name = rs.getString("name");
            String email = rs.getString("email");
            String password = rs.getString("password");
            String location = rs.getString("location");
            String profession = rs.getString("profession");
            String additional = rs.getString("additional");
            String admin_wms = rs.getString("admin_wms");
            String admin_mapcatalog = rs.getString("admin_mapcatalog");
            String admin_wps = rs.getString("admin_wps");
            User use = new User(id_user, name, email, password, location,profession,additional, admin_wms, admin_mapcatalog, admin_wps);
            paged.put(usewor,use);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Querys for a join from user_workspace and Workspace, to get the information about each workspaces linked to a user where the access to management is granted
     * @param MC the mapcatalog object for the connection
     * @param id The id of the user
     * @return A hashmap containing, for each Workspace, the relation as key, and the workspace as value were the user has management right
     */
    public static Map<UserWorkspace, Workspace> pageWithWorkspaceManage(MapCatalog MC, String id) throws SQLException{
        String query = "SELECT * FROM USER_WORKSPACE JOIN WORKSPACE ON WORKSPACE.ID_WORKSPACE=USER_WORKSPACE.ID_WORKSPACE WHERE USER_WORKSPACE.ID_USER = ? AND( ALL_MANAGE = 1 OR MANAGE_USER = 1)";
        HashMap<UserWorkspace, Workspace> paged = new HashMap<UserWorkspace, Workspace>();
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_user, name, all_read, all_write, all_manage, description);
            paged.put(usewor,wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Queries for a join from user_workspace and Workspace, to get the information about each workspaces linked to a user where the access to management is granted
     * @param MC the mapcatalog object for the connection
     * @param id The id of the user
     * @return A hashmap containing, for each Workspace, the relation as key, and the workspace as value were the user has management right
     */
    public static Map<UserWorkspace, Workspace> pageWithWorkspaceManage(MapCatalog MC, String id, int offset) throws SQLException{
        String query = "SELECT * FROM USER_WORKSPACE JOIN WORKSPACE ON WORKSPACE.ID_WORKSPACE=USER_WORKSPACE.ID_WORKSPACE WHERE USER_WORKSPACE.ID_USER = ? AND( ALL_MANAGE = 1 OR MANAGE_USER = 1) LIMIT '10' OFFSET ?";
        HashMap<UserWorkspace, Workspace> paged = new HashMap<UserWorkspace, Workspace>();
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        stmt.setInt(2, offset);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace,id_user, name, all_read, all_write, all_manage, description);
            paged.put(usewor,wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Queries for a join from user_workspace and Workspace, to get the information about each workspaces linked to a user where the access to management is granted
     * @param MC the mapcatalog object for the connection
     * @param id The id of the user
     * @return A hashmap containing, for each Workspace, the relation as key, and the workspace as value
     */
    public static Map<UserWorkspace, Workspace> pageWithWorkspace(MapCatalog MC, String id) throws SQLException{
        String query = "SELECT * FROM USER_WORKSPACE JOIN WORKSPACE ON WORKSPACE.ID_WORKSPACE=USER_WORKSPACE.ID_WORKSPACE WHERE USER_WORKSPACE.ID_USER = ?";
        HashMap<UserWorkspace, Workspace> paged = new HashMap<UserWorkspace, Workspace>();
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace, id_creator, name, all_read, all_write, all_manage, description);
            paged.put(usewor,wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Querys for a join from user_workspace and Workspace, to get the information about each workspaces linked to a user where the access to management is granted
     * @param MC the mapcatalog object for the connection
     * @param id The id of the user
     * @return A hashmap containing, for each Workspace, the relation as key, and the workspace as value were the user has management right
     */
    public static Map<UserWorkspace, Workspace> pageWithWorkspace(MapCatalog MC, String id, int offset) throws SQLException{
        String query = "SELECT * FROM USER_WORKSPACE JOIN WORKSPACE ON WORKSPACE.ID_WORKSPACE=USER_WORKSPACE.ID_WORKSPACE WHERE USER_WORKSPACE.ID_USER = ? LIMIT '10' OFFSET ?";
        HashMap<UserWorkspace, Workspace> paged = new HashMap<UserWorkspace, Workspace>();
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        stmt.setInt(2, offset);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace, id_creator, name, all_read, all_write, all_manage, description);
            paged.put(usewor,wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Querys for a join from user_workspace and Workspace, to get the information about each workspaces linked to a user where the access to management is granted
     * @param MC the mapcatalog object for the connection
     * @param id The id of the user
     * @return the number of results corresponding to the query
     */
    public static int pageWithWorkspaceCount(MapCatalog MC, String id) throws SQLException{
        String query = "SELECT COUNT(*) FROM USER_WORKSPACE JOIN WORKSPACE ON WORKSPACE.ID_WORKSPACE=USER_WORKSPACE.ID_WORKSPACE WHERE USER_WORKSPACE.ID_USER = ?";
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        int count = 0;
        if(rs.next()){
            count = rs.getInt("COUNT(*)");
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
        String query = "UPDATE user_workspace SET read = ? , write = ? , manage_user = ? WHERE id_user = ? AND id_workspace = ?;";
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, read);
        stmt.setString(2, write);
        stmt.setString(3, manageUser);
        stmt.setString(4, id_user);
        stmt.setString(5, id_workspace);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Verify if a user is following the workspace
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     * @return true if monitoring, false if not
     */
    public static boolean isMonitoring(MapCatalog MC, String id_workspace, String id_user) throws SQLException{
        String[] attributes = {"id_user","id_workspace"};
        String[] values = {id_user, id_workspace};
        List<UserWorkspace> useworList = UserWorkspace.page(MC, attributes, values);
        return !useworList.isEmpty();
    }

    /**
     * Verify if a user has read right
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     */
    public static boolean hasReadRight(MapCatalog MC, String id_workspace, String id_user) throws SQLException{
        String[] attributes = {"id_user","id_workspace","READ"};
        String[] values = {id_user, id_workspace,"1"};
        List<UserWorkspace> useworList = UserWorkspace.page(MC, attributes, values);
        return !useworList.isEmpty();
    }

    /**
     * Verify if a user has read right
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     */
    public static boolean hasWriteRight(MapCatalog MC, String id_workspace, String id_user) throws SQLException{
        String[] attributes = {"id_user","id_workspace","WRITE"};
        String[] values = {id_user, id_workspace,"1"};
        List<UserWorkspace> useworList = UserWorkspace.page(MC, attributes, values);
        return !useworList.isEmpty();
    }

    /**
     * Verify if a user has management right
     * @param id_workspace the workspace to test
     * @param id_user the user to test
     */
    public static boolean hasManageRight(MapCatalog MC, String id_workspace, String id_user) throws SQLException{
        String[] attributes = {"id_user","id_workspace","MANAGE_USER"};
        String[] values = {id_user, id_workspace,"1"};
        List<UserWorkspace> useworList = UserWorkspace.page(MC, attributes, values);
        return !useworList.isEmpty();
    }

    /**
     * Queries for a join from user_workspace and Workspace, to get the information about each workspaces linked to a user where the access to management is granted
     * @param MC the mapcatalog object for the connection
     * @param expression the expression to look for in the name or description of the workspaces
     * @param id the user's id
     * @return A hasmap containing the key UserWorkspace linked to workspace monitored
     */
    public static HashMap<UserWorkspace, Workspace> searchMyWorkspacesMonitored(MapCatalog MC, String expression, String id) throws SQLException{
        String query = "SELECT * FROM USER_WORKSPACE JOIN WORKSPACE ON WORKSPACE.ID_WORKSPACE=USER_WORKSPACE.ID_WORKSPACE WHERE USER_WORKSPACE.ID_USER = ? AND ((LOWER(name) LIKE ?) OR (LOWER(description) LIKE ?))";
        HashMap<UserWorkspace, Workspace> paged = new HashMap<UserWorkspace, Workspace>();
        expression = "%" + expression.toLowerCase() + "%";
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        stmt.setString(2, expression);
        stmt.setString(3, expression);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace, id_creator, name, all_read, all_write, all_manage, description);
            paged.put(usewor,wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Querys for a join from user_workspace and Workspace, to get the information about each workspaces linked to a user where the access to management is granted
     * @param MC the mapcatalog object for the connection
     * @param expression the expression to look for in the name or description of the workspaces
     * @param id The id of the user
     * @return A hashmap containing the key UserWorkspace linked to workspace monitored
     */
    public static HashMap<UserWorkspace, Workspace> searchMyWorkspacesMonitored(MapCatalog MC, String expression, String id, int offset) throws SQLException{
        String query = "SELECT * FROM USER_WORKSPACE JOIN WORKSPACE ON WORKSPACE.ID_WORKSPACE=USER_WORKSPACE.ID_WORKSPACE WHERE USER_WORKSPACE.ID_USER = ? AND ((LOWER(name) LIKE ?) OR (LOWER(description) LIKE ?)) LIMIT '10' OFFSET ?";
        HashMap<UserWorkspace, Workspace> paged = new HashMap<UserWorkspace, Workspace>();
        expression = "%" + expression.toLowerCase() + "%";
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        stmt.setString(2, expression);
        stmt.setString(3, expression);
        stmt.setInt(4, offset);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_user = rs.getString("id_user");
            String id_workspace = rs.getString("id_workspace");
            String read = rs.getString("read");
            String write = rs.getString("write");
            String manageUser = rs.getString("manage_user");
            UserWorkspace usewor = new UserWorkspace(id_user,id_workspace,read,write,manageUser);
            String id_creator = rs.getString("id_creator");
            String name = rs.getString("name");
            String all_read = rs.getString("all_read");
            String all_write = rs.getString("all_write");
            String all_manage = rs.getString("all_manage");
            String description = rs.getString("description");
            Workspace wor = new Workspace(id_workspace, id_creator, name, all_read, all_write, all_manage, description);
            paged.put(usewor,wor);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Querys for a join from user_workspace and Workspace, to get the information about each workspaces linked to a user where the access to management is granted
     * @param MC the mapcatalog object for the connection
     * @param expression the expression to look for in the name or description of the workspaces
     * @param id the id of the user
     * @return the number of workspaces and UserWorkspaces corresponding to the search
     */
    public static int searchMyWorkspacesMonitoredCount(MapCatalog MC, String expression, String id) throws SQLException{
        String query = "SELECT COUNT(*) FROM USER_WORKSPACE JOIN WORKSPACE ON WORKSPACE.ID_WORKSPACE=USER_WORKSPACE.ID_WORKSPACE WHERE USER_WORKSPACE.ID_USER = ? AND ((LOWER(name) LIKE ?) OR (LOWER(description) LIKE ?))";
        expression = "%" + expression.toLowerCase() + "%";
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id);
        stmt.setString(2, expression);
        stmt.setString(3, expression);
        //Retrieving values
        ResultSet rs = stmt.executeQuery();
        int count = 0;
        if(rs.next()){
            count = rs.getInt("COUNT(*)");
        }
        rs.close();
        stmt.close();
        return count;
    }
}