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

import java.util.*;

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
    public static final Set<String> MANDATORY_PARAMETERS;

    static {
        Set<String> temp = new HashSet<String>();
        temp.add(LAYERS);
        temp.add(STYLES);
        temp.add(BBOX);
        temp.add(WIDTH);
        temp.add(HEIGHT);
        temp.add(FORMAT);
        temp.add(CRS);
        MANDATORY_PARAMETERS = Collections.unmodifiableSet(temp);
    }


    private String[] layerList;
    private String[] styleList;
    private double[] bBox;
    private String crs;
    private int width;
    private int height;
    private String imageFormat = "undefined";
    //Use a 72 dpi resolution by default
    private double pixelSize = 0.35;
    private String exceptionsFormat = null;
    private String sld;
    private String bgColor = "#FFFFFF";
    private boolean transparent;

    /**
     * Parses all the key-value pairs given in argument to decide how to draw the requested map.
     * @param queryParameters The HTTP key-value arguments in a Map
     * @throws WMSException If some mandatory argument is missing or if an argument is invalid.
     */
    public GetMapParameters(Map<String, String[]> queryParameters) throws WMSException {
        for(String s : MANDATORY_PARAMETERS){
            //The following test should work even for STYLES as we should have an empty string as the value for the
            //key STYLES if there is nothing after the "=" in the HTTP request.
            if(!queryParameters.containsKey(s) || queryParameters.get(s) == null || queryParameters.get(s).length == 0){
                throw new WMSException("The following parameter is mandatory: "+s);
            }
        }
        crs = queryParameters.get(CRS)[0];
        bBox = parseBBox(queryParameters.get(BBOX)[0]);
        width = parseInteger(queryParameters.get(WIDTH)[0]);
        height = parseInteger(queryParameters.get(HEIGHT)[0]);
        layerList = parseLayers(queryParameters.get(LAYERS)[0]);
        if (!queryParameters.get(STYLES)[0].isEmpty()) {
            styleList = queryParameters.get(STYLES)[0].split(",");
        } else {
            styleList = new String[0];
        }

        if (queryParameters.containsKey("PIXELSIZE")) {
            pixelSize = Double.valueOf(queryParameters.get("PIXELSIZE")[0]);
        }

        imageFormat = queryParameters.get(FORMAT)[0];

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

    private String[] parseLayers(String s) throws WMSException{
        String[] ret = s.split(",");
        ArrayList<String> tmp = new ArrayList<String>();
        for(String in : ret){
            if(!in.isEmpty()){
                tmp.add(in);
            }
        }
        if(tmp.isEmpty()){
            throw new WMSException("There shall be at least one layer in the map");
        }
        return tmp.toArray(new String[tmp.size()]);
    }

    protected final int parseInteger(String s) throws WMSException{
        try{
            return Integer.valueOf(s);
        } catch (NumberFormatException nfe){
            throw new WMSException("The given int value is not valid: "+s, nfe);
        }
    }

    private double[] parseBBox(String s) throws WMSException{
        String[] boxArray = s.split(",");
        if(boxArray.length != 4){
            throw new WMSException("There shall be exactly four ordinates in a BBOX definition");
        }
        double[] ret = new double[boxArray.length];
        for (int i = 0; i < ret.length; i++) {
            try{
                ret[i] = Double.valueOf(boxArray[i]);
            } catch(NumberFormatException nfe){
                throw new WMSException("The ordinate #"+i+" is not valid: "+boxArray[i], nfe);
            }
        }
        return ret;
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
    public double[] getbBox() {
        return Arrays.copyOf(bBox, bBox.length);
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
