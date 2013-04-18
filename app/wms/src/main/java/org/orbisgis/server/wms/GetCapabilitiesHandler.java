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
package org.orbisgis.server.wms;

import com.vividsolutions.jts.geom.Envelope;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import net.opengis.wms.*;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.NoSuchTableException;
import org.gdms.driver.DriverException;
import org.gdms.source.SourceEvent;
import org.gdms.source.SourceListener;
import org.gdms.source.SourceManager;
import org.gdms.source.SourceRemovalEvent;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;

/**
 * Creates the answer to a getCapabilities request and writes it into the output
 * stream
 *
 * @author Tony MARTIN
 */
public final class GetCapabilitiesHandler {

    private final WMSProperties properties;
    private Map<String, Layer> layerMap = new HashMap<String, Layer>();
    private Map<String, String[]> layerStyles;
    private final JAXBContext jaxbContext;
    private List<String> authCRS;

        /**
         * Handles the getCapabilities request and gives the XML formated server
         * capabilities to the outputStream
         *
         * @param output servlet outputStream
         * @param wmsResponse HttpServletResponse modified for WMS use
         * @throws WMSException
         * @throws UnsupportedEncodingException
         */
        public void getCap(OutputStream output, WMSResponse wmsResponse) throws WMSException, UnsupportedEncodingException {
                PrintStream pr = new PrintStream(output, false, "UTF-8");
                WMSCapabilities cap = new WMSCapabilities();

                //Setting service WMS metadata
                cap.setService(getService());

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
                req.setGetMap(getMapOperation(wmsResponse));
                //GetCap capabilities
                req.setGetCapabilities(getCapOperation(wmsResponse));
                //GetFeatureInfo capabilities
                req.setGetFeatureInfo(getFeatureOperation(wmsResponse));


                c.setRequest(req);


                cap.setCapability(c);

                try {
                        //Marshalling the WMS Capabilities into an XML response
                        Marshaller marshaller = jaxbContext.createMarshaller();

                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                        wmsResponse.setContentType("text/xml;charset=UTF-8");
                        marshaller.marshal(cap, pr);

                } catch (JAXBException ex) {
                        wmsResponse.setContentType("text/xml;charset=UTF-8");
                        wmsResponse.setResponseCode(500);
                        pr.append("<?xml version='1.0' encoding=\"UTF-8\"?><ServiceExceptionReport xmlns=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.3.0\" xsi:schemaLocation=\"http://www.opengis.net/ogc http://schemas.opengis.net/wms/1.3.0/exceptions_1_3_0.xsd\"><ServiceException>Something went wrong</ServiceException></ServiceExceptionReport>");
                        pr.append(ex.toString());
                        ex.printStackTrace(pr);
                }
        }

    /**
     * Prepare the Service JAXB object in order to build the XML response.
     * @return A Service instance
     */
    private Service getService(){
        Service s = new Service();
        s.setName("WMS");
        s.setTitle((String)properties.getProperty(WMSProperties.TITLE));
        OnlineResource oR = new OnlineResource();
        oR.setHref((String)properties.getProperty(WMSProperties.RESOURCE_URL));
        oR.setTitle((String)properties.getProperty(WMSProperties.RESOURCE_NAME));
        s.setOnlineResource(oR);
        ContactInformation cI = new ContactInformation();
        cI.setContactElectronicMailAddress("info@orbisgis.org");
        s.setContactInformation(cI);
        return s;
    }

    private OperationType getFeatureOperation(WMSResponse wmsResponse) {
        OperationType opFeature = new OperationType();
        opFeature.getFormat().add("text/xml");
        //GET
        Get getFeature = new Get();
        getFeature.setOnlineResource(buildOnlineResource(wmsResponse,WMSProperties.FEATURE_GET, "GetFeatureInfo"));
        //POST
        Post postFeature = new Post();
        postFeature.setOnlineResource(buildOnlineResource(wmsResponse,WMSProperties.FEATURE_POST, "GetFeatureInfo"));
        //Both in HTTP
        HTTP httpFeature = new HTTP();
        httpFeature.setGet(getFeature);
        httpFeature.setPost(postFeature);
        DCPType dcpTypeFeature = new DCPType();
        dcpTypeFeature.setHTTP(httpFeature);
        opFeature.getDCPType().add(dcpTypeFeature);
        return opFeature;
    }


    /**
     * Gets the parameters of the GetCapabilities capability answer.
     * @return The operation type representing the GetCapabilities operation.
     */
    private OperationType getCapOperation(WMSResponse wmsResponse) {
        OperationType opCap = new OperationType();
        opCap.getFormat().add("text/xml");
        Get getCap = new Get();
        getCap.setOnlineResource(buildOnlineResource(wmsResponse,WMSProperties.CAP_GET, "GetCapabilities"));
        Post postCap = new Post();
        postCap.setOnlineResource(buildOnlineResource(wmsResponse,WMSProperties.CAP_POST, "GetCapabilities"));
        HTTP httpCap = new HTTP();
        httpCap.setGet(getCap);
        httpCap.setPost(postCap);
        DCPType dcpTypeCap = new DCPType();
        dcpTypeCap.setHTTP(httpCap);
        opCap.getDCPType().add(dcpTypeCap);
        return opCap;
    }

    /**
     * Gets the parameters of the GetMap capability answer.
     * @return The operation type representing the getMap operation.
     */
    private OperationType getMapOperation(WMSResponse wmsResponse) {
        OperationType opMap = new OperationType();
        for (ImageFormats im : ImageFormats.values()) {
            opMap.getFormat().add(im.toString());
        }
        Get get = new Get();
        get.setOnlineResource(buildOnlineResource(wmsResponse,WMSProperties.MAP_GET, "GetMap"));
        Post post = new Post();
        post.setOnlineResource(buildOnlineResource(wmsResponse,WMSProperties.MAP_POST, "GetMap"));
        //We feed the http object
        HTTP http = new HTTP();
        http.setGet(get);
        http.setPost(post);
        DCPType dcpType = new DCPType();
        dcpType.setHTTP(http);
        opMap.getDCPType().add(dcpType);
        return opMap;
    }

    private OnlineResource buildOnlineResource(WMSResponse wmsResponse, String key, String title){
        OnlineResource oRGet = new OnlineResource();
        String map = (String)properties.getProperty(key);
        if(map == null){
            oRGet.setHref(wmsResponse.getRequestUrl());
        } else {
            oRGet.setHref(map);
        }
        oRGet.setTitle(title);
        return oRGet;
    }

    GetCapabilitiesHandler(Map<String, Layer> lMap, Map<String, String[]> lS, WMSProperties props) {
                this.properties = props;
                layerMap = lMap;
                layerStyles = lS;
                try {
                        jaxbContext = JAXBContext.newInstance("net.opengis.wms:net.opengis.sld._1_2:net.opengis.se._2_0.core:oasis.names.tc.ciq.xsdschema.xal._2");
                } catch (JAXBException ex) {
                        throw new RuntimeException("Failed to build the JAXB Context, can't build the associated XML.",ex);
                }
                Set<String> codes = CRS.getSupportedCodes("EPSG");
                LinkedList<String> ll = new LinkedList<String>();
                for(String s : codes){
                        ll.add("EPSG:"+s);
                }
                authCRS = ll;

                final DataSourceFactory dsf = Services.getService(DataManager.class).getDataSourceFactory();
                final SourceManager sm = dsf.getSourceManager();

                SourceListener sourceListener = new SourceListener() {

                        @Override
                        public void sourceAdded(SourceEvent e) {
                                String name = e.getName();
                                if (e.isWellKnownName() && !sm.getSource(name).isSystemTableSource() && !layerMap.containsKey(name)) {
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
                                                        Integer code = null;
                                                        try {
                                                                code = CRS.lookupEpsgCode(crs, true);
                                                        } catch (FactoryException ex) {
                                                        }
                                                        if (code != null) {
                                                                bBox.setCRS("EPSG:" + code);
                                                                layer.getCRS().add("EPSG:" + code);
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
                                                        String[] lStyles = layerStyles.get(name);
                                                        for (int i = 0; i < lStyles.length; i++) {
                                                                Style style = new Style();
                                                                String styleName = lStyles[i];
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

                        @Override
                        public void sourceRemoved(SourceRemovalEvent e) {
                                String name = e.getName();
                                layerMap.remove(name);
                        }

                        @Override
                        public void sourceNameChanged(SourceEvent e) {
                                String name = e.getName();
                                String newName = e.getNewName();

                                if (sm.getSource(name) != null
                                                && !sm.getSource(name).isSystemTableSource()
                                                && layerMap.containsKey(name)) {
                                        layerMap.put(newName, layerMap.remove(name));
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
