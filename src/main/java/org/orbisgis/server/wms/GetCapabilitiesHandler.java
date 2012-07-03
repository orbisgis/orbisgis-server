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

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import net.opengis.wms.Capability;
import net.opengis.wms.ContactInformation;
import net.opengis.wms.DCPType;
import net.opengis.wms.Get;
import net.opengis.wms.HTTP;
import net.opengis.wms.Layer;
import net.opengis.wms.OnlineResource;
import net.opengis.wms.OperationType;
import net.opengis.wms.Request;
import net.opengis.wms.Service;
import net.opengis.wms.WMSCapabilities;
import org.gdms.source.SourceManager;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;

/**
 *
 * @author Tony MARTIN
 */
public class GetCapabilitiesHandler {

        static void getCap(String queryString, OutputStream output, WMSResponse wmsResponse, File styleDirectory) {
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
                SourceManager sm = Services.getService(DataManager.class).getSourceManager();
                String[] names = sm.getSourceNames();

                for (int i = 0; i < names.length; i++) {
                        if (!sm.getSource(names[i]).isSystemTableSource()) {
                                Layer layer = new Layer();
                                layer.setName(names[i]);
                                layer.setTitle(names[i]);

                                availableLayers.getLayer().add(layer);
                        }
                }
                c.setLayer(availableLayers);

                //setting the request capabilities
                Request req = new Request();
                OperationType opMap = new OperationType();
                opMap.getFormat().add("image/jpeg");
                opMap.getFormat().add("image/png");
                OnlineResource oRMap = new OnlineResource();
                oRMap.setHref(wmsResponse.getRequestUrl());
                System.out.println(wmsResponse.getRequestUrl());
                oRMap.setTitle("GetMap");
                Get get = new Get();
                get.setOnlineResource(oRMap);
                HTTP http = new HTTP();
                http.setGet(get);
                DCPType dcpType = new DCPType();
                dcpType.setHTTP(http);
                opMap.getDCPType().add(dcpType);
                req.setGetMap(opMap);

                OperationType opCap = new OperationType();
                opCap.getFormat().add("text/xml");
                OnlineResource oRCap = new OnlineResource();
                oRCap.setHref(wmsResponse.getRequestUrl());
                oRCap.setTitle("GetCapabilities");
                Get getCap = new Get();
                getCap.setOnlineResource(oRCap);
                HTTP httpCap = new HTTP();
                httpCap.setGet(getCap);
                DCPType dcpTypeCap = new DCPType();
                dcpTypeCap.setHTTP(httpCap);
                opCap.getDCPType().add(dcpTypeCap);
                req.setGetCapabilities(opCap);

                c.setRequest(req);


                cap.setCapability(c);


                try {


                        JAXBContext jaxbContext = JAXBContext.newInstance("net.opengis.wms:net.opengis.sld._1_2:net.opengis.se._2_0.core:net.opengis.wms:oasis.names.tc.ciq.xsdschema.xal._2");
                        Marshaller marshaller = jaxbContext.createMarshaller();
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                        wmsResponse.setContentType("text/xml;charset=UTF-8");
                        marshaller.marshal(cap, out);

                } catch (Exception ex) {
                        wmsResponse.setContentType("text/html;charset=UTF-8");
                        out.print("<h1>something went wrong</h1>");
                        out.print(ex);
                        ex.printStackTrace(out);
                }
        }

        private GetCapabilitiesHandler() {
        }
}
