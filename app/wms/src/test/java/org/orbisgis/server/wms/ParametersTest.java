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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the GetFeatureInfoParameters and GetMapParameters classes.
 * @author Alexis Guéganno
 */
public class ParametersTest {

    private static final double EPS = 0.00001;

    private Map<String,String[]> getMapMap(){
        Map<String,String[]> ret = new HashMap<String, String[]>();
        ret.put(GetMapParameters.BBOX, new String[]{"0,1,4,5"});
        ret.put(GetMapParameters.LAYERS, new String[]{"road,fields"});
        ret.put(GetMapParameters.STYLES, new String[]{"dash_roads"});
        ret.put(GetMapParameters.CRS, new String[]{"EPSG:4326"});
        ret.put(GetMapParameters.FORMAT, new String[]{"image/png"});
        ret.put(GetMapParameters.WIDTH, new String[]{"40"});
        ret.put(GetMapParameters.HEIGHT, new String[]{"50"});
        ret.put(GetMapParameters.TRANSPARENT, new String[]{"TRUE"});
        ret.put(GetMapParameters.BGCOLOR, new String[]{"#111111"});
        ret.put(GetMapParameters.EXCEPTIONS, new String[]{"text/xml"});
        return ret;
    }

    private Map<String,String[]> getFeatureInfoMap(){
        Map<String,String[]> ret= getMapMap();
        ret.put(GetFeatureInfoParameters.QUERY_LAYERS,new String[]{"road"});
        ret.put(GetFeatureInfoParameters.INFO_FORMAT,new String[]{"text/plain"});
        ret.put(GetFeatureInfoParameters.FEATURE_COUNT,new String[]{"4"});
        ret.put(GetFeatureInfoParameters.I,new String[]{"12"});
        ret.put(GetFeatureInfoParameters.J,new String[]{"24"});
        return ret;
    }

    @Test
    public void testMapParsing() throws Exception{
        Map<String, String[]> map= getMapMap();
        GetMapParameters params = new GetMapParameters(map);
        assertTrue(params.getbBox()[0] - 0 < EPS);
        assertTrue(params.getbBox()[1] - 1 < EPS);
        assertTrue(params.getbBox()[2] - 4 < EPS);
        assertTrue(params.getbBox()[3] - 5 < EPS);
        assertTrue(params.getbBox().length == 4);
        assertTrue(params.getLayerList()[0].equals("road"));
        assertTrue(params.getLayerList()[1].equals("fields"));
        assertTrue(params.getLayerList().length == 2);
        assertTrue(params.getStyleList()[0].equals("dash_roads"));
        assertTrue(params.getStyleList().length == 1);
        assertTrue(params.getCrs().equals("EPSG:4326"));
        assertTrue(params.getImageFormat().equals("image/png"));
        assertTrue(params.getWidth() == 40);
        assertTrue(params.getHeight() == 50);
        assertTrue(params.isTransparent());
        assertTrue(params.getBgColor().equals("#111111"));
        assertTrue(params.getExceptionsFormat().equals("text/xml"));
    }

    @Test
    public void testFeatureInfoParsing() throws Exception {
        Map<String, String[]> map= getFeatureInfoMap();
        GetFeatureInfoParameters params = new GetFeatureInfoParameters(map);
        assertTrue(params.getI() == 12);
        assertTrue(params.getJ() == 24);
        assertTrue(params.getFeatureCount() == 4);
        assertTrue(params.getInfoFormat().equals("text/plain"));
        assertTrue(params.getQueryLayerList()[0].equals("road"));
    }

    @Test
    public void testMapWithoutCRS() throws Exception{
        missingMapParameter("CRS");
    }

    @Test
    public void testMapWithoutBBOX() throws Exception{
        missingMapParameter("BBOX");
    }

    @Test
    public void testMapWithoutWidth() throws Exception{
        missingMapParameter("WIDTH");
    }

    @Test
    public void testMapWithoutHeight() throws Exception{
        missingMapParameter("HEIGHT");
    }

    @Test
    public void testMapWithoutFormat() throws Exception{
        missingMapParameter("FORMAT");
    }

    @Test
    public void testMapWithoutLayers() throws Exception{
        missingMapParameter("LAYERS");
    }

    @Test
    public void testMapWithoutStyles() throws Exception{
        missingMapParameter("STYLES");
    }

    @Test
    public void testInvalidBbox() throws Exception{
        Map<String, String[]> map= getMapMap();
        map.put("BBOX", new String[]{"1,2,3"});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{"1,3"});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{"1"});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{""});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", null);
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{"1,2,3,4,5"});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{"1,2,3,yo"});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{"1,2,yo,4"});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{"1,yo,3,4"});
        mapBuildFail(map);
        map= getMapMap();
        map.put("BBOX", new String[]{"yo,2,3,4"});
        mapBuildFail(map);
    }

    @Test
    public void testInvalidWidth() throws Exception {
        Map<String, String[]> map= getMapMap();
        map.put("WIDTH", new String[]{"potato"});
        mapBuildFail(map);
    }
    
    @Test
    public void testInvalidWidth2() throws Exception {
        Map<String, String[]> map= getMapMap();
        map.put("WIDTH", new String[]{"0"});
        mapBuildFail(map);
    }

    @Test
    public void testInvalidHeight() throws Exception {
        Map<String, String[]> map= getMapMap();
        map.put("HEIGHT", new String[]{"potato"});
        mapBuildFail(map);
    }
    
    @Test
    public void testInvalidHeight2() throws Exception {
        Map<String, String[]> map= getMapMap();
        map.put("HEIGHT", new String[]{"0"});
        mapBuildFail(map);
    }

    @Test
    public void testInvalidLayerList() throws Exception {
        Map<String, String[]> map= getMapMap();
        map.put("LAYERS", new String[]{",,,,"});
        mapBuildFail(map);
        map= getMapMap();
        map.put("LAYERS", new String[]{""});
        mapBuildFail(map);
    }

    @Test
    public void testMapSLDAndStyles(){
        Map<String, String[]> map= getMapMap();
        map.put(GetMapParameters.SLD,new String[]{"sld"});
        mapBuildFail(map);
    }

    @Test
    public void testMapSLDAlone() throws Exception {
        Map<String, String[]> map= getMapMap();
        map.put(GetMapParameters.SLD,new String[]{"sld"});
        map.remove(GetMapParameters.LAYERS);
        map.remove(GetMapParameters.STYLES);
        GetMapParameters params = new GetMapParameters(map);
        assertTrue(params.getSld().equals("sld"));
    }

    private void missingMapParameter(String param){
        Map<String, String[]> map= getMapMap();
        map.remove(param);
        mapBuildFail(map);
    }

    @Test
    public void testWithoutI(){
        missingInfoParameter(GetFeatureInfoParameters.I);
    }

    @Test
    public void testWithoutJ(){
        missingInfoParameter(GetFeatureInfoParameters.J);
    }

    @Test
    public void testWithoutQueryLayers(){
        missingInfoParameter(GetFeatureInfoParameters.QUERY_LAYERS);
    }

    @Test
    public void testWithoutInfoFormat(){
        missingInfoParameter(GetFeatureInfoParameters.INFO_FORMAT);
    }

    @Test
    public void testInvalidI(){
        Map<String, String[]> map= getFeatureInfoMap();
        map.put(GetFeatureInfoParameters.I,new String[]{"patatoïde"});
        infoBuildFail(map);
    }

    @Test
    public void testInvalidJ(){
        Map<String, String[]> map= getFeatureInfoMap();
        map.put(GetFeatureInfoParameters.J,new String[]{"patatoïde"});
        infoBuildFail(map);
    }

    @Test
    public void testInvalidQueryLayers(){
        Map<String, String[]> map= getFeatureInfoMap();
        map.put(GetFeatureInfoParameters.QUERY_LAYERS,new String[]{",,,"});
        infoBuildFail(map);
        map= getFeatureInfoMap();
        map.put(GetFeatureInfoParameters.QUERY_LAYERS,new String[]{""});
        infoBuildFail(map);
    }

    @Test
    public void testInvalidInfoFormat(){
        Map<String, String[]> map= getFeatureInfoMap();
        map.put(GetFeatureInfoParameters.INFO_FORMAT,new String[]{""});
        infoBuildFail(map);
    }



    private void mapBuildFail(Map<String,String[]> map){
        try{
            GetMapParameters params = new GetMapParameters(map);
            fail();
        } catch(WMSException e){
            assertTrue(true);
        }
    }

    private void missingInfoParameter(String param){
        Map<String, String[]>  map = getFeatureInfoMap();
        map.remove(param);
    }

    private void infoBuildFail(Map<String, String[]> map){
        try{
            GetFeatureInfoParameters params = new GetFeatureInfoParameters(map);
            fail();
        } catch(WMSException e){
            assertTrue(true);
        }
    }

}
