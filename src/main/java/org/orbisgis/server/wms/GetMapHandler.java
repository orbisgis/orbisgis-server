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
import java.io.File;
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
import org.orbisgis.core.renderer.se.SeExceptions;
import org.orbisgis.core.renderer.se.Style;
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
                WMSResponse wmsResponse, File styleDirectory) throws WMSException {
                Double minX;
                Double minY;
                Double maxX;
                Double maxY;
                boolean isSld = false;
                
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
                
                
                Style the_style = null;
                //First case : Layers and Styles are given with shp and se file names
                if (layerList != null && layerList.size() > 0) {
                        int i;
                        // Reverse order make the first layer been rendered in the last
                        try {
                                for (i = 0; i < layerList.size(); i++) {
                                        //Create the Ilayer with given layer name
                                        String layer = layerList.get(i);
                                        ILayer Il = dataManager.createLayer(layer);

                                        //then adding the Ilayer to the layers to render list
                                        layers.addLayer(Il);
                                }
                        } catch (LayerException e) {
                                throw new WMSException(e);
                        }
                        
                } else // Changing the sld String object to a Style type object
                if (stringSLD != null) {
                        try {
                                isSld = true;
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
                        
                        if (styleList != null) {
                                int j;
                                //Adding the style to the Ilayer
                                for (j = 0; j < styleList.size(); j++) {
                                        if (j < layerList.size()) {
                                                String style = styleList.get(j);
                                                try {
                                                        the_style = new Style(layers.getChildren()[j], new File(styleDirectory, style + ".se").getAbsolutePath());
                                                        layers.getChildren()[j].setStyle(0, the_style);
                                                        
                                                } catch (SeExceptions.InvalidStyle ex) {
                                                        throw new WMSException(ex);
                                                }
                                        }
                                }
                        }
                        if (isSld) {
                                try {
                                        SLD sld = new SLD(stringSLD);
                                        for (int i = 0; i < sld.size(); i++) {
                                                the_style = sld.getLayer(i).getStyle(0);
                                                layers.getChildren()[i].setStyle(0, the_style);
                                                
                                        }
                                } catch (URISyntaxException ex) {
                                        throw new WMSException(ex);
                                }
                        }
                        
                        
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
                        renderer.draw(mt, g2, width, height, layers, pm);
                        
                        g2.dispose();
                        MapImageWriter.write(wmsResponse, output, imageFormat, img, pixelSize);
                        
                } catch (Exception ex) {
                        ex.printStackTrace(new PrintWriter(output));
                        wmsResponse.setContentType("text/plain");
                } finally {
                        try {
                                layers.close();
                        } catch (LayerException ex1) {
                                throw new WMSException(ex1);
                        }
                }





                // Write img
        }
        
        public static void getMapUrlParser(String queryString, OutputStream output, WMSResponse wmsResponse, File styleDirectory) throws WMSException {
                
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
                
                getMap(layerList, styleList, crs, bbox, width, height, pixelSize, imageFormat, transparent, bgColor, sld, exceptionsFormat, output, wmsResponse, styleDirectory);
        }
        
        public static void getMapXmlParser(String queryString, PrintWriter print) {
        }
        
        private GetMapHandler() {
        }
}
