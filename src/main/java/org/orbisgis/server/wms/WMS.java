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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.orbisgis.core.context.main.MainContext;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
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
         * @param workspacePath
         * @param serverStyles
         */
        public void init(String workspacePath, Map<String, Style> serverStyles) {
                CoreWorkspace c = new CoreWorkspace();
                c.setWorkspaceFolder(workspacePath);
                context = new MainContext(false, c);
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
         * @param queryString
         * @param output
         * @param wmsResponse
         * @throws WMSException
         */
        public void processURL(String queryString, OutputStream output, WMSResponse wmsResponse) throws WMSException {
                String service = "undefined";
                String version = "undefined";
                String requestType = "undefined";


                //Spliting request parameters to determine the requestType to execute

                for (String parameter : queryString.split("&")) {
                        if (parameter.matches("(?i:service=.*)")) {
                                String param[] = parameter.split("=");
                                if (param.length > 1) {
                                        service = param[1];
                                }
                                if (!service.equalsIgnoreCase("wms")) {
                                        exceptionDescription(wmsResponse, output, "<h2>The service specified is either unsupported or wrongly requested</h2><p>Please specify WMS service as it is the only one supported by this server</p>");
                                        return;
                                }
                        }
                        if (parameter.matches("(?i:version=.*)")) {
                                String param[] = parameter.split("=");
                                if (param.length > 1) {
                                        version = param[1];
                                }
                        }
                        if (parameter.matches("(?i:request=.*)")) {
                                String param[] = parameter.split("=");
                                if (param.length > 1) {
                                        requestType = param[1];
                                }
                        }


                }

                // In case of a GetMap request, the WMS version is checked as recomended by the standard. If 
                // a wrong version is selected
                if (requestType.equalsIgnoreCase("getmap")) {
                        if (!(version.equalsIgnoreCase("1.3.0") || version.equalsIgnoreCase("1.3"))) {
                                exceptionDescription(wmsResponse, output, "<h2>The version number is incorrect or unspecified</h2><p>Please specify 1.3 version number as it is the only supported by this server</p>");
                                return;
                        }
                        GetMapHandler.getMapUrlParser(queryString, output, wmsResponse, this.serverStyles);

                } else if (requestType.equalsIgnoreCase("getcapabilities")) {

                        GetCapabilitiesHandler.getCap(queryString, output, wmsResponse);

                } else {
                        exceptionDescription(wmsResponse, output, "<h2>The requested request type is not supported or wrongly specified</h2><p>Please specify either getMap or getCapabilities request as it they are the only two supported by this server</p>");
                }
        }

        public void processXML(InputStream postStream, OutputStream printStream) {
        }

        /**
         * Class constructor with styleDirectory path
         *
         * @param styleDirectory
         */
        public WMS() {
        }

        /**
         * @return the context
         */
        public MainContext getContext() {
                return context;
        }

        public static void exceptionDescription(WMSResponse wmsResponse, OutputStream output, String errorMessage) {
                PrintWriter out = new PrintWriter(output);
                wmsResponse.setContentType("text/html;charset=UTF-8");
                wmsResponse.setResponseCode(400);
                out.print(errorMessage);
                out.flush();
        }
}
