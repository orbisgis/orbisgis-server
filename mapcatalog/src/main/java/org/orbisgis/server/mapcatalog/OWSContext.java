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
import java.io.InputStream;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Java model of the table OWSContext
 * @author Mario Jothy
 */
public class OWSContext {
    private String id_owscontext = null;
    private String id_root = null;
    private String id_parent = null;
    private String id_uploader = null;
    private String title = "default";
    private Date date = null;

    /**
     * Constructor
     * @param id_root
     * @param id_parent
     * @param id_uploader
     * @param title
     */
    public OWSContext(String id_root, String id_parent, String id_uploader, String title) {
        this.id_root = id_root;
        this.id_parent = id_parent;
        this.id_uploader = id_uploader;
        this.title = title;
    }

    /**
     * Constructor with primary key
     * @param id_owscontext
     * @param id_root
     * @param id_parent
     * @param id_uploader
     * @param title
     * @param date
     */
    public OWSContext(String id_owscontext, String id_root, String id_parent, String id_uploader, String title, Date date) {
        this.id_owscontext = id_owscontext;
        this.id_root = id_root;
        this.id_parent = id_parent;
        this.id_uploader = id_uploader;
        this.title = title;
        this.date = date;
    }

    public String getId_owscontext() {
        return id_owscontext;
    }

    public String getId_root() {
        return id_root;
    }

    public String getId_parent() {
        return id_parent;
    }

    public String getId_uploader() {
        return id_uploader;
    }

    /**
     * Gets an input stream for the xml content of a context in the database
     * @param MC
     * @return
     * @throws SQLException
     */
    public InputStream getContent(MapCatalog MC) throws SQLException{
        String query = "SELECT content FROM owscontext WHERE id_owscontext = ?";
        InputStream content = null;
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id_owscontext);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            content = rs.getAsciiStream("content");
        }
        return content;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    /**
     * Method that saves a instantiated OWSContext into database. Handles SQL injections.
     * @param MC the mapcatalog object for the connection
     * @param content the input stream containing the XML content of the ows context
     * @return The ID of the OWSContext just created (primary key)
     */
    public  Long save(MapCatalog MC, InputStream content) throws SQLException{
        Long last = null;
        String query = "INSERT INTO owscontext (id_root,id_parent,id_uploader,content, title) VALUES (? , ? , ? , ? , ?);";
        PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, id_root);
        pstmt.setString(2, id_parent);
        pstmt.setString(3, id_uploader);
        pstmt.setAsciiStream(4, content);
        pstmt.setString(5, title);
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
     * Deletes a owscontext from database
     * @param MC the mapcatalog object for the connection
     * @param id_owscontext The primary key of the owscontext
     */
    public static void delete(MapCatalog MC, Long id_owscontext) throws SQLException{
        String query = "DELETE FROM owscontext WHERE id_owscontext = ? ;";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setLong(1, id_owscontext);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Method that queries the database for owscontext, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of owscontext containing the result of the query
     */
    public static List<OWSContext> page(MapCatalog MC, String[] attributes, String[] values) throws SQLException{
        String query = "SELECT * FROM owscontext WHERE ";
        List<OWSContext> paged = new LinkedList<OWSContext>();
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
            String id_owscontext = rs.getString("id_owscontext");
            String id_root = rs.getString("id_root");
            String id_parent = rs.getString("id_parent");
            String id_uploader = rs.getString("id_uploader");
            String title = rs.getString("title");
            Date date = rs.getDate("date");
            OWSContext ows = new OWSContext(id_owscontext,id_root,id_parent,id_uploader,title,date);
            paged.add(ows);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Method that sends a query to database SELECT * FROM OWSCONTEXT
     * @param MC the mapcatalog object for the connection
     * @return A list of owscontext containing the result of the query
     */
    public static List<OWSContext> page(MapCatalog MC) throws SQLException{
        String query = "SELECT * FROM owscontext";
        List<OWSContext> paged = new LinkedList<OWSContext>();
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_owscontext = rs.getString("id_owscontext");
            String id_root = rs.getString("id_root");
            String id_parent = rs.getString("id_parent");
            String id_uploader = rs.getString("id_uploader");
            String title = rs.getString("title");
            Date date = rs.getDate("date");
            OWSContext ows = new OWSContext(id_owscontext,id_root,id_parent,id_uploader,title,date);
            paged.add(ows);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     *Queries database to search for a owscontext containing a certain expression in his name
     * @param expression the String to run the search on, case insensitive
     * @return The list of workspaces corresponding to the search
     */
    public static List<OWSContext> search(MapCatalog MC, String id_root, String expression) throws SQLException{
        String query = "SELECT * FROM FOLDER WHERE (LOWER(title) LIKE ?) AND (id_root = ?)";
        List<OWSContext> searched = new LinkedList<OWSContext>();
        expression = "%" + expression.toLowerCase() + "%";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, expression);
        stmt.setString(2, id_root);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_owscontext = rs.getString("id_owscontext");
            id_root = rs.getString("id_root");
            String id_parent = rs.getString("id_parent");
            String id_uploader = rs.getString("id_uploader");
            String title = rs.getString("title");
            Date date = rs.getDate("date");
            OWSContext ows = new OWSContext(id_owscontext,id_root,id_parent,id_uploader,title,date);
            searched.add(ows);
        }
        rs.close();
        stmt.close();
        return searched;
    }

    /**
     * Execute a query "UPDATE" in the database
     * @param MC the mapcatalog used for database connection
     */
    public void update(MapCatalog MC, InputStream content) throws SQLException{
        String query = "UPDATE owscontext SET id_root = ? , id_parent = ? , id_uploader = ? , content = ? , title = ? WHERE id_user = ?;";
        //preparation of the statement
        PreparedStatement pstmt = MC.getConnection().prepareStatement(query);
        pstmt.setString(1, id_root);
        pstmt.setString(2, id_parent);
        pstmt.setString(3, id_uploader);
        pstmt.setAsciiStream(4, content);
        pstmt.setString(5, title);
        pstmt.setString(6, id_owscontext);
        pstmt.executeUpdate();
        pstmt.close();
    }
}
