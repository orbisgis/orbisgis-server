package controllers;

/**
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

import org.xml.sax.SAXException;
import play.mvc.*;
import org.orbisgis.server.mapcatalog.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import csp.ContentSecurityPolicy;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Class for REST APi, this will changes when the API will be rewritten with database
 */
@ContentSecurityPolicy
public class MapCatalogAPI extends Controller {
    private static MapCatalog MC = MapCatalogC.getMapCatalog();

    /**
     * Returns a context
     * @param workspace of the workspace, not used
     * @param id the id of the context
     * @return bad request status if error, the xml context else
     */
    public static Result getContext(String workspace, String id){
        try {
            String[] attributes = {"id_owscontext"};
            String[] values = {id};
            List<OWSContext> list = OWSContext.page(MC, attributes, values);
            if(list.isEmpty()){
                return badRequest();
            }else{
                return ok(list.get(0).getContent(MC));
            }
        } catch (SQLException e) {
            return badRequest(e.getMessage());
        }
    }

    /**
     * Deletes a context
     * @param workspace id of the workspace, not used, just here for rest
     * @param id the id of the context
     * @return no content if success, bad request else
     */
    public static Result deleteContext(String workspace, String id){
        try {
            OWSContext.delete(MC, Long.valueOf(id));
            return noContent();
        } catch (SQLException e) {
            return badRequest(e.getMessage());
        }
    }

    /**
     * Get the list of workspaces
     * @return The XML list of workspaces, or bad request if error
     */
    public static Result listWorkspaces(){
        try {
            return ok(MC.getWorkspaceListWrite()); //only workspaces with write access
        } catch (SQLException e) {
            return badRequest(e.getMessage());
        } catch (ParserConfigurationException e) {
            return badRequest(e.getMessage());
        } catch (TransformerException e) {
            return badRequest(e.getMessage());
        }
    }

    /**
     * Get the context list of a workspace
     * @param id_workspace the id of the workspace
     * @return the list of the contexts in a workspace (xml) or bad request if error
     */
    public static Result listContexts(String id_workspace){
        try {
            return ok(MC.getContextList(id_workspace));
        } catch (SQLException e) {
            return badRequest(e.getMessage());
        } catch (ParserConfigurationException e) {
            return badRequest(e.getMessage());
        } catch (TransformerException e) {
            return badRequest(e.getMessage());
        }
    }

    /**
     * adds a context with root as parent
     * @param id_workspace the root workspace
     * @return bad request if errors, XML validation else (created)
     */
    @BodyParser.Of(BodyParser.Xml.class)
    public static Result addContextFromRoot(String id_workspace){
       try {
           Http.RequestBody body = request().body();
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           Source xmlSource = new DOMSource(body.asXml());
           try{
               StreamResult outputTarget = new StreamResult(outputStream);
               TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
           }catch(Exception e){
               return badRequest(e.getMessage());
           }
           //saving the ows
           InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
           OWSContext ows = new OWSContext(id_workspace, null, null, "");
           Long id_ows = ows.save(MC, is);
           //retrieving the title
           String[] attributes = {"id_owscontext"};
           String[] values = {id_ows.toString()};
           OWSContext updated = OWSContext.page(MC, attributes, values).get(0);
           String[] titleLang = MapCatalog.getTitleLang(updated.getContent(MC));
           //updating the ows with new title
           OWSContext toUpdate = new OWSContext(id_ows.toString(), id_workspace, null, null, titleLang[0], null);
           toUpdate.update(MC);
           //sending the response
           String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(updated.getDate());
           String answer = "<context id=\""+id_ows+"\" date=\""+date+"\">\n" +
                           "  <title>"+id_ows+"</title>\n" +
                           "</context>";
           return created(answer);
       } catch (SQLException e) {
           return badRequest(e.getMessage());
       } catch (ParserConfigurationException e) {
           return badRequest(e.getMessage());
       } catch (SAXException e) {
           return badRequest(e.getMessage());
       } catch (IOException e) {
           return badRequest(e.getMessage());
       }
   }

    /**
     * Adds a context with folder as parent
     * @param id_workspace the root workspace
     * @param id_folder the parent folder
     * @return bad request if errors, XML validation else (created)
     */
    @BodyParser.Of(BodyParser.Xml.class)
    public static Result addContextFromParent(String id_workspace, String id_folder){
        try {
            //processing the input
            Http.RequestBody body = request().body();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(body.asXml());
            try{
                StreamResult outputTarget = new StreamResult(outputStream);
                TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            }catch(Exception e){
                return badRequest(e.getMessage());
            }
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
            //saving the ows
            OWSContext ows = new OWSContext(id_workspace, id_folder, null, "");
            Long id_ows = ows.save(MC, is);
            //retrieving the title
            String[] attributes = {"id_owscontext"};
            String[] values = {id_ows.toString()};
            OWSContext updated = OWSContext.page(MC, attributes, values).get(0);
            String[] titleLang = MapCatalog.getTitleLang(updated.getContent(MC));
            //updating the ows with new title
            OWSContext toUpdate = new OWSContext(id_ows.toString(), id_workspace, id_folder, null, titleLang[0], null);
            toUpdate.update(MC);
            //sending the response
            String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(updated.getDate());
            String answer = "<context id=\""+id_ows+"\" date=\""+date+"\">\n" +
                    "  <title>"+id_ows+"</title>\n" +
                    "</context>";
            return created(answer);
        } catch (SQLException e) {
            return badRequest(e.getMessage());
        } catch (ParserConfigurationException e) {
            return badRequest(e.getMessage());
        } catch (SAXException e) {
            return badRequest(e.getMessage());
        } catch (IOException e) {
            return badRequest(e.getMessage());
        }
    }

    /**
     * Updates a context
     * @param id_root Root workspace of the context
     * @param id_owscontext the id of the context to update
     * @return  status ok with XML validation, or bad request if errors
     */
    @BodyParser.Of(BodyParser.Xml.class)
    public static Result updateContext(String id_root, String id_owscontext){
        try {
            //Processing the input
            Http.RequestBody body = request().body();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(body.asXml());
            try{
                StreamResult outputTarget = new StreamResult(outputStream);
                TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            }catch(Exception e){
                return badRequest(e.getMessage());
            }
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
            //looking for the current ows
            String[] attributes = {"id_owscontext"};
            String[] values = {id_owscontext};
            OWSContext current = OWSContext.page(MC, attributes, values).get(0);
            //saving the new one
            OWSContext ows = new OWSContext(id_owscontext, current.getId_root(), current.getId_parent(), current.getId_uploader(), "", current.getDate());
            ows.update(MC, is);
            //retrieving the title
            OWSContext updated = OWSContext.page(MC, attributes, values).get(0);
            String[] titleLang = MapCatalog.getTitleLang(updated.getContent(MC));
            //updating the ows with new title
            OWSContext toUpdate = new OWSContext(id_owscontext, current.getId_root(), current.getId_parent(), current.getId_uploader(), titleLang[0], null);
            toUpdate.update(MC, ows.getContent(MC));
            //sending response
            String answer = "<context id=\""+id_owscontext+"\" date=\""+current.getDate()+"\">\n" +
                            "  <title>"+id_owscontext+"</title>\n" +
                            "</context>";
            return ok(answer);
        } catch (SQLException e) {
            return badRequest(e.getMessage());
        } catch (ParserConfigurationException e) {
            return badRequest(e.getMessage());
        } catch (SAXException e) {
            return badRequest(e.getMessage());
        } catch (IOException e) {
            return badRequest(e.getMessage());
        }
    }
}
