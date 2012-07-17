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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import org.orbisgis.core.context.main.MainContext;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.core.workspace.CoreWorkspace;

/**
 *
 * @author Tony MARTIN
 */
public final class WMS {

        private MainContext context;
        private Map<String, Style> serverStyles;

        /**
         * Initialize the context (containing datasources, datamanager...)
         *
         * @param coreWorkspace
         * @param serverStyles
         */
        public void init(CoreWorkspace coreWorkspace, Map<String, Style> serverStyles) {

                context = new MainContext(false, coreWorkspace);
                this.serverStyles = serverStyles;
        }

        /**
         * Free ressources
         */
        public void destroy() {
                getContext().dispose();
        }

        /**
         * Handles the URL request and reads the request type to start
         * processing of either a getMap or getCapabilities request
         *
         * @param queryParameters
         * @param output
         * @param wmsResponse
         * @throws WMSException
         */
        public void processURL(Map<String, String[]> queryParameters, OutputStream output, WMSResponse wmsResponse) throws WMSException {


                //Spliting request parameters to determine the requestType to execute

                String service = "undefined";
                if (queryParameters.containsKey("SERVICE")) {
                        service = queryParameters.get("SERVICE")[0];
                }
                if (!service.equalsIgnoreCase("wms")) {
                        exceptionDescription(wmsResponse, output, "The service specified is either unsupported or wrongly requested. Please specify WMS service as it is the only one supported by this server");
                        return;
                }
                
                String version = "undefined";
                if (queryParameters.containsKey("VERSION")) {
                        version = queryParameters.get("VERSION")[0];
                }
                
                String requestType = "undefined";
                if (queryParameters.containsKey("REQUEST")) {
                        requestType = queryParameters.get("REQUEST")[0];
                }



                // In case of a GetMap request, the WMS version is checked as recomended by the standard. If 
                // a wrong version is selected, an error is sent and the user is asked for a supported version
                if (requestType.equalsIgnoreCase("getmap")) {
                        if (!(version.equalsIgnoreCase("1.3.0") || version.equalsIgnoreCase("1.3"))) {
                                exceptionDescription(wmsResponse, output, "The version number is incorrect or unspecified. Please specify 1.3 version number as it is the only supported by this server. ");
                                return;
                        }
                        GetMapHandler.getMapUrlParser(queryParameters, output, wmsResponse, this.serverStyles);

                } else if (requestType.equalsIgnoreCase("getcapabilities")) {

                        GetCapabilitiesHandler.getCap(output, wmsResponse);

                } else {
                        exceptionDescription(wmsResponse, output, "The requested request type is not supported or wrongly specified. Please specify either getMap or getCapabilities request as it they are the only two supported by this server. ");
                }
        }

        public void processXML(InputStream postStream, OutputStream printStream, WMSResponse wmsResponse) {
        }

        /**
         * Class constructor
         *
         */
        public WMS() {
        }

        /**
         * Returns the context so it can be disposed
         *
         * @return the context
         */
        public MainContext getContext() {
                return context;
        }

        /**
         * Generates the error message in case of an exception created by a bad
         * client request
         *
         * @param wmsResponse
         * @param output
         * @param errorMessage
         */
        public static void exceptionDescription(WMSResponse wmsResponse, OutputStream output, String errorMessage) {
                PrintWriter out = new PrintWriter(output);
                wmsResponse.setContentType("text/xml;charset=UTF-8");
                wmsResponse.setResponseCode(400);
                out.print("<?xml version='1.0' encoding=\"UTF-8\"?><ServiceExceptionReport xmlns=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.3.0\" xsi:schemaLocation=\"http://www.opengis.net/ogc http://schemas.opengis.net/wms/1.3.0/exceptions_1_3_0.xsd\"><ServiceException>"
                        + errorMessage + "</ServiceException></ServiceExceptionReport>");
                out.flush();
        }
}
