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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.opengis.se._2_0.core.AbstractStyleType;
import net.opengis.se._2_0.core.OnlineResourceType;
import net.opengis.se._2_0.core.StyleReferenceType;
import net.opengis.se._2_0.core.StyleType;
import net.opengis.sld._1_2.NamedLayerElement;
import net.opengis.sld._1_2.StyledLayerDescriptorElement;
import net.opengis.sld._1_2.UserStyleElement;
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
 * @author Maxence Laurent
 * @author Tony MARTIN
 * @author Alexis Guéganno
 */
public class SLD {

        private List<SLDLayer> layers;

        /**
         * Constructor of the SLD object. Contains the URI file path to the
         * desired SLD file.
         *
         * @param sld An URI pointing to the SLD file we want to retrieve and use.
         * @throws URISyntaxException
         * @throws WMSException
         */
        public SLD(String sld) throws URISyntaxException, WMSException {
                URI uri = new URI(sld);
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
         * Gets a copy of the inner list of {@code SLDLayer} instances.
         * @return A copy of the inner list of {@code SLDLayer} instances.
         */
        public List<SLDLayer> getSLDLayers() {
                return layers == null ? new ArrayList<SLDLayer>() : new ArrayList<SLDLayer>(layers);
        }

        /**
         * Returns the number of layers in the SLD file.
         *
         * @return The number of layers in the SLD file.
         */
        public int size() {
                return layers.size();
        }

        /**
         * Returns the selected layer from the SLD object that contains
         * references to the layer and the associated style
         *
         * @param i The index of the layer in the list of layers returned by {@code getSLDLayers()}.
         * @return A new OrbisGIS ILayer configured with the good name and style.
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
                 * @param name The name of the layer
                 * @param sldStyle The associated style.
                 */
                public SLDLayer(String name, StyleType sldStyle) {
                        if(name == null || sldStyle == null){
                                throw new NullPointerException("You can't build SLDLayer instances with null input");
                        }
                        this.name = name;
                        style = sldStyle;
                }

                /**
                 * Returns the name of the layer
                 *
                 * @return The name of the layer
                 */
                public String getName() {
                        return name;
                }

                /**
                 * Returns the style of the layer
                 *
                 * @return The JaXB representation of the layer's style.
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
                        List<Object> styles = l.getNamedStyleOrUserStyle();
                        for (Object object : styles) {
                                if(object instanceof UserStyleElement){
                                        UserStyleElement nse = (UserStyleElement) object;
                                        List<JAXBElement<? extends AbstractStyleType>> as = nse.getAbstractStyle();
                                        for (JAXBElement<? extends AbstractStyleType> stEl : as) {
                                                SLDLayer sldLayer = processStyle(stEl.getValue(), name);
                                                if(sldLayer != null){
                                                        this.layers.add(sldLayer);
                                                }
                                        }

                                }
                        }
                }
        }

        /**
         * This recursive method will try to build a SLDLayer instance using the
         * given AbstractStyleType instance. We currently process StyleType
         * instances and StyleReferenceType
         * @param se The original JaXB SLD Style
         * @param name The name of the layer
         * @return A new SLDLayer instance
         * @throws WMSException
         */
        private SLDLayer processStyle(AbstractStyleType se, String name) throws WMSException{
                if (se instanceof StyleType) {
                        return new SLDLayer(name, (StyleType) se);
                } else if (se instanceof StyleReferenceType) {
                        StyleReferenceType seRef = (StyleReferenceType) se;
                        OnlineResourceType seOR = seRef.getOnlineResource();
                        String seHref = seOR.getHref();
                        try {
                                URI uri = new URI(seHref);
                                Unmarshaller u = Services.JAXBCONTEXT.createUnmarshaller();
                                JAXBElement<? extends AbstractStyleType> abstractStyle =
                                        (JAXBElement<? extends AbstractStyleType>) u.unmarshal(uri.toURL());
                                StyleType st = (StyleType) abstractStyle.getValue();
                                return processStyle(st, name);
                        } catch (JAXBException jaxbException) {
                                throw new WMSException(jaxbException);
                        } catch (MalformedURLException malformedURLException) {
                                throw new WMSException("We had a problem with your URL",malformedURLException);
                        } catch (URISyntaxException ex) {
                                throw new WMSException("We had a problem with your URI",ex);
                        }
                }
                return null;
        }
}