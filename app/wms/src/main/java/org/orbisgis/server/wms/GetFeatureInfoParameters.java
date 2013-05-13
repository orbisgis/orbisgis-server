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
 * Gathers the GetFeatureInfo parameters by parsing them in a Map of HTTP parameters.
 * @author Alexis Guéganno
 */
public class GetFeatureInfoParameters extends GetMapParameters {
    public static final String QUERY_LAYERS = "QUERY_LAYERS";
    public static final String INFO_FORMAT = "INFO_FORMAT";
    public static final String FEATURE_COUNT = "FEATURE_COUNT";
    public static final String I = "I";
    public static final String J = "J";
    private String infoFormat;
    private String[] queryLayerList;
    private int i;
    private int j;
    private int featureCount;
    public static final Set<String> FEATURE_INFO_MANDATORY_PARAMS;

    static{
        Set<String> tmp = new HashSet<String>();
        tmp.add(QUERY_LAYERS);
        tmp.add(INFO_FORMAT);
        tmp.add(J);
        tmp.add(I);
        FEATURE_INFO_MANDATORY_PARAMS = Collections.unmodifiableSet(tmp);
    }

    /**
     * Parses the given map of HTTP parameters to build this set of GetFeatureInfo
     * @param queryParameters The map of HTTP parameters
     * @throws WMSException If some parameters is invalid
     */
    public GetFeatureInfoParameters(Map<String, String[]> queryParameters) throws WMSException {
        super(queryParameters);
        //The map parameters are valid. Let's check the ones dedicated to the GetFeatureInfo request.
        for(String s : FEATURE_INFO_MANDATORY_PARAMS){
            if(!queryParameters.containsKey(s)){
                throw new WMSException("The following parameter is mandatory: "+s);
            }
        }
        queryLayerList = parseLayers(queryParameters.get(QUERY_LAYERS)[0]);
        infoFormat = queryParameters.get(INFO_FORMAT)[0];
        if(infoFormat.isEmpty()){
            throw new WMSException("INFO_FORMAT can't be empty.");
        }
        if (queryParameters.containsKey(FEATURE_COUNT)) {
            featureCount = Integer.valueOf(queryParameters.get(FEATURE_COUNT)[0]);
        }
        i = parseInteger(queryParameters.get(I)[0]);
        j = parseInteger(queryParameters.get(J)[0]);
    }

    /**
     * Gets the expected format for the answer to this FeatureInfo request
     * @return The expected format for the answer to this FeatureInfo request
     */
    public String getInfoFormat() {
        return infoFormat;
    }

    /**
     * Gets the list of layers to be queried
     * @return The layers to be queried in an array
     */
    public String[] getQueryLayerList() {
        return Arrays.copyOf(queryLayerList, queryLayerList.length);
    }

    /**
     * The I ordinate of the query.
     * @return I ordinate of the query.
     */
    public int getI() {
        return i;
    }

    /**
     * The J ordinate of the query.
     * @return J ordinate of the query.
     */
    public int getJ() {
        return j;
    }

    /**
     * The max number of features to return for each queried layer.
     * @return The max number of features to return for each queried layer.
     */
    public int getFeatureCount() {
        return featureCount;
    }
}
