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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.LayerCollection;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.ImageRenderer;
import org.orbisgis.core.renderer.Renderer;
import org.orbisgis.core.renderer.se.parameter.color.ColorHelper;
import org.orbisgis.progress.NullProgressMonitor;

/**
 *
 * @author maxence, Tony MARTIN
 */
public class GetMapHandler {

        public static void getMap(ArrayList<String> layerList, ArrayList<String> styleList, String crs,
                ArrayList<Double> bbox, int width, int height, double pixelSize, String imageFormat,
                boolean transparent, String bgColor, String stringSLD, String exceptionsFormat, OutputStream output,
                WMSResponse wmsResponse) throws WMSException {
                Double minX;
                Double minY;
                Double maxX;
                Double maxY;

                if (bbox.size() == 4) {
                        minX = bbox.get(0);
                        minY = bbox.get(1);
                        maxX = bbox.get(2);
                        maxY = bbox.get(3);
                } else {
                        minX = null;
                        minY = null;
                        maxX = null;
                        maxY = null;
                }



                boolean transparency = transparent;
                
                Double dpi = 25.4 / pixelSize;

                DataManager dataManager = Services.getService(DataManager.class);

                LayerCollection layers = new LayerCollection("Map");

                if (layerList != null && layerList.size() > 0) {
                        int i;
                        // Reverse order make the first layer been rendered in the last
                        try {
                                for (i = layerList.size() - 1; i >= 0; i--) {
                                        String layer = layerList.get(i);
                                        ILayer Il = dataManager.createLayer(layer);
                                        layers.addLayer(Il);
                                        String style = styleList.get(i);
                                }
                        } catch (LayerException e) {
                                throw new WMSException(e);
                        }

                } else // Changing the sld String object to a Style type object
                if (stringSLD != null) {



                        try {
                                SLD sld = new SLD(stringSLD);

                                for (int i = 0; i < sld.size(); i++) {
                                        try {
                                                layers.addLayer(sld.getLayer(i));
                                        } catch (LayerException ex) {
                                                throw new WMSException(ex);
                                        }
                                }
                        } catch (URISyntaxException ex) {
                                throw new WMSException(ex);
                        }
                }

                BufferedImage img;

                try {
                        layers.open();

                        Envelope env;

                        if (minX != null && minY != null && maxX != null && maxY != null) {
                                env = new Envelope(minX, maxX, minY, maxY);
                        } else {
                                env = layers.getEnvelope();
                        }

                        Renderer renderer = new ImageRenderer();
                        MapTransform mt = new MapTransform();

                        // WMS Ask to distort map CRS when envelope and dimension box (i.e. the map to genereate) differ
                        mt.setAdjustExtent(false);

                        int imgType = BufferedImage.TYPE_4BYTE_ABGR;

                        if ("image/jpeg".equals(imageFormat)) {
                                imgType = BufferedImage.TYPE_3BYTE_BGR;
                        }

                        img = new BufferedImage(width, height, imgType);

                        mt.setDpi(dpi);
                        mt.setImage(img);
                        mt.setExtent(env);

                        Graphics2D g2 = img.createGraphics();

                        Color color;
                        if (transparency) {
                                color = ColorHelper.getColorWithAlpha(Color.decode(bgColor), 0.0);
                        } else {
                                color = Color.decode(bgColor);
                        }

                        g2.setBackground(color);
                        g2.clearRect(0, 0, width, height);

                        NullProgressMonitor pm = new NullProgressMonitor();
                        //System.out.println("Render starts");
                        renderer.draw(mt, g2, width, height, layers, pm);
                        //System.out.println("Render ends");

                        //g2.setColor(Color.black);
                        //g2.drawString("DPI: " + dpi, 10, 10);


                        g2.dispose();

                        /*
                         * System.out.println("Layers:"); for (ILayer lay :
                         * layers.getChildren()) { System.out.println(" - " +
                         * lay.getName()); }
                         */

                        layers.close();

                        MapImageWriter.write(wmsResponse, output, imageFormat, img, pixelSize);
                } // ??
                catch (Exception ex) {
                        //ex.printStackTrace(System.out);
                        //System.out.flush();
                        try {
                                layers.close();
                        } catch (LayerException ex1) {
                                throw new WMSException(ex1);
                        }
                        img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                        Graphics2D g2 = img.createGraphics();
                        g2.drawString(ex.getMessage(), 10, 10);
                        g2.dispose();
                }





                // Write img
        }

        public static void getMapUrlParser(String queryString, OutputStream output, WMSResponse wmsResponse) throws WMSException {

                ArrayList<String> layerList = new ArrayList<String>();
                ArrayList<String> styleList = new ArrayList<String>();
                String crs = null;
                ArrayList<Double> bbox = new ArrayList<Double>();
                Integer width = null;
                Integer height = null;
                Double pixelSize = 0.028;
                String imageFormat = "image/png";
                boolean transparent = false;
                String bgColor = "#FFFFFF";
                String sld = null;
                String exceptionsFormat = null;

                for (String parameter : queryString.split("&")) {
                        String[] paramValues = parameter.split("=");

                        if (paramValues[0].equalsIgnoreCase("layers")) {
                                layerList.addAll(Arrays.asList(paramValues[1].split(",")));
                        }

                        if (paramValues[0].equalsIgnoreCase("styles")) {
                                styleList.addAll(Arrays.asList(paramValues[1].split(",")));
                        }

                        if (paramValues[0].equalsIgnoreCase("crs")) {
                                crs = paramValues[1];
                        }

                        if (paramValues[0].equalsIgnoreCase("bbox")) {
                                for (String coord : queryString.split(",")) {
                                        bbox.add(Double.parseDouble(coord));
                                }
                        }

                        if (paramValues[0].equalsIgnoreCase("width")) {
                                width = Integer.parseInt(paramValues[1]);
                        }

                        if (paramValues[0].equalsIgnoreCase("height")) {
                                height = Integer.parseInt(paramValues[1]);
                        }

                        if (paramValues[0].equalsIgnoreCase("pixelsize")) {
                                pixelSize = Double.parseDouble(paramValues[1]);
                        }

                        if (paramValues[0].equalsIgnoreCase("format")) {
                                imageFormat = paramValues[1];
                        }

                        if (paramValues[0].equalsIgnoreCase("transparent")) {
                                transparent = paramValues[1].equalsIgnoreCase("true") || paramValues[1].equalsIgnoreCase("1");
                        }

                        if (paramValues[0].equalsIgnoreCase("bgcolor")) {
                                bgColor = paramValues[1];
                        }

                        if (paramValues[0].equalsIgnoreCase("sld")) {
                                sld = paramValues[1];
                        }

                        if (paramValues[0].equalsIgnoreCase("exceptions")) {
                                exceptionsFormat = paramValues[1];
                        }
                }

                getMap(layerList, styleList, crs, bbox, width, height, pixelSize, imageFormat, transparent, bgColor, sld, exceptionsFormat, output, wmsResponse);
        }

        public static void getMapXmlParser(String queryString, PrintWriter print) {
        }

        private GetMapHandler() {
        }
}
