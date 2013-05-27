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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

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

    private static final String URL = "jdbc:h2:tcp://localhost/~/test";
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
    public static ArrayList executeSQLselect(Connection con, String query) {
        Statement stmt;
        int indexbegin = query.lastIndexOf("SELECT")+6;
        int indexend = query.indexOf("FROM")-1;
        String[] collumns;
        collumns = query.substring(indexbegin, indexend).split(",");
        ArrayList<String[]> value = new ArrayList();
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                String[] temp = new String[collumns.length];
                for (int i=0; i<collumns.length; i++){
                    temp[i] = rs.getString(collumns[i].trim());
                }
                value.add(temp);
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
     * Creates a User and saves it into  database with right connection
     * @param name
     * @param email
     * @param password
     * @param location
     * @return The id_user of the user created (primary key)
     */
    public static Long createUser(String name, String email, String password, String location) {
        Long last = null;
        User use = new User(name, email, password, location);
        try{
            last = use.saveUser(getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return last;
    }

    /**
     * Creates a OWSContext and saves it into  database with right connection
     * @param id_root
     * @param id_parent
     * @param id_uploader
     * @param content
     * @return The id_owscontext of the context created (primary key)
     */
    public static Long createOWS(Long id_root, Long id_parent, Long id_uploader, String content) {
        Long last = null;
        OWSContext ows = new OWSContext(id_root, id_parent, id_uploader, content);
        try{
            last = ows.saveOWSContext(getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return last;
    }

    /**
     * Creates a comment and saves it into  database with right connection
     * @param id_writer
     * @param id_map
     * @param content
     * @return The id_comment of the comment created (primary key)
     */
    public static Long createComment(Long id_writer, Long id_map, String content) {
        Long last = null;
        Comment com = new Comment(id_writer, id_map, content);
        try{
            last = com.saveComment(getConnection());
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

    /**
     * Delete a user from database
     * @param id_user
     */
    public static void deleteUser(Long id_user) {
        String query = "DELETE FROM user " +
                "WHERE id_user = " + id_user +";";
        try{
            executeSQLupdate(getConnection(), query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a owscontext from database
     * @param id_owscontext
     */
    public static void deleteOWSContext(Long id_owscontext) {
        String query = "DELETE FROM owscontext " +
                "WHERE id_owscontext = " + id_owscontext +";";
        try{
            executeSQLupdate(getConnection(), query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a comment from database
     * @param id_comment
     */
    public static void deleteComment(Long id_comment) {
        String query = "DELETE FROM comment " +
                "WHERE id_comment = " + id_comment +";";
        try{
            executeSQLupdate(getConnection(), query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a query to database that returns the workspace list, then write it in a xml file a the root of project.
     */
    public static void getWorkspaceList() {
        String query = "SELECT name FROM workspace";
        ArrayList<String[]> values;
        try{
            //get the list of workspace names from the database
            values = executeSQLselect(getConnection(), query);

            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();;
            Element e ;
            Element e2 ;
            Element rootEle = dom.createElement("workspaces");

            //Create a node for each workspace, and add the name
            for (int i=0; i<values.size(); i++) {
                e = dom.createElement("workspace");
                e2 = dom.createElement("name");
                e2.appendChild(dom.createTextNode(values.get(i)[0]));
                e.appendChild(e2);
                rootEle.appendChild(e);
            }
            dom.appendChild(rootEle);

            //transform the dom in XML
            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream("workspaces")));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Queries the database for the list of context, then writes it into a XML file
     * @param id_workspace The workspace which
     */
    public static void getContextList(Long id_workspace){
        String query = "SELECT id_owscontext,id_root,id_parent,id_uploader,content,title,date FROM owscontext WHERE id_root="+id_workspace;
        ArrayList<String[]> values;
        try{
            //get the ows from database
            values = executeSQLselect(getConnection(), query);

            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();;
            Element e;
            Element e2 ;
            Element rootEle = dom.createElement("contexts");

            //create a node for each ows
            for (int i=0; i<values.size(); i++) {
                e = dom.createElement("context");
                e.setAttribute("id", String.valueOf(i));
                e.setAttribute("date", values.get(i)[6]);
                e2 = dom.createElement("title");
                e2.setAttribute("xml:lang", getTitleLang(values.get(i))[0]);
                e2.appendChild(dom.createTextNode(getTitleLang(values.get(i))[1]));
                e.appendChild(e2);
                rootEle.appendChild(e);
            }
            dom.appendChild(rootEle);

            //transforms into XML
            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream("contexts")));

            } catch (Exception exe) {
                exe.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method's only purpose is to simplify the parsing of the content of an OWSCONTEXT to get the lang and the title
     * @param values The Array of string containing exactly 7 rows
     * @return The lang and the title in an array if found, else returns {"default","default"}
     */
    private static String[] getTitleLang(String[] values){
       if (!values[4].contains("ns1:Title xml:lang=")) {
           return (new String[] {"default", "default"});
       }
       return values[4].substring(values[4].indexOf("ns1:Title xml:lang=")+20, values[4].indexOf("</ns1:Title>")).split(">");
    }

    public static void main(String[] args) {
        getContextList(new Long(1));
        getWorkspaceList();
    }
}