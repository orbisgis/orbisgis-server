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
import java.util.LinkedList;
import java.util.List;

/**
 * Java model of the table Comment
 * @author Mario Jothy
 */
public class Comment {
    private String id_comment = null;
    private String id_writer = null;
    private String id_map = null;
    private String content = "";
    private String title = "default";

    /**
     * Constructor
     * @param id_writer
     * @param id_map
     * @param content
     * @param title
     */
    public Comment(String id_writer, String id_map, String content, String title) {
        this.id_writer = id_writer;
        this.id_map = id_map;
        this.content = content;
        this.title = title;
    }

    /**
     * Constructor with primary key
     * @param id_comment
     * @param id_writer
     * @param id_map
     * @param content
     * @param title
     */
    public Comment(String id_comment, String id_writer, String id_map, String content, String title) {
        this.id_comment = id_comment;
        this.id_writer = id_writer;
        this.id_map = id_map;
        this.content = content;
        this.title = title;
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

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Method that saves a instantiated comment into database. Handles SQL injections.
     * @return The ID of the comment just created (primary key)
     */
    public Long save(MapCatalog MC) {
        Long last = null;
        try{
            String query = "INSERT INTO comment (id_writer,id_map,content,title) VALUES (? , ? , ? , ?);";
            PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, id_writer);
            pstmt.setString(2, id_map);
            pstmt.setString(3, content);
            pstmt.setString(4, title);
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
     * Deletes a comment from database
     * @param id_comment The primary key of the comment
     */
    public static void delete(MapCatalog MC, Long id_comment) {
        String query = "DELETE FROM comment WHERE id_comment = ? ;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_comment);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that queries the database for comments, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of Comment containing the result of the query
     */
    public static List<Comment> page(MapCatalog MC, String[] attributes, String[] values){
        String query = "SELECT * FROM comment WHERE ";
        List<Comment> paged = new LinkedList<Comment>();
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
                String id_comment = rs.getString("id_Comment");
                String id_writer = rs.getString("id_writer");
                String id_map = rs.getString("id_map");
                String content = rs.getString("content");
                String title = rs.getString("title");
                Comment com = new Comment(id_comment,id_writer,id_map,content,title);
                paged.add(com);
            }
            rs.close();
        } catch (SQLException e) {e.printStackTrace();}
        return paged;
    }

    /**
     * Method that sends a query to database SELECT * FROM COMMENT
     * @return A list of comment containing the result of the query
     */
    public static List<Comment> page(MapCatalog MC){
        String query = "SELECT * FROM comment";
        List<Comment> paged = new LinkedList<Comment>();
        try {
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String id_comment = rs.getString("id_Comment");
                String id_writer = rs.getString("id_writer");
                String id_map = rs.getString("id_map");
                String content = rs.getString("content");
                String title = rs.getString("title");
                Comment com = new Comment(id_comment,id_writer,id_map,content,title);
                paged.add(com);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paged;
    }
}
