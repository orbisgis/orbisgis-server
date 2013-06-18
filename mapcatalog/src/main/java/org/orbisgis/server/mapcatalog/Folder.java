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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The class of the folder model representation
 * @author Mario Jothy
 */
public class Folder {
    private String id_folder = null;
    private String id_root = "0";
    private String id_parent = null;
    private String name =  "default";

    /**
     * The constructor of the Folder
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
     * Constructor with primary key
     * @param id_folder
     * @param id_root
     * @param id_parent
     * @param name
     */
    public Folder(String id_folder, String id_root, String id_parent, String name) {
        this.id_folder = id_folder;
        this.id_root = id_root;
        this.id_parent = id_parent;
        this.name = name;
    }

    public String getId_folder() {
        return id_folder;
    }

    public String getId_root() {
        return id_root;
    }

    public String getId_parent() {
        return id_parent;
    }

    public String getName() {
        return name;
    }

    /**
     * Method that saves a instantiaCollections.reverse(list)ted folder into database. Handles SQL injections.
     * @return The ID of the folder just created (primary key)
     */
    public  Long save(MapCatalog MC) {
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
    public static void delete(MapCatalog MC, Long id_folder) {
        String query = "DELETE FROM folder WHERE id_folder = ? ;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_folder);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that queries the database for folders, with a where clause, be careful, as only the values in the where clause will be checked for SQL injections
     * @param attributes The attributes in the where clause, you should NEVER let the user bias this parameter, always hard code it.
     * @param values The values of the attributes, this is totally SQL injection safe
     * @return A list of Folder containing the result of the query
     */
    public static List<Folder> page(MapCatalog MC, String[] attributes, String[] values){
        String query = "SELECT * FROM folder WHERE ";
        List<Folder> paged = new LinkedList<Folder>();
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
                String id_folder = rs.getString("id_folder");
                String id_root = rs.getString("id_root");
                String id_parent = rs.getString("id_parent");
                String name = rs.getString("name");
                Folder fol = new Folder(id_folder,id_root,id_parent,name);
                paged.add(fol);
            }
            rs.close();
        } catch (SQLException e) {e.printStackTrace();}
        return paged;
    }

    /**
     * Method that sends a query to database SELECT * FROM FOLDER
     * @return A list of folder containing the result of the query
     */
    public static List<Folder> page(MapCatalog MC){
        String query = "SELECT * FROM folder";
        List<Folder> paged = new LinkedList<Folder>();
        try {
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String id_folder = rs.getString("id_folder");
                String id_root = rs.getString("id_root");
                String id_parent = rs.getString("id_parent");
                String name = rs.getString("name");
                Folder fol = new Folder(id_folder,id_root,id_parent,name);
                paged.add(fol);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paged;
    }

    public static List<String> getPath(MapCatalog MC, String id_folder){
        String id = id_folder;
        List<String> list = new LinkedList<String>();
        String id_root=null;
        while(id!=null){
            String[] attributes={"id_folder"};
            String[] values={id};
            List<Folder> temp = Folder.page(MC, attributes, values);
            id = temp.get(0).getId_parent();
            list.add(temp.get(0).getName());
            id_root = temp.get(0).getId_root();
        }
        String[] attributes={"id_workspace"};
        String[] values={id_root};
        list.add(Workspace.page(MC,attributes,values).get(0).getName());
        Collections.reverse(list);
        return list;
    }
}
