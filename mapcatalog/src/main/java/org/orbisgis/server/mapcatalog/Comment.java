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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Java model of the table Comment
 * @author Mario Jothy
 */
public class Comment implements Comparable{
    private String id_comment = null;
    private String id_writer = null;
    private String id_map = null;
    private String title = "default";
    private Timestamp date;

    /**
     * Constructor without primary key, usually when saving in database
     * @param id_writer
     * @param id_map
     * @param title
     */
    public Comment(String id_writer, String id_map, String title) {
        this.id_writer = id_writer;
        this.id_map = id_map;
        this.title = title;
    }

    /**
     * Constructor with primary key, usually for SELECT queries
     * @param id_comment
     * @param id_writer
     * @param id_map
     * @param title
     * @param date
     */
    public Comment(String id_comment, String id_writer, String id_map, String title, Timestamp date) {
        this.id_comment = id_comment;
        this.id_writer = id_writer;
        this.id_map = id_map;
        this.title = title;
        this.date = date;
    }

    public String getId_comment() {
        return id_comment;
    }

    public String getId_writer() {
        return id_writer;
    }

    public String getId_map() {
        return id_map;
    }

    public Timestamp getDate() {
        return date;
    }

    /**
     * Gets an input stream for the content of a comment in the database
     * @param MC The instance of MapCatalog to get connection to database from
     * @return An InputStream containing content of a comment
     * @throws SQLException
     */
    public InputStream getContent(MapCatalog MC) throws SQLException{
        String query = "SELECT content FROM comment WHERE id_comment = ?";
        InputStream content = null;
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id_comment);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            content = rs.getAsciiStream("content");
        }
        return content;
    }

    public String getTitle() {
        return title;
    }

    public int compareTo (Object object) throws ClassCastException{
        if(!(object instanceof Comment)){
            throw  new ClassCastException("compareTo method can only be applied to Comment");
        }else{
            return this.id_comment.compareTo(((Comment)object).id_comment);
        }
    }

    /**
     * Method that saves a instantiated comment into database. Handles SQL injections.
     * @param MC the mapcatalog object for the connection
     * @return The ID of the comment just created (primary key)
     */
    public Long save(MapCatalog MC, InputStream content) throws SQLException{
        Long last = null;
        String query = "INSERT INTO comment (id_writer,id_map,content,title) VALUES (? , ? , ? , ?);";
        PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, id_writer);
        pstmt.setString(2, id_map);
        pstmt.setAsciiStream(3, content);
        pstmt.setString(4, title);
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
     * Deletes a comment from database
     * @param MC the mapcatalog object for the connection
     * @param id_comment The primary key of the comment
     */
    public static void delete(MapCatalog MC, Long id_comment) throws SQLException{
        String query = "DELETE FROM comment WHERE id_comment = ? ;";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setLong(1, id_comment);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Method that queries the database for comments, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param MC the mapcatalog object for the connection
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of Comment containing the result of the query
     */
    public static List<Comment> page(MapCatalog MC, String[] attributes, String[] values) throws SQLException{
        String query = "SELECT * FROM comment WHERE ";
        List<Comment> paged = new LinkedList<Comment>();
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
            String id_comment = rs.getString("id_Comment");
            String id_writer = rs.getString("id_writer");
            String id_map = rs.getString("id_map");
            String title = rs.getString("title");
            Timestamp date = rs.getTimestamp("date");
            Comment com = new Comment(id_comment,id_writer,id_map,title,date);
            paged.add(com);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * Method that sends a query to database SELECT * FROM COMMENT
     * @param MC the mapcatalog object for the connection
     * @return A list of comment containing the result of the query
     */
    public static List<Comment> page(MapCatalog MC) throws SQLException{
        String query = "SELECT * FROM comment";
        List<Comment> paged = new LinkedList<Comment>();
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_comment = rs.getString("id_Comment");
            String id_writer = rs.getString("id_writer");
            String id_map = rs.getString("id_map");
            String title = rs.getString("title");
            Timestamp date = rs.getTimestamp("date");
            Comment com = new Comment(id_comment,id_writer,id_map,title, date);
            paged.add(com);
        }
        rs.close();
        stmt.close();
        return paged;
    }

    /**
     * This method returns a hash map containing all Comments linked to their writer (user). The password is not specified. Only ten comments are returned, begining at offset, in date order desc
     * @param MC The mapcalog instance to get the connection to database
     * @param id_owscontext The id of the map context containing the comments
     * @param offset the number of the first of ten comments returned
     * @return A hash map comment user
     * @throws SQLException
     */
    public static SortedMap<Comment, User> pageWithMap(MapCatalog MC, String id_owscontext, int offset) throws SQLException {
        String query = "SELECT * FROM comment JOIN user ON comment.id_writer=user.id_user WHERE comment.id_map=? ORDER BY id_comment ASC NULLS LAST LIMIT '10' offset ?";
        TreeMap<Comment, User> paged = new TreeMap<Comment,User>();
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id_owscontext);
        stmt.setInt(2, offset);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            String id_comment = rs.getString("id_comment");
            String id_writer = rs.getString("id_writer");
            String id_map = rs.getString("id_map");
            String title = rs.getString("title");
            Timestamp date = rs.getTimestamp("date");
            Comment com = new Comment(id_comment, id_writer, id_map, title, date);
            String id_user = rs.getString("id_user");
            String name = rs.getString("name");
            String email = rs.getString("email");
            String location = rs.getString("location");
            String profession = rs.getString("profession");
            String additional = rs.getString("additional");
            User use = new User(id_user, name, email, null, location, profession, additional);
            paged.put(com, use);
        }
        rs.close();
        stmt.close();
        return paged.descendingMap();
    }

    /**
     * Counts the number of comments linked to a map
     * @param MC The MapCatalog instance to get the connection to database
     * @param id_owscontext The map context containing the comments
     * @return the number of comments
     * @throws SQLException
     */
    public static int pageWithMapCount(MapCatalog MC, String id_owscontext) throws SQLException {
        String query = "SELECT count(*) FROM comment JOIN user ON comment.id_writer=user.id_user WHERE comment.id_map=?";
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, id_owscontext);
        int count = 0;
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            count = rs.getInt("count(*)");
        }
        rs.close();
        stmt.close();
        return count;
    }

    /**
     * Execute a query "UPDATE" in the database, only title and id are taken into account in the object
     * @param MC the mapcatalog used for database connection
     */
    public void update(MapCatalog MC, InputStream content) throws SQLException{
        String query = "UPDATE comment SET title = ?, content = ? WHERE id_comment = ?;";
        //preparation of the statement
        PreparedStatement stmt = MC.getConnection().prepareStatement(query);
        stmt.setString(1, title);
        stmt.setAsciiStream(2, content);
        stmt.setString(3, id_comment);
        stmt.executeUpdate();
        stmt.close();
    }
}
