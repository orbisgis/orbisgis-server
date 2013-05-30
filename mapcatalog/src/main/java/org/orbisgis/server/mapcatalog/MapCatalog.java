package org.orbisgis.server.mapcatalog;
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
     * Executes a DELETE or INSERT query in database, be carefull, as it does not handle SQL injections (use it without input from user)
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
     * Executes a SELECT values FROM table WHERE conditions query into database, be carefull, as it does not handle SQL injections
     * @param con   The connection to the database
     * @param query The query you wish to execute
     * @return  The Selected data in a string array
     */
    public static ArrayList executeSQLselect(Connection con, String query) {
        Statement stmt;
        ArrayList<String[]> value = new ArrayList();
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                int lenght = rs.getMetaData().getColumnCount();
                String[] temp = new String[lenght];
                for (int i=0; i<lenght; i++){
                    temp[i] = rs.getString(i+1);
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

    public static ArrayList<String[]> selectWhere(String model, String whereclause){
        ArrayList<String[]> values = new ArrayList<String[]>();
        String[] attributesvalue = whereclause.split("[=,]");
        String qmark = attributesvalue[0]+" = ? ";
        for(int i=1;2*i<attributesvalue.length;i++){
            qmark += ","+attributesvalue[2*i]+" = ?";
        }
        String query = "SELECT * FROM "+model+" WHERE "+qmark+";";
        try{
            PreparedStatement pstmt = MapCatalog.getConnection().prepareStatement(query);
            for(int i=1;i<attributesvalue.length;i+=2){
                pstmt.setString(i, attributesvalue[i].trim());
            }
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                int length = rs.getMetaData().getColumnCount();
                String[] ar = new String[length];
                for(int i=0;i<length;i++){
                    ar[i] = rs.getString(i+1);
                }
                values.add(ar);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return values;
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
                e2.setAttribute("xml:lang", getTitleLang(values.get(i)[4])[0]);
                e2.appendChild(dom.createTextNode(getTitleLang(values.get(i)[4])[1]));
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
     * @param values The string "content" of the ows context
     * @return The lang and the title in an array if found, else returns {"default","default"}
     */
    private static String[] getTitleLang(String values){
        int indexbegin = values.indexOf("ns1:Title xml:lang=")+20;
        int indexend = values.indexOf("</ns1:Title>");
       if (!values.contains("ns1:Title xml:lang=")) {
           return (new String[] {"default", "default"});
       }
       return values.substring(indexbegin, indexend).split(">");
    }

    public static void main(String[] args) {
        ArrayList<String[]> value = selectWhere("workspace", "isPublic = 0");
        for(int i=0; i<value.size(); i++){
            for(int j=0; j<value.get(i).length; j++){
                System.out.println(value.get(i)[j]);
            }
        }
    }
}