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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
/**
 * Manages workspaces and map contexts
 * @author Mario Jothy
 */
public class MapCatalog {

    private static final int VERSION = 1;
    static {
        try
        {
            String DRIVER_NAME = "org.h2.Driver";
            Class.forName(DRIVER_NAME).newInstance();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    private String URL = "jdbc:h2:~/test";
    private String USER = "sa";
    private String PASSWORD = "";
    static final String URL_TEST = "jdbc:h2:./target/testdb";
    static final String USER_TEST = "sa";
    static final String PASSWORD_TEST = "";

    /**
     * Constructor to specify a database to connect
     * @param URL The URL of the database
     * @param USER The Username to connect to database
     * @param PASSWORD The password to connect to database
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
     * @param mcp The properties of connection to database (url username and password)
     * @return The current instance of MapCatalog
     */
    public static MapCatalog init(MapCatalogProperties mcp) throws SQLException{
        String URL = mcp.getProperty(MapCatalogProperties.DATABASE_URL).toString();
        String user = mcp.getProperty(MapCatalogProperties.DATABASE_USER).toString();
        String password = mcp.getProperty(MapCatalogProperties.DATABASE_PASSWORD).toString();
        //Connection creation
        MapCatalog mc = new MapCatalog(URL, user, password);
        //Database initialization
        mc.executeSQL("ups.sql");
        //Verification of version
        int dbVersion = mc.getVersion();
        while(dbVersion!=VERSION){
            mc.updateVersion(dbVersion);
            dbVersion = mc.getVersion();
        }
        //Admin creation
        String[] attributes = {"email"};
        String[] values = {"admin@admin.com"};
        List<User> list = User.page(mc, attributes, values);
        if(list.isEmpty()){
            mc.executeSQL("populate.sql");
        }
        return mc;
    }

    /**
     * Executes a .sql file
     * @param file The name of the sql file (stocked in resources) to be executed
     **/
    void executeSQL(String file) throws SQLException{
        String s;
        StringBuilder sb = new StringBuilder();
        try{
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
        } catch(IOException e){
            e.printStackTrace(); //unreachable code
        }
    }

    /**
     * Sends a query to database that returns the workspace list, then write it in a xml file a the root of project.
     */
    public InputStream getWorkspaceList() throws SQLException, ParserConfigurationException, TransformerException {
        //get the list of workspace names from the database
        List<Workspace>  list = Workspace.page(this);
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element e ;
        Element e2 ;
        Element rootEle = dom.createElement("workspaces");

        //Create a node for each workspace, and add the name
        for (Workspace wor : list) {
            e = dom.createElement("workspace");
            e2 = dom.createElement("name");
            e2.appendChild(dom.createTextNode(wor.getId_workspace()));
            e.appendChild(e2);
            rootEle.appendChild(e);
        }
        dom.appendChild(rootEle);
        //transform the dom in XML

        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(dom);
        Result outputTarget = new StreamResult(outputStream);
        tr.transform(xmlSource, outputTarget);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * Queries the database for the list of context, then writes it into a XML file
     * @param id_workspace The workspace which
     */
    public InputStream getContextList(String id_workspace) throws SQLException, ParserConfigurationException, TransformerException {
        //get the ows from database
        String[] attributes = {"id_root"};
        String[] values = {id_workspace};
        List<OWSContext> list = OWSContext.page(this, attributes, values);
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element e;
        Element e2 ;
        Element rootEle = dom.createElement("contexts");

        //create a node for each ows
        for (OWSContext ows : list) {
            e = dom.createElement("context");
            e.setAttribute("id", ows.getId_owscontext());
            e.setAttribute("date", new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(ows.getDate()));
            e2 = dom.createElement("title");
            e2.setAttribute("xml:lang", "fr");
            e2.appendChild(dom.createTextNode(ows.getTitle()));
            e.appendChild(e2);
            rootEle.appendChild(e);
        }
        dom.appendChild(rootEle);

        //transforms into XML
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(dom);
        Result outputTarget = new StreamResult(outputStream);
        tr.transform(xmlSource, outputTarget);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * Parses a map context to get the title and the lang
     * @param content The map context
     * @return the title and the lang
     */
    public static String[] getTitleLang(InputStream content) throws SQLException, ParserConfigurationException, IOException, SAXException {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(content);
        doc.getDocumentElement().normalize();
        HashMap hm = XMLTools.getNameSpacesMap(doc);
        String namespace = XMLTools.getTitleNameSpace(hm);
        NodeList nodes = doc.getElementsByTagName(namespace+":Title");
        Element titleNode = (Element) nodes.item(0);
        String title = titleNode.getTextContent();
        String lang = titleNode.getAttribute("xml:lang");
        return (new String[]{title,lang});
    }

    /**
     * Hashes a string into a string SHA 256
     * @param toHash The string to hash
     * @return The hashed string
     * @throws NoSuchAlgorithmException
     */
    public static String hasher(String toHash) throws NoSuchAlgorithmException {
        //hashing the password
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(toHash.getBytes());
        byte byteData[] = md.digest();
        //convert the byte to hex format
        StringBuilder sb = new StringBuilder();
        for (byte aByteData : byteData) {
            sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /**
     * Returns the number of version of the database
     * @return The version of database
     */
    private int getVersion() throws SQLException{
        String query = "SELECT * FROM version";
        int version=0;
        PreparedStatement stmt = this.getConnection().prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            version = rs.getInt("version");
        }
        rs.close();
        stmt.close();
        return version;
    }

    /**
     *Updates the version of the database with SQL scripts
     * @param n the number of version that needs to be updated
     */
    private void updateVersion(int n) throws SQLException{
        this.executeSQL("update"+n+".sql");
    }

    public static void main(String[] args) throws Exception{
        System.out.println(hasher("thecakeisalie"));
        System.out.println(1==Integer.parseInt(null));
    }
}