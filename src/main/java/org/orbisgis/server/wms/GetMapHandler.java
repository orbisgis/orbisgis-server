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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
 * This object contains all the methods that are used to handle the getMap
 * request and give the correct parameters to the renderer
 *
 * @author maxence, Tony MARTIN
 */
public final class GetMapHandler {

        /**
         * Receives all the getMap request parameters from getMapUrlParser and
         * turns them into acceptable objects for the renderer to process, then
         * writes the rendrer image into the output stream via MapImageWriter
         *
         * @param layerList contains the names of requested layers
         * @param styleList contains the names of the desired se files (must be
         * equal or shorter than layerList)
         * @param crs desired CRS (string)
         * @param bbox geographic extent, given in the correct CRS
         * @param width pixel with of the image
         * @param height pixel height of the image
         * @param pixelSize used to calculate the dpi resolution desired for the
         * image
         * @param imageFormat chosen between the image format server
         * capabilities
         * @param transparent boolean that determines whether the background is
         * visible or not (only works on png outputs)
         * @param bgColor
         * @param stringSLD used if the layers and styles are defined in a SLD
         * file given by its URI rather than layers and se styles files present
         * on the server
         * @param exceptionsFormat
         * @param output
         * @param wmsResponse
         * @param serverStyles
         * @throws WMSException
         */
        public static void getMap(List<String> layerList, List<String> styleList, String crs,
                List<Double> bbox, int width, int height, double pixelSize, String imageFormat,
                boolean transparent, String bgColor, String stringSLD, String exceptionsFormat, OutputStream output,
                WMSResponse wmsResponse, Map<String, Style> serverStyles) throws WMSException {
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

                Double dpi = 25.4 / pixelSize;

                DataManager dataManager = Services.getService(DataManager.class);

                LayerCollection layers = new LayerCollection("Map");

                SLD sld = null;

                //First case : Layers and Styles are given with shp and se file names
                if (layerList != null && layerList.size() > 0) {
                        int i;
                        // Reverse order make the first layer been rendered in the last
                        try {
                                for (i = 0; i < layerList.size(); i++) {
                                        //Create the Ilayer with given layer name
                                        String layer = layerList.get(i);
                                        ILayer iLayer = dataManager.createLayer(layer);

                                        //then adding the Ilayer to the layers to render list
                                        layers.addLayer(iLayer);
                                }
                        } catch (LayerException e) {
                                WMS.exceptionDescription(wmsResponse, output, "At least one of the chosen layer is invalid. Make sure of the available layers by requesting the server capabilities.");
                                return;
                        }

                } else // Changing the sld String object to a Style type object
                if (stringSLD != null) {
                        try {
                                sld = new SLD(stringSLD);
                                for (int i = 0; i < sld.size(); i++) {
                                        try {
                                                layers.addLayer(sld.getLayer(i));
                                        } catch (LayerException ex) {
                                                WMS.exceptionDescription(wmsResponse, output, "At least one of the chosen layer is invalid. Make sure of the available layers by requesting the server capabilities.");
                                                return;
                                        } catch (SeExceptions.InvalidStyle ex) {
                                                WMS.exceptionDescription(wmsResponse, output, "The se style is invalid. Please give a SE valid SLD file.");
                                                return;
                                        }
                                }
                        } catch (URISyntaxException ex) {
                                WMS.exceptionDescription(wmsResponse, output, "The SLD URI is invalid. Please enter a valid SLD file URI path.");
                                return;
                        }
                }

                BufferedImage img;

                try {
                        layers.open();

                        //After opening the layers, we can add the styles to each layer
                        if (layerList != null) {
                                int j;
                                //In case of using the server's styles
                                for (j = 0; j < layerList.size(); j++) {
                                        if (j < styleList.size()) {
                                                String styleString = styleList.get(j);
                                                if (serverStyles.containsKey(styleString)) {
                                                        Style style = serverStyles.get(styleString);
                                                        layers.getChildren()[j].setStyle(0, style);
                                                } else {
                                                        WMS.exceptionDescription(wmsResponse, output, "One of the requested SE styles doesn't exist on this server. Please look for an existing style in the server extended capabilities.");
                                                        return;
                                                }

                                        } else //we add a server default style associated with the layer 
                                        {
                                                String styleString = layers.getLayer(j).getName();
                                                if (serverStyles.containsKey(styleString)) {
                                                        Style style = serverStyles.get(styleString);
                                                        layers.getChildren()[j].setStyle(0, style);
                                                }
                                        }
                                }
                        } else if (stringSLD != null) {

                                //In case of an external sld
                                try {
                                        for (int i = 0; i < sld.size(); i++) {
                                                Style theStyle = sld.getLayer(i).getStyle(0);
                                                layers.getChildren()[i].setStyle(0, theStyle);
                                        }
                                } catch (SeExceptions.InvalidStyle ex) {
                                        WMS.exceptionDescription(wmsResponse, output, "The se style is invalid. Please give a SE valid style in the SLD file..");
                                        return;
                                }
                        }


                        //Setting the envelope according to given bounding box
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

                        if (ImageFormats.JPEG.toString().equals(imageFormat)) {
                                imgType = BufferedImage.TYPE_3BYTE_BGR;
                        }

                        img = new BufferedImage(width, height, imgType);

                        mt.setDpi(dpi);
                        mt.setImage(img);
                        mt.setExtent(env);

                        Graphics2D g2 = img.createGraphics();

                        Color color;
                        if (transparent) {
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

                } catch (IOException ex) {
                        ex.printStackTrace(new PrintWriter(output));
                        wmsResponse.setContentType("text/plain");
                } catch (LayerException lEx) {
                        throw new WMSException(lEx);
                } finally {
                        try {
                                layers.close();
                        } catch (LayerException ex1) {
                                throw new WMSException(ex1);
                        }
                }
        }

        /**
         * Parses the url into the getMap request parameters and gives them to
         * the getMap method
         *
         * @param queryString
         * @param output
         * @param wmsResponse
         * @param serverStyles
         * @throws WMSException
         */
        public static void getMapUrlParser(String queryString, OutputStream output, WMSResponse wmsResponse, Map<String, Style> serverStyles) throws WMSException {

                List<String> layerList = new ArrayList<String>();
                List<String> styleList = new ArrayList<String>();
                String crs = null;
                List<Double> bbox = new ArrayList<Double>();
                Integer width = null;
                Integer height = null;
                Double pixelSize = 0.084;
                String imageFormat = ImageFormats.PNG.toString();
                boolean transparent = false;
                String bgColor = "#FFFFFF";
                String sld = null;
                String exceptionsFormat = null;

                for (String parameter : queryString.split("&")) {
                        String[] paramValues = parameter.split("=");

                        if (paramValues[0].equalsIgnoreCase("layers") && (paramValues.length > 1)) {
                                layerList.addAll(Arrays.asList(paramValues[1].split(",")));
                        }

                        if (paramValues[0].equalsIgnoreCase("styles") && (paramValues.length > 1)) {
                                styleList.addAll(Arrays.asList(paramValues[1].split(",")));
                        }

                        if (paramValues[0].equalsIgnoreCase("crs")) {
                                crs = paramValues[1];
                        }

                        if (paramValues[0].equalsIgnoreCase("bbox") && (paramValues.length > 1)) {
                                for (String coord : paramValues[1].split(",")) {
                                        bbox.add(Double.parseDouble(coord));
                                }
                        }

                        if (paramValues[0].equalsIgnoreCase("width") && (paramValues.length > 1)) {
                                width = Integer.parseInt(paramValues[1]);
                        }
                        if (paramValues[0].equalsIgnoreCase("height") && (paramValues.length > 1)) {
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

                getMap(layerList, styleList, crs, bbox, width, height, pixelSize, imageFormat, transparent, bgColor, sld, exceptionsFormat, output, wmsResponse, serverStyles);
        }

        public static void getMapXmlParser(String queryString, PrintWriter print) {
        }

        private GetMapHandler() {
        }
}
