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
         * @param params The parameters of the request in a GetMapParameters instance.
         * @param output The stream where to write in
         * @param wmsResponse The HTTP response that will be given by the server
         * @param serverStyles Styles registered in this server
         * @throws WMSException
         * @throws UnsupportedEncodingException  
         */
        public synchronized void getMap(GetMapParameters params, OutputStream output,
                WMSResponse wmsResponse, Map<String, Style> serverStyles) throws WMSException, UnsupportedEncodingException {

                LayerCollection layers = prepareLayers(params.getLayerList(),params.getStyleList(),params.getCrs(),
                params.getSld(),params.getExceptionsFormat(),output,wmsResponse,serverStyles);
                if(layers.getChildren().length == 0){
                    return;
                }
                try {
                    //Finally we can draw things...
                        MapTransform mt = getMapTransform(params.getbBox(), layers, params.getImageFormat(), params.getWidth(),
                                params.getHeight(), params.getPixelSize());
                        BufferedImage img = mt.getImage();
                        Graphics2D g2 = img.createGraphics();
                        Color color;
                        if (!params.isTransparent()) {
                                color = Color.decode(params.getBgColor());
                                g2.setBackground(color);
                                g2.clearRect(0, 0, params.getWidth(), params.getHeight());
                        }
                        NullProgressMonitor pm = new NullProgressMonitor();
                        LOGGER.debug("Starting to draw the image");
                        Renderer renderer = new ImageRenderer();
                        LOGGER.trace("Renderer ready");
                        renderer.draw(mt, g2, params.getWidth(), params.getHeight(), layers, pm);
                        LOGGER.trace("Disposing of the graphics.");
                        g2.dispose();
                        LOGGER.debug("Image ready to be sent to the client.");
                        MapImageWriter.write(wmsResponse, output, params.getImageFormat(), img, params.getPixelSize());
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


}
