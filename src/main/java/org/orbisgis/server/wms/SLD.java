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
import net.opengis.se._2_0.core.AbstractStyleType;
import net.opengis.se._2_0.core.OnlineResourceType;
import net.opengis.se._2_0.core.StyleReferenceType;
import net.opengis.se._2_0.core.StyleType;
import net.opengis.sld._1_2.NamedLayerElement;
import net.opengis.sld._1_2.StyledLayerDescriptorElement;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.renderer.se.SeExceptions;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.Style;

/**
 * Called in the case where the styles and layers are defined by a SLD file URI
 *
 * @author maxence, Tony MARTIN
 */
public class SLD {

        private List<SLDLayer> layers;

        /**
         * Constructor of the SLD object. Contains the URI file path to the
         * desired SLD file.
         *
         * @param sld
         * @throws URISyntaxException
         * @throws WMSException
         */
        public SLD(String sld) throws URISyntaxException, WMSException {

                URI uri = new URI(sld);

                JAXBContext jaxbContext;
                try {
                        Unmarshaller u = Services.JAXBCONTEXT.createUnmarshaller();

                        StyledLayerDescriptorElement sldElem = (StyledLayerDescriptorElement) u.unmarshal(uri.toURL());

                        init(sldElem);

                } catch (JAXBException jaxbException) {
                        throw new WMSException(jaxbException);
                } catch (MalformedURLException malformedURLException) {
                        throw new WMSException(malformedURLException);
                } catch (SeExceptions.InvalidStyle invalidStyleException) {
                        throw new WMSException(invalidStyleException);
                }
        }

        /**
         * Returns the number of layers in the SLD file.
         *
         * @return
         */
        public int size() {
                return layers.size();
        }

        /**
         * Returns the selected layer from the SLD object that contains
         * references to the layer and the associated style
         *
         * @param i
         * @return
         * @throws WMSException
         * @throws org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle
         */
        public ILayer getLayer(int i) throws WMSException, InvalidStyle {

                DataManager dataManager = Services.getService(DataManager.class);
                ILayer layer;
                try {
                        layer = dataManager.createLayer(layers.get(i).getName());
                } catch (LayerException ex) {
                        throw new WMSException(ex);
                }
                StyleType st = layers.get(i).getStyle();
                Style theStyle;

                theStyle = new Style(st, layer);
                List<Style> styles = new ArrayList<Style>();
                styles.add(theStyle);
                layer.setStyles(styles);

                return layer;
        }

        /**
         * Helps manage a SLDLayer, element of the SLD object
         */
        public static class SLDLayer {

                private StyleType style;
                private final String name;

                /**
                 * SLD Layer constructor, created with the name of the queryable
                 * layer
                 *
                 * @param name
                 */
                public SLDLayer(String name) {
                        this.name = name;
                }

                /**
                 * Adds the style reference to the layer
                 *
                 * @param sldStyle
                 */
                public void addStyle(StyleType sldStyle) {
                        style = sldStyle;
                }

                /**
                 * Returns the name of the layer
                 *
                 * @return
                 */
                public String getName() {
                        return name;
                }

                /**
                 * Returns the style of the layer
                 *
                 * @return
                 */
                public StyleType getStyle() {
                        return style;
                }
        }

        private void init(StyledLayerDescriptorElement sldType) throws SeExceptions.InvalidStyle, WMSException {

                List<NamedLayerElement> sldLayers = sldType.getNamedLayer();

                this.layers = new ArrayList<SLDLayer>();

                for (NamedLayerElement l : sldLayers) {

                        String name = l.getName();

                        SLDLayer sldLayer = new SLDLayer(name);
                        JAXBElement<? extends AbstractStyleType> style = l.getAbstractStyle();

                        //If the style is given inline in the SLD file
                        if (style.getValue() instanceof StyleType) {
                                StyleType se = (StyleType) style.getValue();
                                sldLayer.addStyle(se);
                        } else //If the SE file is given by an external URL - Not Working Yet
                        if (style.getValue() instanceof StyleReferenceType) {
                                StyleReferenceType seRef = (StyleReferenceType) style.getValue();
                                OnlineResourceType seOR = seRef.getOnlineResource();
                                String seHref = seOR.getHref();
                                try {
                                        URI uri = new URI(seHref);
                                        Unmarshaller u = Services.JAXBCONTEXT.createUnmarshaller();
                                        JAXBElement<? extends AbstractStyleType> abstractStyle = (JAXBElement<? extends AbstractStyleType>) u.unmarshal(uri.toURL());
                                        StyleType se = (StyleType) abstractStyle.getValue();
                                        sldLayer.addStyle(se);

                                } catch (JAXBException jaxbException) {
                                        throw new WMSException(jaxbException);
                                } catch (MalformedURLException malformedURLException) {
                                        throw new WMSException(malformedURLException);
                                } catch (URISyntaxException ex) {
                                        throw new WMSException(ex);
                                }
                        }




                        this.layers.add(sldLayer);

                }
        }
}