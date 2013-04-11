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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;
import net.opengis.wms.Layer;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.MetadataUtilities;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.sql.function.spatial.geometry.crs.ST_Transform;
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
import org.orbisgis.progress.NullProgressMonitor;

/**
 * This object contains all the methods that are used to handle the getMap
 * request and give the correct parameters to the renderer
 *
 * @author maxence, Tony MARTIN
 */
public final class GetMapHandler {

        private Map<String, Layer> layerMap;

        /**
         * Receives all the getMap request parameters from getMapParameterParser
         * and turns them into acceptable objects for the renderer to process,
         * then writes the rendrer image into the output stream via
         * MapImageWriter
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
         * @throws UnsupportedEncodingException  
         */
        public void getMap(String[] layerList, String[] styleList, String crs,
                double[] bbox, int width, int height, double pixelSize, String imageFormat,
                boolean transparent, String bgColor, String stringSLD, String exceptionsFormat, OutputStream output,
                WMSResponse wmsResponse, Map<String, Style> serverStyles) throws WMSException, UnsupportedEncodingException {


                double dpi = 25.4 / pixelSize;

                DataManager dataManager = Services.getService(DataManager.class);

                LayerCollection layers = new LayerCollection("Map");

                SLD sld = null;

                //First case : Layers and Styles are given with shp and se file names
                if (layerList != null && layerList.length > 0) {
                        int i;
                        // Reverse order make the first layer been rendered in the last
                        try {
                                for (i = 0; i < layerList.length; i++) {
                                        //Create the Ilayer with given layer name
                                        String layer = layerList[i];
                                        ILayer iLayer;

                                        //Checking if the layer CRS matches the requested one
                                        if (layerMap.containsKey(layer)) {
                                                String layerCRS = layerMap.get(layer).getCRS().get(0);
                                                if (layerCRS.equals(crs)) {
                                                        iLayer = dataManager.createLayer(layer);
                                                } else {
                                                        String newLayer = project(layer, crs);
                                                        iLayer = dataManager.createLayer(newLayer);
                                                }
                                        } else {
                                                throw new LayerException();
                                        }

                                        //Then adding the Ilayer to the layers to render list
                                        layers.addLayer(iLayer);
                                }
                        } catch (LayerException e) {
                                WMS.exceptionDescription(wmsResponse, output,
                                        "At least one of the chosen layer is invalid. "
                                        + "Make sure of the available layers by requesting "
                                        + "the server capabilities.");
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
                                                WMS.exceptionDescription(wmsResponse, output,
                                                        "At least one of the chosen layer is "
                                                        + "invalid. Make sure of the available "
                                                        + "layers by requesting the server capabilities.");
                                                return;
                                        } catch (SeExceptions.InvalidStyle ex) {
                                                WMS.exceptionDescription(wmsResponse, output,
                                                        "The se style is invalid. Please give a SE valid SLD file.");
                                                return;
                                        }
                                }
                        } catch (URISyntaxException ex) {
                                WMS.exceptionDescription(wmsResponse, output,
                                        "The SLD URI is invalid. Please enter a valid SLD file URI path.");
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
                                for (j = 0; j < layerList.length; j++) {
                                        if (j < styleList.length) {
                                                String styleString = styleList[j];
                                                if (serverStyles.containsKey(styleString)) {
                                                        Style style = serverStyles.get(styleString);
                                                        layers.getChildren()[j].setStyle(0, style);
                                                } else {
                                                        WMS.exceptionDescription(wmsResponse, output,
                                                                "One of the requested SE styles doesn't "
                                                                + "exist on this server. Please look for an "
                                                                + "existing style in the server extended capabilities.");
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
                                        WMS.exceptionDescription(wmsResponse, output,
                                                "The se style is invalid. Please give a SE valid style in the SLD file..");
                                        return;
                                }
                        }


                        //Setting the envelope according to given bounding box
                        Envelope env;
                        //bbox: {minx, miny, maxx, maxy}
                        if (bbox.length == 4) {
                                env = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
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
                        if (!transparent) {
                                color = Color.decode(bgColor);
                                g2.setBackground(color);
                                g2.clearRect(0, 0, width, height);
                        }


                        NullProgressMonitor pm = new NullProgressMonitor();
                        renderer.draw(mt, g2, width, height, layers, pm);
                                                        
                        g2.dispose();
                        MapImageWriter.write(wmsResponse, output, imageFormat, img, pixelSize);

                } catch (IOException ex) {
                        ex.printStackTrace(new PrintStream(output, false, "UTF-8"));
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
         * @param queryParameters
         * @param output
         * @param wmsResponse
         * @param serverStyles
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

        GetMapHandler(Map<String, Layer> lMap) {
                layerMap = lMap;
        }

        /**
         * When a source need to be reprojected a new source is created with a specific name, this function compute the name.
         * This function only concatenate strings, it does not manage projection.
         * @param sourceName Source name
         * @param targetCrs Coordinate reference system != of the source one.
         * @return A new source name that should contain the projection data.
         */
        public static String getProjectionSourceName(String sourceName, String targetCrs) {
            return sourceName + "_" + targetCrs.hashCode();
        }
        /**
         * Compute the targetCrs version of the provided layer and return the source name
         * @param sourceName Input data source
         * @param targetCrs convert to this crs
         * @return The source name having the geometry converted into targetCrs
         * @throws WMSException
         */
        private String project(String sourceName, String targetCrs) throws WMSException {
                DataSourceFactory dsf = Services.getService(DataManager.class).getDataSourceFactory();

                // maybe we already converted it and there is nothing to do
                final String newName = getProjectionSourceName(sourceName,targetCrs);
                if (!dsf.getSourceManager().exists(newName)) {
                        try {
                            DataSource sds = dsf.getDataSource(sourceName);
                            sds.open();
                            try {
                                // create a new datasource
                                Value newCRS = ValueFactory.createValue(targetCrs);
                                DiskBufferDriver driver = new DiskBufferDriver(dsf, sds.getMetadata());
                                long rowCount = sds.getRowCount();
                                int spatialFieldIndex = MetadataUtilities.getSpatialFieldIndex(sds.getMetadata());
                                ST_Transform transformFunction = new ST_Transform();

                                for (long i = 0; i < rowCount; i++) {
                                    final Value[] newValues = sds.getRow(i).clone();
                                    // Use transform method and update geometry field, put it in the new file
                                    newValues[spatialFieldIndex] = transformFunction.evaluate(dsf,
                                            sds.getFieldValue(i, spatialFieldIndex), newCRS);
                                    driver.addValues(newValues);
                                }
                                driver.writingFinished();
                                driver.close();
                                dsf.getSourceManager().register(newName,driver.getFile());
                            } finally {
                                sds.close();
                            }
                        } catch (Exception ex) {
                            throw new WMSException(ex);
                        }

                }

                return newName;
        }
}
