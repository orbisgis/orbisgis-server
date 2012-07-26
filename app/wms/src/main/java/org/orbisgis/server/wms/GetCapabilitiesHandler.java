/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.


 * Team leader : Erwan Bocher, scientific researcher,

 * User support leader : Gwendall Petit, geomatic engineer.

 * Previous computer developer : Pierre-Yves FADET, computer engineer, Thomas LEDUC,
 * scientific researcher, Fernando GONZALEZ CORTES, computer engineer.

 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC

 * Copyright (C) 2010 Erwan BOCHER, Alexis GUEGANNO, Maxence LAURENT, Antoine GOURLAY

 * Copyright (C) 2012 Erwan BOCHER, Antoine GOURLAY

 * This file is part of OrbisGIS.

 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.

 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.

 * For more information, please consult: <http://www.orbisgis.org/>

 * or contact directly:
 * info@orbisgis.org
 */
package org.orbisgis.server.wms;

import com.vividsolutions.jts.geom.Envelope;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import net.opengis.wms.BoundingBox;
import net.opengis.wms.Capability;
import net.opengis.wms.ContactInformation;
import net.opengis.wms.DCPType;
import net.opengis.wms.Get;
import net.opengis.wms.HTTP;
import net.opengis.wms.Layer;
import net.opengis.wms.OnlineResource;
import net.opengis.wms.OperationType;
import net.opengis.wms.Post;
import net.opengis.wms.Request;
import net.opengis.wms.Service;
import net.opengis.wms.Style;
import net.opengis.wms.WMSCapabilities;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.NoSuchTableException;
import org.gdms.driver.DriverException;
import org.gdms.source.SourceEvent;
import org.gdms.source.SourceListener;
import org.gdms.source.SourceManager;
import org.gdms.source.SourceRemovalEvent;
import org.jproj.CoordinateReferenceSystem;
import org.jproj.Registry;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import scala.actors.threadpool.Arrays;

/**
 * Creates the answer to a getCapabilities request and writes it into the output
 * stream
 *
 * @author Tony MARTIN
 */
public final class GetCapabilitiesHandler {

        private Map<String, Layer> layerMap = new HashMap<String, Layer>();
        private Map<String, String[]> layerStyles;
        private Map<String, Style> serverStyles;
        private final JAXBContext jaxbContext;
        private List<String> authCRS;

        /**
         * Handles the getCapabilities request and gives the XML formated server
         * capabilities to the outputStream
         *
         * @param output servlet outputStream
         * @param wmsResponse HttpServletResponse modified for WMS use
         */
        void getCap(OutputStream output, WMSResponse wmsResponse) throws WMSException {
                PrintWriter out = new PrintWriter(output);
                WMSCapabilities cap = new WMSCapabilities();


                //Setting service WMS metadata
                Service s = new Service();
                s.setName("WMS");
                s.setTitle("WMS Service for OrbisWMS");

                OnlineResource oR = new OnlineResource();
                oR.setHref("http://www.orbisgis.org");
                oR.setTitle("OrbisGIS Website");
                s.setOnlineResource(oR);

                ContactInformation cI = new ContactInformation();
                cI.setContactElectronicMailAddress("info@orbisgis.org");
                s.setContactInformation(cI);

                cap.setService(s);

                //Setting Capability parameters

                //Setting Layers capabilities
                Capability c = new Capability();
                Layer availableLayers = new Layer();
                for (Layer e : layerMap.values()) {
                        availableLayers.getLayer().add(e);
                }
                //Server supported CRS


                availableLayers.getCRS().addAll(authCRS);

                availableLayers.setName("Server available layers");

                c.setLayer(availableLayers);

                //Setting the request capabilities

                //GetMap capabilities
                Request req = new Request();
                OperationType opMap = new OperationType();
                for (ImageFormats im : ImageFormats.values()) {
                        opMap.getFormat().add(im.toString());
                }
                OnlineResource oRMap = new OnlineResource();
                oRMap.setHref(wmsResponse.getRequestUrl());
                oRMap.setTitle("GetMap");
                Get get = new Get();
                get.setOnlineResource(oRMap);
                HTTP http = new HTTP();
                http.setGet(get);
                Post post = new Post();
                post.setOnlineResource(oRMap);
                http.setPost(post);
                DCPType dcpType = new DCPType();
                dcpType.setHTTP(http);
                opMap.getDCPType().add(dcpType);
                req.setGetMap(opMap);

                //GetCap capabilities
                OperationType opCap = new OperationType();
                opCap.getFormat().add("text/xml");
                OnlineResource oRCap = new OnlineResource();
                oRCap.setHref(wmsResponse.getRequestUrl());
                oRCap.setTitle("GetCapabilities");
                Get getCap = new Get();
                getCap.setOnlineResource(oRCap);
                HTTP httpCap = new HTTP();
                httpCap.setGet(getCap);
                Post postCap = new Post();
                postCap.setOnlineResource(oRMap);
                http.setPost(postCap);
                DCPType dcpTypeCap = new DCPType();
                dcpTypeCap.setHTTP(httpCap);
                opCap.getDCPType().add(dcpTypeCap);
                req.setGetCapabilities(opCap);

                //GetFeatureinfo capabilities
                OperationType opFeature = new OperationType();
                opFeature.getFormat().add("text/xml");
                OnlineResource oRFeature = new OnlineResource();
                oRFeature.setHref(wmsResponse.getRequestUrl());
                oRFeature.setTitle("GetFeatureInfo");
                Get getFeature = new Get();
                getFeature.setOnlineResource(oRFeature);
                HTTP httpFeature = new HTTP();
                httpFeature.setGet(getFeature);
                Post postFeature = new Post();
                postFeature.setOnlineResource(oRMap);
                http.setPost(postFeature);
                DCPType dcpTypeFeature = new DCPType();
                dcpTypeFeature.setHTTP(httpFeature);
                opFeature.getDCPType().add(dcpTypeFeature);
                req.setGetFeatureInfo(opFeature);


                c.setRequest(req);


                cap.setCapability(c);

                try {
                        //Marshalling the WMS Capabilities into an XML response
                        Marshaller marshaller = jaxbContext.createMarshaller();
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                        wmsResponse.setContentType("text/xml;charset=UTF-8");
                        marshaller.marshal(cap, out);

                } catch (Exception ex) {
                        wmsResponse.setContentType("text/xml;charset=UTF-8");
                        wmsResponse.setResponseCode(500);
                        out.print("<?xml version='1.0' encoding=\"UTF-8\"?><ServiceExceptionReport xmlns=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.3.0\" xsi:schemaLocation=\"http://www.opengis.net/ogc http://schemas.opengis.net/wms/1.3.0/exceptions_1_3_0.xsd\"><ServiceException>Something went wrong</ServiceException></ServiceExceptionReport>");
                        out.print(ex);
                        ex.printStackTrace(out);
                }
        }

        GetCapabilitiesHandler(Map<String, Layer> lMap, Map<String, String[]> lS) {
                layerMap = lMap;
                layerStyles = lS;
                try {
                        jaxbContext = JAXBContext.newInstance("net.opengis.wms:net.opengis.sld._1_2:net.opengis.se._2_0.core:net.opengis.wms:oasis.names.tc.ciq.xsdschema.xal._2");
                } catch (JAXBException ex) {
                        throw new RuntimeException(ex);
                }

                String[] codes = Registry.getAvailableCodes("EPSG", true);
                authCRS = Arrays.asList(codes);

                final DataSourceFactory dsf = Services.getService(DataManager.class).getDataSourceFactory();
                final SourceManager sm = dsf.getSourceManager();

                SourceListener sourceListener = new SourceListener() {

                        @Override
                        public void sourceAdded(SourceEvent e) {
                                String name = e.getName();
                                if (e.isWellKnownName() && !sm.getSource(name).isSystemTableSource()) {

                                        if (!layerMap.containsKey(name)) {
                                                try {
                                                        Layer layer = new Layer();
                                                        layer.setName(name);
                                                        layer.setTitle(name);

                                                        //Setting the bouding box data
                                                        DataSource ds = dsf.getDataSource(name);
                                                        ds.open();
                                                        Envelope env = ds.getFullExtent();
                                                        CoordinateReferenceSystem crs = ds.getCRS();
                                                        ds.close();
                                                        BoundingBox bBox = new BoundingBox();
                                                        if (crs != null) {
                                                                int epsgCode = crs.getEPSGCode();
                                                                if (epsgCode != -1) {
                                                                        bBox.setCRS("EPSG:" + epsgCode);
                                                                        layer.getCRS().add("EPSG:" + epsgCode);
                                                                } else {
                                                                        return;
                                                                }
                                                        } else {
                                                                return;
                                                        }
                                                        bBox.setMaxx(env.getMaxX());
                                                        bBox.setMinx(env.getMinX());
                                                        bBox.setMiny(env.getMinY());
                                                        bBox.setMaxy(env.getMaxY());
                                                        layer.getBoundingBox().add(bBox);
                                                        layer.setQueryable(true);
                                                        if (layerStyles.containsKey(name)) {
                                                                for (int i = 0; i < layerStyles.get(name).length; i++) {
                                                                        Style style = new Style();
                                                                        String styleName = layerStyles.get(name)[i];
                                                                        style.setName(styleName);
                                                                        style.setTitle(styleName);
                                                                        layer.getStyle().add(style);
                                                                }
                                                        }
                                                        layerMap.put(name, layer);
                                                } catch (NoSuchTableException ex) {
                                                } catch (DataSourceCreationException ex) {
                                                } catch (DriverException ex) {
                                                }
                                        }
                                }
                        }

                        @Override
                        public void sourceRemoved(SourceRemovalEvent e) {
                                String name = e.getName();
                                layerMap.remove(name);
                        }

                        @Override
                        public void sourceNameChanged(SourceEvent e) {
                                String name = e.getName();
                                String newName = e.getNewName();

                                if (!sm.getSource(name).isSystemTableSource()) {
                                        if (layerMap.containsKey(name)) {
                                                layerMap.put(newName, layerMap.remove(name));
                                        }
                                }
                        }
                };

                String[] layerNames = sm.getSourceNames();
                for (int i = 0; i < layerNames.length; i++) {
                        SourceEvent sEvent = new SourceEvent(layerNames[i], sm.getSource(layerNames[i]).isWellKnownName(), sm);
                        sourceListener.sourceAdded(sEvent);
                }

                sm.addSourceListener(sourceListener);

        }
}
