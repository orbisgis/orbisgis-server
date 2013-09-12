/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.server.wms;

import com.vividsolutions.jts.geom.Envelope;
import net.opengis.wms.Layer;
import org.apache.log4j.Logger;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.schema.MetadataUtilities;
import org.gdms.data.types.CRSConstraint;
import org.gdms.data.types.Constraint;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.source.SourceManager;
import org.gdms.sql.function.spatial.geometry.crs.ST_Transform;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.LayerCollection;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.se.SeExceptions;
import org.orbisgis.core.renderer.se.Style;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Map;
import org.cts.crs.CRSException;

/**
 * Abstract class used to store method shared between GetMapHandler and GetFeatureInfoHandler.
 * @author Alexis Guéganno
 */
public abstract class AbstractGetHandler {

    private Map<String, Layer> layerMap;
    private static final Logger LOGGER = Logger.getLogger(AbstractGetHandler.class);

    public AbstractGetHandler(Map<String,Layer> input){
        layerMap = input;
    }

    /**
     * Gets the map of registered layers
     * @return The map of registered layers
     */
    protected Map<String, Layer> getLayerMap() {
        return layerMap;
    }

    /**
     * Prepare the layers that have been asked in the input WMS GetMap request.
     * @param layerList The list of layers. Takes precedence over stringSLD
     * @param styleList The styles associated to the list
     * @param crs The expected CRS
     * @param stringSLD A potential external SLD file.
     * @param exceptionsFormat The expected format for exceptions
     * @param output The output stream
     * @param wmsResponse The wms response that will be used by Play in its HTTP trades
     * @param serverStyles The map of known styles in the server
     * @return A LayerCollection that gathers the layers asked in the request
     * @throws WMSException If something wrong happen
     */
    protected LayerCollection prepareLayers(String[] layerList, String[] styleList, String crs, String stringSLD,
                                            String exceptionsFormat, OutputStream output, WMSResponse wmsResponse,
                                            Map<String, Style> serverStyles)throws WMSException{

        LayerCollection layers = new LayerCollection("Map");
        try {
            //First case : Layers and Styles are given with shp and se file names
            if (layerList != null && layerList.length > 0) {
                layers = getLayerList(layerList, crs, styleList, serverStyles);

            } else if (stringSLD != null) {
                // Changing the sld String object to a Style type object
                try {
                    layers = getLayersFromSLD(stringSLD);
                } catch (URISyntaxException ex) {
                    WMS.exceptionDescription(wmsResponse, output,
                            "The SLD URI is invalid. Please enter a valid SLD file URI path.");
                    return new LayerCollection("Map");
                } catch (SeExceptions.InvalidStyle ex) {
                    WMS.exceptionDescription(wmsResponse, output,
                            "The se style is invalid. Please give a SE valid style in the SLD file..");
                    return new LayerCollection("Map");
                }
            }
        } catch (LayerException ex) {
            WMS.exceptionDescription(wmsResponse, output,
                    "At least one of the chosen layer is "
                            + "invalid. Make sure of the available "
                            + "layers by requesting the server capabilities.");
            return new LayerCollection("Map");
        }
        return layers;

    }

    /**
     * Builds the collection of needed layers, with their associated styles, from the given URI.
     * @param stringSLD The URI of a remote SLD as a String
     * @return The collection of layers.
     * @throws URISyntaxException
     * @throws WMSException
     * @throws SeExceptions.InvalidStyle
     * @throws LayerException
     */
    private LayerCollection getLayersFromSLD(String stringSLD) throws URISyntaxException, WMSException,
            SeExceptions.InvalidStyle, LayerException {
        LayerCollection layers = new LayerCollection("Map");
        SLD sld = new SLD(stringSLD);
        for (int i = 0; i < sld.size(); i++) {
            layers.addLayer(sld.getLayer(i));
        }
        layers.open();
        //In case of an external sld
        for (int i = 0; i < sld.size(); i++) {
            Style theStyle = sld.getLayer(i).getStyle(0);
            layers.getChildren()[i].setStyle(0, theStyle);
        }
        return layers;
    }

    /**
     * Builds the needed layers and put them in a common LayerCollection for later rendering.
     * @param layerList The names of the layers
     * @param crs The requested CRS
     * @param styleList The list of styles to apply
     * @param serverStyles The mapping between styles and their names
     * @return A LayerCollection gathering all the requested layers
     * @throws org.orbisgis.core.layerModel.LayerException
     * @throws WMSException
     */
    protected LayerCollection getLayerList(String[] layerList, String crs, String[] styleList, Map<String, Style> serverStyles)
            throws LayerException, WMSException {
        DataManager dataManager = Services.getService(DataManager.class);
        LayerCollection layers = new LayerCollection("Map");
        int i;
        // Reverse order make the first layer been rendered in the last
        for (i = 0; i < layerList.length; i++) {
            //Create the ILayer with given layer name
            String layer = layerList[i];
            ILayer iLayer;

            //Checking if the layer CRS matches the requested one
            Map<String, Layer> map = getLayerMap();
            if (map.containsKey(layer)) {
                String layerCRS = map.get(layer).getCRS().get(0);
                if (layerCRS.equals(crs)) {
                    iLayer = dataManager.createLayer(layer);
                } else {
                    String newLayer = project(layer, crs);
                    iLayer = dataManager.createLayer(newLayer);
                }
            } else {
                throw new LayerException();
            }

            //Then adding the ILayer to the layers to render list
            layers.addLayer(iLayer);
        }
        layers.open();
        //In case of using the server's styles
        for (int j = 0; j < layerList.length; j++) {
            if (j < styleList.length) {
                String styleString = styleList[j];
                if (serverStyles.containsKey(styleString)) {
                    Style style = serverStyles.get(styleString);
                    layers.getChildren()[j].setStyle(0, style);
                } else {
                    throw new WMSException("One of the requested SE styles doesn't "
                            + "exist on this server. Please look for an "
                            + "existing style in the server extended capabilities.");
                }

            } else {
                //we add a server default style associated with the layer
                String styleString = layers.getLayer(j).getName();
                if (serverStyles.containsKey(styleString)) {
                    Style style = serverStyles.get(styleString);
                    layers.getChildren()[j].setStyle(0, style);
                }
            }
        }
        return layers;
    }

    /**
     * Build the MapTransform instance we will use.
     * @param bBox The requested bounding box.
     * @param layers The input layers
     * @param imageFormat The format of the image
     * @param width The width of the requested image
     * @param height The height of the requested image
     * @param pixelSize The pixel size
     * @return The MapTransform where we will draw our map.
     */
    protected MapTransform getMapTransform(double[] bBox, LayerCollection layers, String imageFormat,
                                           int width, int height, double pixelSize){
        double dpi = 25.4 / pixelSize;
        //Setting the envelope according to given bounding box
        Envelope env;
        //bbox: {minx, miny, maxx, maxy}
        if (bBox.length == 4) {
            env = new Envelope(bBox[0], bBox[2], bBox[1], bBox[3]);
        } else {
            env = layers.getEnvelope();
        }
        MapTransform mt = new MapTransform();
        // WMS Ask to distort map CRS when envelope and dimension box (i.e. the map to generate) differ
        mt.setAdjustExtent(false);
        int imgType = BufferedImage.TYPE_4BYTE_ABGR;

        if (ImageFormats.JPEG.toString().equals(imageFormat)) {
            imgType = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage img = new BufferedImage(width, height, imgType);
        mt.setDpi(dpi);
        LOGGER.debug("DPI set to: "+dpi);
        mt.setImage(img);
        mt.setExtent(env);
        return mt;

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
                    // create a new DataSource
                    Value newCRS = ValueFactory.createValue(targetCrs);
                    Metadata md = sds.getMetadata();
                    int spatialFieldIndex = MetadataUtilities.getSpatialFieldIndex(md);
                    DiskBufferDriver driver = new DiskBufferDriver(dsf, getProjectedMetadata(md, targetCrs));
                    ST_Transform transformFunction = new ST_Transform();
                    long rowCount = sds.getRowCount();
                    for (long i = 0; i < rowCount; i++) {
                        final Value[] newValues = sds.getRow(i).clone();
                        // Use transform method and update geometry field, put it in the new file
                        newValues[spatialFieldIndex] = transformFunction.evaluate(dsf,
                                sds.getFieldValue(i, spatialFieldIndex), newCRS);
                        driver.addValues(newValues);
                    }
                    driver.writingFinished();
                    driver.close();
                    SourceManager sm = dsf.getSourceManager();
                    String randomName = sm.nameAndRegister(driver.getFile());
                    sm.rename(randomName, newName);
                } finally {
                    sds.close();
                }
            } catch (Exception ex) {
                throw new WMSException(ex);
            }

        }

        return newName;
    }

    /**
     * Change the CRS constraint associated to md so that it match the given CRS.
     * @param md The original metadata
     * @param targetCrs The string representation of the target crs
     * @return The new Metadata
     * @throws org.gdms.driver.DriverException If we encounter a problem while handling metadata
     * @throws org.opengis.referencing.FactoryException If we failed at building the new CRS, if targetCrs is not a known EPSG code
     */
    private Metadata getProjectedMetadata(Metadata md, String targetCrs)
            throws DriverException, CRSException {
        int spatialFieldIndex = MetadataUtilities.getSpatialFieldIndex(md);
        Type geomType = md.getFieldType(spatialFieldIndex);
        Constraint[] constraints = geomType.getConstraints().clone();
        for(int i=0; i<constraints.length; i++){
            Constraint c = constraints[i];
            if(c.getConstraintCode() == Constraint.CRS){
                constraints[i] = new CRSConstraint(DataSourceFactory.getCRSFactory().getCRS(targetCrs));
                break;
            }
        }
        Type newType = TypeFactory.createType(geomType.getTypeCode(), constraints);
        String[] names = md.getFieldNames();
        Type[] types = new Type[names.length];
        for(int i=0; i<types.length; i++){
            if(i == spatialFieldIndex){
                types[i] = newType;
            } else {
                types[i] = md.getFieldType(i);
            }
        }
        return new DefaultMetadata(types, names);
    }
}
