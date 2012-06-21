/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.


 * Team leader : Erwan Bocher, scientific researcher,

 * User support leader : Gwendall Petit, geomatic engineer.

 * Previous computer developer : Pierre-Yves FADET, computer engineer, Thomas LEDUC,
 * scientific researcher, Fernando GONZALEZ CORTES, computer engineer.

 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC

 * Copyright (C) 2010 Erwan BOCHER, Alexis GUEGANNO, Maxence LAURENT, Antoine GOURLAY

 * Copyright (C) 2012 Erwan BOCHER, Antoine GOURLAY

 * This file is part of OrbisGIS.

 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.

 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.

 * For more information, please consult: <http://www.orbisgis.org/>

 * or contact directly:
 * info@orbisgis.org
 */
package org.orbisgis.server.wms;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.*;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.validation.Schema;
import net.opengis.se._2_0.core.AbstractStyleType;
import net.opengis.se._2_0.core.StyleType;
import net.opengis.sld._1_2.NamedLayerElement;
import net.opengis.sld._1_2.StyledLayerDescriptorElement;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.renderer.se.SeExceptions;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.Style;

/**
 *
 * @author maxence, Tony MARTIN
 */
public class SLD {

        private ArrayList<SLDLayer> layers;

        SLD(String sld) throws URISyntaxException {

                URI uri = new URI(sld);

                JAXBContext jaxbContext;
                try {
                        jaxbContext = JAXBContext.newInstance("net.opengis.wms:net.opengis.sld._1_2:net.opengis.se._2_0.core");
                        Unmarshaller u = jaxbContext.createUnmarshaller();

                        Schema schema = u.getSchema();
                        ValidationEventCollector validationCollector = new ValidationEventCollector();
                        u.setEventHandler(validationCollector);

                        JAXBElement<StyledLayerDescriptorElement> sldElem = (JAXBElement<StyledLayerDescriptorElement>) u.unmarshal(uri.toURL());
                        String errors = "";

                        for (ValidationEvent event : validationCollector.getEvents()) {
                                String msg = event.getMessage();
                                ValidationEventLocator locator = event.getLocator();
                                int line = locator.getLineNumber();
                                int column = locator.getColumnNumber();
                                errors = errors + "Error at line " + line + " column " + column + " (" + msg + ")\n";

                        }
                        if (errors.isEmpty()) {
                                try {
                                        init(sldElem.getValue());
                                } catch (InvalidStyle ex) {
                                }
                        }
                } catch (JAXBException jaxbException) {
                } catch (MalformedURLException malformedURLException) {
                }
        }

        int size() {
                return layers.size();
        }

        ILayer getLayer(int i) {

                ILayer layer = null;
                StyleType st = layers.get(i).getStyle();
                Style the_style;
                try {
                        the_style = new Style(st, null);
                        Style fts = new Style(layer, false);
                        layer.addStyle(fts);
                        fts.merge(the_style);
                } catch (InvalidStyle ex) {
                }
                return layer;
        }

        public static class SLDLayer {

                private StyleType style;
                private final String name;

                public SLDLayer(String name) {
                        this.name = name;
                }

                public void addStyle(StyleType sldStyle) {
                        style = sldStyle;
                }

                public String getName() {
                        return name;
                }

                public StyleType getStyle() {
                        return style;
                }
        }

        private void init(StyledLayerDescriptorElement sldType) throws SeExceptions.InvalidStyle {
                List<NamedLayerElement> layers = sldType.getNamedLayer();

                this.layers = new ArrayList<SLDLayer>();

                for (NamedLayerElement l : layers) {

                        String name = l.getName();

                        SLDLayer sldLayer = new SLDLayer(name);
                        JAXBElement<? extends AbstractStyleType> style = l.getAbstractStyle();

                        net.opengis.se._2_0.core.StyleType se = (net.opengis.se._2_0.core.StyleType) style.getValue();
                        //Style the_style = new Style(se, null);
                        sldLayer.addStyle(se);


                        this.layers.add(sldLayer);

                }
        }
}