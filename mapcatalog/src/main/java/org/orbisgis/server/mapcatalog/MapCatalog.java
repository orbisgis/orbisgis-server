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
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
/**
 * Manages workspaces and map contexts
 * @author Mario Jothy
 */
public class MapCatalog {

    private final String DRIVER_NAME = "org.h2.Driver";

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
    private MapCatalogProperties mcp = new MapCatalogProperties();
    private String URL = "jdbc:h2:tcp://localhost/~/test";
    private String USER = "sa";
    private String PASSWORD = "";

    /**
     * Constructor to specify a database to connect
     * @param URL
     * @param USER
     * @param PASSWORD
     */
    public MapCatalog (String URL, String USER, String PASSWORD){
        this.URL = URL;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    public MapCatalog (){
    }

    /**
     * Getter for the connection to database
     * @return The connection
     * @throws SQLException if the connection is invalid
     */
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Initialize the database, and returns the MapCatalog object that provide the connection to it
     * @param mcp
     * @return
     */
    public static MapCatalog init(MapCatalogProperties mcp){
        String URL = mcp.getProperty(MapCatalogProperties.DATABASE_URL).toString();
        String user = mcp.getProperty(MapCatalogProperties.DATABASE_USER).toString();
        String password = mcp.getProperty(MapCatalogProperties.DATABASE_PASSWORD).toString();
        MapCatalog mc = new MapCatalog(URL, user, password);
        mc.executeSQL("ups.sql");
        return mc;
    }

    /**
     * Executes a .sql file
     * @param file
     */
    public void executeSQL(String file){
        String s;
        StringBuffer sb = new StringBuffer();
        try{
            //FileReader fr = new FileReader(new File(MapCatalog.class.getResource(file).toURI()));
            InputStreamReader fr = new InputStreamReader(getClass().getResourceAsStream(file));
            BufferedReader br = new BufferedReader(fr);
            while((s = br.readLine()) != null){
                sb.append(s);
            }
            br.close();
            Connection c = this.getConnection();
            Statement st = c.createStatement();
            st.execute(sb.toString());
            c.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This methods enquires a SELECT query to database with a WHERE clause, SQL injection-safe
     * @param model         The name of the table, do not let the user modify this
     * @param whereclause   The where clause
     * @return  The content in an array list
     */
    public ArrayList<ArrayList<String>> selectWhere(String model, String whereclause){
        ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
        String[] attributesvalue = whereclause.split("[=,]");
        String qmark = attributesvalue[0]+" = ? ";
        for(int i=1;2*i<attributesvalue.length;i++){
            qmark += " AND "+attributesvalue[2*i]+" = ?";
        }
        String query = "SELECT * FROM "+model+" WHERE "+qmark+";";
        try{
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            for(int i=0;2*i<attributesvalue.length;i++){
                pstmt.setString(i+1, attributesvalue[2*i+1].trim());
            }
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                int length = rs.getMetaData().getColumnCount();
                ArrayList ar = new ArrayList();
                for(int i=0;i<length;i++){
                    ar.add(rs.getString(i+1));
                }
                values.add(ar);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return values;
    }

    /**
     * Sends a query to database that returns the workspace list, then write it in a xml file a the root of project.
     */
    public InputStream getWorkspaceList() {
        try{
            //get the list of workspace names from the database
            List<Workspace>  list = Workspace.page(this);

            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();;
            Element e ;
            Element e2 ;
            Element rootEle = dom.createElement("workspaces");

            //Create a node for each workspace, and add the name
            for (Workspace wor : list) {
                e = dom.createElement("workspace");
                e2 = dom.createElement("name");
                e2.appendChild(dom.createTextNode(wor.getName()));
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
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Source xmlSource = new DOMSource(dom);
                Result outputTarget = new StreamResult(outputStream);
                tr.transform(xmlSource, outputTarget);
                InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
                return is;
            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Queries the database for the list of context, then writes it into a XML file
     * @param id_workspace The workspace which
     */
    public InputStream getContextList(String id_workspace){
        try{
            //get the ows from database
            String[] attributes = {"id_root"};
            String[] values = {id_workspace};
            List<OWSContext> list = OWSContext.page(this, attributes, values);

            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();;
            Element e;
            Element e2 ;
            Element rootEle = dom.createElement("contexts");

            //create a node for each ows
            for (OWSContext ows : list) {
                e = dom.createElement("context");
                e.setAttribute("id", ows.getId_owscontext());
                e.setAttribute("date", ows.getDate().toString());
                e2 = dom.createElement("title");
                e2.setAttribute("xml:lang", getTitleLang(ows.getContent())[0]);
                e2.appendChild(dom.createTextNode(getTitleLang(ows.getContent())[1]));
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
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Source xmlSource = new DOMSource(dom);
                Result outputTarget = new StreamResult(outputStream);
                tr.transform(xmlSource, outputTarget);
                InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
                return is;
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * This method's only purpose is to simplify the parsing of the content of an OWSCONTEXT to get the lang and the title
     * @param values The string "content" of the ows context
     * @return The lang and the title in an array if found, else returns {"default","default"}
     */
    private String[] getTitleLang(String values){
       int indexbegin = values.indexOf("ns1:Title xml:lang=")+20;
       int indexend = values.indexOf("</ns1:Title>");
       if (!values.contains("ns1:Title xml:lang=")) {
           return (new String[] {"default", "default"});
       }
       return values.substring(indexbegin, indexend).split(">");
    }

    /**
     * Hashes a string into a string SHA 256
     * @param tohash The string to hash
     * @return The hashed string
     * @throws NoSuchAlgorithmException
     */
    public static String hasher(String tohash) throws NoSuchAlgorithmException {
        //hashing the password
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(tohash.getBytes());
        byte byteData[] = md.digest();
        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception{
    }
}