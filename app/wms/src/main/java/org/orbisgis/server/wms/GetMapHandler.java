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

import net.opengis.wms.Layer;
import org.apache.log4j.Logger;
import org.orbisgis.core.layerModel.LayerCollection;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.ImageRenderer;
import org.orbisgis.core.renderer.Renderer;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.progress.NullProgressMonitor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * This object contains all the methods that are used to handle the getMap
 * request and give the correct parameters to the renderer
 *
 * @author Maxence Laurent
 * @author Tony MARTIN
 * @author Alexis Guéganno
 */
public final class GetMapHandler extends AbstractGetHandler {
        private static final Logger LOGGER = Logger.getLogger(GetMapHandler.class);

        /**
         * Builds a new GetMapHandler with the given map of layers
         * @param inputMap
         */
        public GetMapHandler(Map<String, Layer> inputMap) {
            super(inputMap);
        }

        /**
         * Receives all the getMap request parameters from getMapParameterParser
         * and turns them into acceptable objects for the renderer to process,
         * then writes the rendered image into the output stream via
         * MapImageWriter
         *
         * @param layerList contains the names of requested layers
         * @param styleList contains the names of the desired se files (must be
         * equal or shorter than layerList)
         * @param crs desired CRS (string)
         * @param bBox geographic extent, given in the correct CRS
         * @param width pixel with of the image
         * @param height pixel height of the image
         * @param pixelSize used to calculate the dpi resolution desired for the
         * image
         * @param imageFormat chosen between the image format server
         * capabilities
         * @param transparent boolean that determines whether the background is
         * visible or not (only works on png outputs)
         * @param bgColor The background colour.
         * @param stringSLD used if the layers and styles are defined in a SLD
         * file given by its URI rather than layers and se styles files present
         * on the server
         * @param exceptionsFormat The format used to return Exceptions to the client
         * @param output The stream where to write in
         * @param wmsResponse The HTTP response that will be given by the server
         * @param serverStyles Styles registered in this server
         * @throws WMSException
         * @throws UnsupportedEncodingException  
         */
        public void getMap(String[] layerList, String[] styleList, String crs,
                double[] bBox, int width, int height, double pixelSize, String imageFormat,
                boolean transparent, String bgColor, String stringSLD, String exceptionsFormat, OutputStream output,
                WMSResponse wmsResponse, Map<String, Style> serverStyles) throws WMSException, UnsupportedEncodingException {

                LayerCollection layers = prepareLayers(layerList,styleList,crs,stringSLD,exceptionsFormat,output,wmsResponse,serverStyles);
                if(layers.getChildren().length == 0){
                    return;
                }
                try {
                    //Finally we can draw things...
                        MapTransform mt = getMapTransform(bBox, layers, imageFormat, width, height, pixelSize);
                        BufferedImage img = mt.getImage();
                        Graphics2D g2 = img.createGraphics();
                        Color color;
                        if (!transparent) {
                                color = Color.decode(bgColor);
                                g2.setBackground(color);
                                g2.clearRect(0, 0, width, height);
                        }
                        NullProgressMonitor pm = new NullProgressMonitor();
                        LOGGER.debug("Starting to draw the image");
                        Renderer renderer = new ImageRenderer();
                        LOGGER.trace("Renderer ready");
                        renderer.draw(mt, g2, width, height, layers, pm);
                        LOGGER.trace("Disposing of the graphics.");
                        g2.dispose();
                        LOGGER.debug("Image ready to be sent to the client.");
                        MapImageWriter.write(wmsResponse, output, imageFormat, img, pixelSize);
                } catch (Exception ex) {
                        LOGGER.debug("An error occurred while generating the image:\n",ex);
                        ex.printStackTrace(new PrintStream(output, false, "UTF-8"));
                        wmsResponse.setContentType("text/plain");
                } finally {
                        try {
                                layers.close();
                        } catch (LayerException ex1) {
                                LOGGER.debug("An error occurred while closing resources:\n",ex1);
                                throw new WMSException(ex1);
                        }
                }
        }

        /**
         * Parses the url into the getMap request parameters and gives them to
         * the getMap method
         *
         * @param queryParameters The original parameters set in the HTTP query.
         * @param output The stream we'll write in
         * @param wmsResponse The HTTP response qe have to feed
         * @param serverStyles The known SE Styles.
         * @throws WMSException
         */
        public void getMapParameterParser(Map<String, String[]> queryParameters, OutputStream output,
                        WMSResponse wmsResponse, Map<String, Style> serverStyles)
                        throws WMSException, UnsupportedEncodingException {

                String[] layerList = new String[0];
                String[] styleList = new String[0];
                double[] bbox;
                String crs;
                int width;
                int height;
                //Use a 72 dpi resolution by default
                double pixelSize = 0.35;
                String imageFormat = "undefined";
                boolean transparent = false;
                String bgColor = "#FFFFFF";
                String sld = null;
                String exceptionsFormat = null;

                if (queryParameters.containsKey("CRS")) {
                        crs = queryParameters.get("CRS")[0];
                } else {
                        WMS.exceptionDescription(wmsResponse, output, "No CRS has been declared");
                        return;
                }

                if (queryParameters.containsKey("BBOX")) {
                        String[] sbbox = queryParameters.get("BBOX")[0].split(",");
                        bbox = new double[sbbox.length];

                        for (int i = 0; i < bbox.length; i++) {
                                bbox[i] = Double.valueOf(sbbox[i]);
                        }
                } else {
                        WMS.exceptionDescription(wmsResponse, output, "You must specify a bounding box");
                        return;
                }

                if (queryParameters.containsKey("WIDTH")) {
                        width = Integer.valueOf(queryParameters.get("WIDTH")[0]);
                } else {
                        WMS.exceptionDescription(wmsResponse, output, "You must specify an image width.");
                        return;
                }

                if (queryParameters.containsKey("HEIGHT")) {
                        height = Integer.valueOf(queryParameters.get("HEIGHT")[0]);
                } else {
                        WMS.exceptionDescription(wmsResponse, output, "You must specify an image height.");
                        return;
                }

                if (queryParameters.containsKey("LAYERS")) {
                        layerList = queryParameters.get("LAYERS")[0].split(",");
                }

                if (queryParameters.containsKey("STYLES") && !queryParameters.get("STYLES")[0].isEmpty()) {
                        styleList = queryParameters.get("STYLES")[0].split(",");
                }

                if (queryParameters.containsKey("PIXELSIZE")) {
                        pixelSize = Double.valueOf(queryParameters.get("PIXELSIZE")[0]);
                }

                if (queryParameters.containsKey("FORMAT")) {
                        imageFormat = queryParameters.get("FORMAT")[0];
                }

                if (queryParameters.containsKey("TRANSPARENT")) {
                        transparent = Boolean.valueOf(queryParameters.get("TRANSPARENT")[0]);
                }

                if (queryParameters.containsKey("BGCOLOR")) {
                        bgColor = queryParameters.get("BGCOLOR")[0];
                }

                if (queryParameters.containsKey("SLD")) {
                        sld = queryParameters.get("SLD")[0];
                }

                if (queryParameters.containsKey("EXCEPTIONS")) {
                        exceptionsFormat = queryParameters.get("EXCEPTIONS")[0];
                }

                getMap(layerList, styleList, crs, bbox, width, height, pixelSize,
                        imageFormat, transparent, bgColor, sld, exceptionsFormat, output, wmsResponse, serverStyles);
        }


}
