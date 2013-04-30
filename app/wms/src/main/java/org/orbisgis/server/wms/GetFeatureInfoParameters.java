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

    /**
     * Parses the given map of HTTP parameters to build this set of GetFeatureInfo
     * @param queryParameters The map of HTTP parameters
     * @throws WMSException If some parameters is invalid
     */
    public GetFeatureInfoParameters(Map<String, String[]> queryParameters) throws WMSException {
        super(queryParameters);
        if (queryParameters.containsKey(QUERY_LAYERS)) {
            queryLayerList = queryParameters.get("LAYERS")[0].split(",");
            if(queryLayerList.length == 0){
                throw new WMSException("There shall be at least one layer to be queried. QUERY_LAYERS can't be empty.");
            }
        }
        if (queryParameters.containsKey(INFO_FORMAT)) {
            infoFormat = queryParameters.get(INFO_FORMAT)[0];
        } else {
            throw new WMSException("INFO_FORMAT is mandatory");
        }
        if (queryParameters.containsKey(FEATURE_COUNT)) {
            featureCount = Integer.valueOf(queryParameters.get(FEATURE_COUNT)[0]);
        }
        if (queryParameters.containsKey(I)) {
            try {
                i = Integer.valueOf(queryParameters.get(I)[0]);
            } catch(NumberFormatException nfe){
                throw new WMSException("Can't read the I value as an int: "+queryParameters.get(I)[0]);
            }
        } else {
            throw new WMSException("I parameter is mandatory");
        }
        if (queryParameters.containsKey(J)) {
            try {
                j = Integer.valueOf(queryParameters.get(J)[0]);
            } catch(NumberFormatException nfe){
                throw new WMSException("Can't read the J value as an int: "+queryParameters.get(J)[0]);
            }
        } else {
            throw new WMSException("J parameter is mandatory");
        }
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
