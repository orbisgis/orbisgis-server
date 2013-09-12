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
    /**
     * Mandatory
     */
    public static final String VERSION = "VERSION";
    /**
     * Mandatory
     */
    public static final String REQUEST = "REQUEST";
    /**
     * Shall be present if SLD is absent, and absent if SLD is present
     */
    public static final String LAYERS = "LAYERS";
    /**
     * Shall be present if SLD is absent, and absent if SLD is present
     */
    public static final String STYLES = "STYLES";
    /**
     * Shall be present if STYLES and LAYERS are absent, and absent if STYLES and LAYERS are present
     */
    public static final String SLD = "SLD";
    /**
     * Mandatory
     */
    public static final String CRS = "CRS";
    /**
     * Mandatory
     */
    public static final String BBOX = "BBOX";
    /**
     * Mandatory
     */
    public static final String WIDTH = "WIDTH";
    /**
     * Mandatory
     */
    public static final String HEIGHT = "HEIGHT";
    /**
     * Mandatory
     */
    public static final String FORMAT = "FORMAT";
    /**
     * Optional
     */
    public static final String TRANSPARENT = "TRANSPARENT";
    /**
     * Optional
     */
    public static final String BGCOLOR = "BGCOLOR";
    /**
     * Optional
     */
    public static final String EXCEPTIONS = "EXCEPTIONS";
    /**
     * Optional
     */
    public static final String TIME = "TIME";
    public static final String ELEVATION = "ELEVATION";
    public static final Set<String> MANDATORY_PARAMETERS;

    static {
        Set<String> temp = new HashSet<String>();
        temp.add(BBOX);
        temp.add(WIDTH);
        temp.add(HEIGHT);
        temp.add(FORMAT);
        temp.add(CRS);
        MANDATORY_PARAMETERS = Collections.unmodifiableSet(temp);
        for(String s : MANDATORY_PARAMETERS){
            System.out.println("Mandatory : "+s);
        }
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
        Set<Map.Entry<String, String[]>> entries = queryParameters.entrySet();
        Map<String, String[]> qp = new HashMap<String,String[]>();
        for(Map.Entry<String, String[]> e : entries){
            qp.put(e.getKey().toUpperCase(), e.getValue());
        }
        for(String s : MANDATORY_PARAMETERS){
            //The following test should work even for STYLES as we should have an empty string as the value for the
            //key STYLES if there is nothing after the "=" in the HTTP request.
            if(!qp.containsKey(s) || qp.get(s) == null || qp.get(s).length == 0){
                throw new WMSException("The following parameter is mandatory: "+s);
            }
        }
        if(!qp.containsKey(SLD)){
            if(!qp.containsKey(LAYERS) || !qp.containsKey(STYLES)){
                throw new WMSException("Both layers and styles must be defined when SLD is absent");
            }
        } else {
            if(qp.containsKey(LAYERS) || qp.containsKey(STYLES)){
                throw new WMSException("Both layers and styles must not be defined when SLD is present");
            }
        }

        crs = qp.get(CRS)[0];
        bBox = parseBBox(qp.get(BBOX)[0]);
        width = parseInteger(qp.get(WIDTH)[0]);
        height = parseInteger(qp.get(HEIGHT)[0]);
        if(width<=0 || height<=0){
             throw new WMSException("The width and the height must be greater than 0.");
        }
        if(qp.containsKey(LAYERS)){
            layerList = parseLayers(qp.get(LAYERS)[0]);
        }
        if(qp.containsKey(STYLES)){
            if (!qp.get(STYLES)[0].isEmpty()) {
                styleList = qp.get(STYLES)[0].split(",");
            } else {
                styleList = new String[0];
            }
        }

        if (qp.containsKey("PIXELSIZE")) {
            try{                
                pixelSize = Double.valueOf(qp.get("PIXELSIZE")[0]);
            } catch(NumberFormatException nfe){
                throw new WMSException("The pixel size must be a double value.", nfe);
            }
        }
        if (pixelSize<=0){
            throw new WMSException("The pixel siz must be greater than 0.");
        }

        imageFormat = qp.get(FORMAT)[0];

        if (qp.containsKey(TRANSPARENT)) {
            try {
                transparent = Boolean.valueOf(qp.get(TRANSPARENT)[0]);
            } catch (NumberFormatException nfe) {
                throw new WMSException("The transparent parameter must be expressed with true or false terms.", nfe);
            }
        }

        if (qp.containsKey(BGCOLOR)) {
            bgColor = qp.get(BGCOLOR)[0];
        }

        if (qp.containsKey(SLD)) {
            sld = qp.get(SLD)[0];
        }

        if (qp.containsKey(EXCEPTIONS)) {
            exceptionsFormat = qp.get(EXCEPTIONS)[0];
        }
    }

    /**
     * Parses the layers contained in s as a comma separated list of values
     * @param s The input string
     * @return The layers' names in an array
     * @throws WMSException
     */
    protected String[] parseLayers(String s) throws WMSException{
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

    /**
     * Parses s as an integer, transforming the potential NumberFormatException in a WMSException
     * @param s The input String
     * @return The parsed integer
     * @throws WMSException
     */
    protected final int parseInteger(String s) throws WMSException{
        try{
            return Integer.valueOf(s);
        } catch (NumberFormatException nfe){
            throw new WMSException("The given int value is not valid: "+s, nfe);
        }
    }  


    /**
     * Parses s as an array of comma separated doubles, transforming the potential 
     * NumberFormatException in a WMSException
     * We want a BBox, so there shall be exactly four double values 
     * that represent  minx, miny, maxx, maxy.
     * 
     * @param s The input String
     * @return The parsed array of double values
     * @throws WMSException
     */
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
        return layerList != null ? Arrays.copyOf(layerList, layerList.length) : new String[0];
    }

    /**
     * Gets a copy of the array of style used to draw the layers that must be queried
     * @return A copy of the array of style used to draw the layers that must be queried
     */
    public String[] getStyleList() {
        return styleList!= null ? Arrays.copyOf(styleList, styleList.length) : new String[0];
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
