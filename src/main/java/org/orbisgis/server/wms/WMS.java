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
import org.orbisgis.core.context.main.MainContext;

/**
 *
 * @author Tony MARTIN
 */
public final class WMS {

        private MainContext context;
        private File styleDirectory;

        public void init() {
                context = new MainContext(false);
        }

        public void destroy() {
                context.dispose();
        }

        void processURL(String queryString, OutputStream output, WMSResponse wmsResponse) throws WMSException {
                String service = "undefined";
                String version = "undefined";
                String requestType = "undefined";

                for (String parameter : queryString.split("&")) {
                        if (parameter.matches("(?i:service=.*)")) {
                                String param[] = parameter.split("=");
                                if (param.length > 1) {
                                        service = param[1];
                                }
                                if (!service.equalsIgnoreCase("wms")) {
                                        PrintWriter out = new PrintWriter(output);
                                        wmsResponse.setContentType("text/html;charset=UTF-8");
                                        wmsResponse.setResponseCode(400);
                                        out.print("<h2>The service specified is either unsupported or wrongly requested</h2>"
                                                + "<p>Please specify WMS service as it is the only one supported by this server</p>");
                                        out.flush();
                                        return;
                                }
                        }
                        if (parameter.matches("(?i:version=.*)")) {
                                String param[] = parameter.split("=");
                                if (param.length > 1) {
                                        version = param[1];
                                }
                                if (!version.equalsIgnoreCase("1.3.0") && !version.equalsIgnoreCase("1.3")) {
                                        PrintWriter out = new PrintWriter(output);
                                        wmsResponse.setContentType("text/html;charset=UTF-8");
                                        wmsResponse.setResponseCode(400);
                                        out.print("<h2>The version number is incorrect or unspecified</h2>"
                                                + "<p>Please specify 1.3 version number as it is the only supported by this server</p>");
                                        out.flush();
                                        return;
                                }
                        }
                        if (parameter.matches("(?i:request=.*)")) {
                                String param[] = parameter.split("=");
                                if (param.length > 1) {
                                        requestType = param[1];
                                }
                                if (requestType.equalsIgnoreCase("getmap")) {

                                        GetMapHandler.getMapUrlParser(queryString, output, wmsResponse, this.styleDirectory);

                                } else if (requestType.equalsIgnoreCase("getcapabilities")) {

                                        GetCapabilitiesHandler.getCap(queryString, output, wmsResponse, this.styleDirectory);

                                } else {
                                        PrintWriter out = new PrintWriter(output);
                                        wmsResponse.setContentType("text/html;charset=UTF-8");
                                        wmsResponse.setResponseCode(400);
                                        out.print("<h2>The requested request is not supported or wrongly specified</h2>"
                                                + "<p>Please specify either getMap or getCapabilities request as it they are the only two supported by this server</p>");
                                        out.flush();
                                        return;
                                }
                        }


                }




        }

        void processXML(InputStream postStream, OutputStream printStream) {
        }

        public WMS(File styleDirectory) {
                this.styleDirectory = styleDirectory;
        }
}
