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

import java.util.Arrays;
import java.util.Map;

/**
 * Gathers the GetMap parameters by parsing them in a Map of HTTP parameters.
 * @author Alexis Guéganno.
 */
public class GetMapParameters {
    public static final String VERSION = "VERSION";
    public static final String REQUEST = "REQUEST";
    public static final String LAYERS = "LAYERS";
    public static final String STYLES = "STYLES";
    public static final String SLD = "SLD";
    public static final String CRS = "CRS";
    public static final String BBOX = "BBOX";
    public static final String WIDTH = "WIDTH";
    public static final String HEIGHT = "HEIGHT";
    public static final String FORMAT = "FORMAT";
    public static final String TRANSPARENT = "TRANSPARENT";
    public static final String BGCOLOR = "BGCOLOR";
    public static final String EXCEPTIONS = "EXCEPTIONS";
    public static final String TIME = "TIME";
    public static final String ELEVATION = "ELEVATION";

    private String[] layerList;
    private String[] styleList;
    private double[] bbox;
    private String crs;
    private int width;
    private int height;
    private String imageFormat = "undefined";
    //Use a 72 dpi resolution by default
    private double pixelSize = 0.35;
    private String exceptionsFormat = null;
    private String sld;
    private String bgColor;
    private boolean transparent;

    public GetMapParameters(Map<String, String[]> queryParameters) throws WMSException {

        if (queryParameters.containsKey(CRS)) {
            crs = queryParameters.get(CRS)[0];
        } else {
            throw new WMSException("No CRS has been declared");
        }

        if (queryParameters.containsKey(BBOX)) {
            String[] sbbox = queryParameters.get(BBOX)[0].split(",");
            bbox = new double[sbbox.length];

            for (int i = 0; i < bbox.length; i++) {
                bbox[i] = Double.valueOf(sbbox[i]);
            }
        } else {
            throw new WMSException("The given BBOX is invalid");
        }

        if (queryParameters.containsKey(WIDTH)) {
            width = Integer.valueOf(queryParameters.get(WIDTH)[0]);
        } else {
            throw new WMSException("The WIDTH must be correctly set");
        }

        if (queryParameters.containsKey(HEIGHT)) {
            height = Integer.valueOf(queryParameters.get(HEIGHT)[0]);
        } else {
            throw new WMSException("The HEIGHT must be correctly set");
        }

        if (queryParameters.containsKey(LAYERS)) {
            layerList = queryParameters.get(LAYERS)[0].split(",");
        }

        if (queryParameters.containsKey(STYLES) && !queryParameters.get(STYLES)[0].isEmpty()) {
            styleList = queryParameters.get(STYLES)[0].split(",");
        }

        if (queryParameters.containsKey("PIXELSIZE")) {
            pixelSize = Double.valueOf(queryParameters.get("PIXELSIZE")[0]);
        }

        if (queryParameters.containsKey(FORMAT)) {
            imageFormat = queryParameters.get(FORMAT)[0];
        }

        if (queryParameters.containsKey(TRANSPARENT)) {
            transparent = Boolean.valueOf(queryParameters.get(TRANSPARENT)[0]);
        }

        if (queryParameters.containsKey(BGCOLOR)) {
            bgColor = queryParameters.get(BGCOLOR)[0];
        }

        if (queryParameters.containsKey(SLD)) {
            sld = queryParameters.get(SLD)[0];
        }

        if (queryParameters.containsKey(EXCEPTIONS)) {
            exceptionsFormat = queryParameters.get(EXCEPTIONS)[0];
        }
    }

    /**
     * Gets a copy of the array of layers that must be queried
     * @return A copy of the array of layers that must be queried
     */
    public String[] getLayerList() {
        return Arrays.copyOf(layerList, layerList.length);
    }

    /**
     * Gets a copy of the array of style used to draw the layers that must be queried
     * @return A copy of the array of style used to draw the layers that must be queried
     */
    public String[] getStyleList() {
        return Arrays.copyOf(styleList, styleList.length);
    }

    /**
     * Gets the bounding box of the map to be displayed or queried
     * @return The bounding box of the map to be displayed or queried
     */
    public double[] getBbox() {
        return Arrays.copyOf(bbox,bbox.length);
    }

    /**
     * Gets the CRS used to define the map
     * @return The CRS used to define the map
     */
    public String getCrs() {
        return crs;
    }

    /**
     * Gets the width of the map in pixel
     * @return The width of the map in pixel
     */
    public int getWidth() {
        return width;
    }


    /**
     * Gets the height of the map in pixel
     * @return The height of the map in pixel
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the expected format of the image.
     * @return The expected format of the image
     */
    public String getImageFormat() {
        return imageFormat;
    }


    public double getPixelSize() {
        return pixelSize;
    }

    /**
     * Gets the expected formats for exceptions formatting
     * @return The expected formats for exceptions formatting
     */
    public String getExceptionsFormat() {
        return exceptionsFormat;
    }

    /**
     * Gets the SLD file definition that defines the way the map is rendered
     * @return The SLD location
     */
    public String getSld() {
        return sld;
    }

    /**
     * Gets the background colour of the map
     * @return The background colour of the map in the format #AEAEAE, as a String.
     */
    public String getBgColor() {
        return bgColor;
    }

    /**
     * Gets whether the map must be transparent or not
     * @return True if the map must be transparent.
     */
    public boolean isTransparent() {
        return transparent;
    }
}
