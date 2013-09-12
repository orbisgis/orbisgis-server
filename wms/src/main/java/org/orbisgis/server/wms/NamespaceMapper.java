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

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Dedicated Namespace Mapper we will give to jaxb in order to have nicer xml answers...
 * This class IS ugly. It's private API, I'm not suppose to use it. But it is :
 * - Documented in JAXB's doc
 * - working
 * So I propose to keep it...
 * Documentation can be found here : http://jaxb.java.net/2.2.6/docs/ch05.html#section-3752096477276927
 *
 * @author alexis
 */
public class NamespaceMapper extends NamespacePrefixMapper {

    private HashMap<String,String> uriMap;
    private String defaultNamespace;

    /**
     * Default constructor.
     */
    public NamespaceMapper(){
        defaultNamespace = "http://www.opengis.net/wms";
        uriMap = new HashMap<String, String>();
        uriMap.put("http://www.opengis.net/sld/1.2","sld");
        uriMap.put("http://www.opengis.net/wms","wms");
        uriMap.put("http://www.opengis.net/se/2.0/core","se");
        uriMap.put("urn:oasis:names:tc:ciq:xsdschema:xAL:2.0","xal");
        uriMap.put("http://www.w3.org/1999/xlink","xlink");
        uriMap.put("http://www.opengis.net/ows/2.0","ows");
        uriMap.put("http://www.opengis.net/fes/2.1","fes");
        uriMap.put("http://www.opengis.net/se/2.0/thematic","thematic");
        uriMap.put("http://www.opengis.net/wfs/2.1","wfs");
        uriMap.put("http://www.opengis.net/ows-context","context");
        uriMap.put("http://www.opengis.net/gml","gml");
        uriMap.put("http://www.opengis.net/kml/2.2","kml");
        uriMap.put("http://www.w3.org/2005/Atom","atom");
        uriMap.put("http://www.w3.org/2001/SMIL20/","smil");
        uriMap.put("http://www.opengis.net/se/2.0/raster","raster");
        uriMap.put("http://www.w3.org/2001/SMIL20/Language","smillang");
    }

    @Override
    public String getPreferredPrefix(String s, String s2, boolean requirePrefix) {
        if(!requirePrefix && defaultNamespace.equals(s)){
            return "";
        }
        return uriMap.get(s);
    }
    @Override
    public String[] getPreDeclaredNamespaceUris() {
        int num = uriMap.size();
        String[] ret = new String[num];
        Set<Map.Entry<String,String>> entries = uriMap.entrySet();
        Iterator<Map.Entry<String,String>> iterator = entries.iterator();
        for(int i=0; i<num; i++){
            ret[i] = iterator.next().getKey();
        }
        return ret;
    }

    @Override
    public String[] getPreDeclaredNamespaceUris2() {
        int num = uriMap.size();
        String[] ret = new String[2*num];
        Set<Map.Entry<String,String>> entries = uriMap.entrySet();
        Iterator<Map.Entry<String,String>> iterator = entries.iterator();
        for(int i=0; i<num; i++){
            Map.Entry<String, String> next = iterator.next();
            if(defaultNamespace.equals(next.getKey())){
                ret[2*i] = "";
            } else {
                ret[2*i] = next.getValue();
            }
            ret[2*i+1] = next.getKey();
        }
        return ret;
    }

    @Override
    public String[] getContextualNamespaceDecls() {
        int num = uriMap.size();
        String[] ret = new String[2*num];
        Set<Map.Entry<String,String>> entries = uriMap.entrySet();
        Iterator<Map.Entry<String,String>> iterator = entries.iterator();
        for(int i=0; i<num; i++){
            Map.Entry<String, String> next = iterator.next();
            if(defaultNamespace.equals(next.getKey())){
                ret[2*i] = "";
            } else {
                ret[2*i] = next.getValue();
            }
            ret[2*i+1] = next.getKey();
        }
        return ret;
    }
}
