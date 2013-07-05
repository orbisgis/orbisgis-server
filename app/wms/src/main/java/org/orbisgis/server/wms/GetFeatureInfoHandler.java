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

import com.vividsolutions.jts.geom.Envelope;
import net.opengis.wms.Layer;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.indexes.DefaultSpatialIndexQuery;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.indexes.IndexManager;
import org.gdms.data.indexes.IndexQueryException;
import org.gdms.data.schema.Metadata;
import org.gdms.driver.DriverException;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import org.orbisgis.core.layerModel.BeanLayer;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.LayerCollection;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.progress.NullProgressMonitor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Handles a GetFeatureInfo request.
 * @author Alexis Guéganno
 */
public class GetFeatureInfoHandler extends AbstractGetHandler {

    public static final String PLAIN_TXT = "text/plain";

    /**
     * Builds a new GetFeatureInfoHandler that knows the given layers.
     * @param input The known layers that can be used as input.
     */
    public GetFeatureInfoHandler(Map<String, Layer> input) {
        super(input);
    }

    /**
     * Feeds the given output stream by retrieving the needed params in the provided GetFeatureInfoParams instance.
     * @param params The parameters of the request set in a GetFeatureInfoParameters instance.
     * @param output The output stream we will write in
     * @param wmsResponse The WMSResponse we must fill
     * @param serverStyles the known styles
     * @throws WMSException
     */
    public void getFeatureInfo(GetFeatureInfoParameters params, OutputStream output,
                               WMSResponse wmsResponse, Map<String, Style> serverStyles) throws WMSException {
        if(!params.getInfoFormat().equalsIgnoreCase(PLAIN_TXT)){
            throw new WMSException("The requested format is not supported: "+params.getInfoFormat());
        }
        LayerCollection layers = prepareLayers(params.getQueryLayerList(), params.getStyleList(), params.getCrs(),
                params.getSld(), params.getExceptionsFormat(), output, wmsResponse, serverStyles);
        int width = params.getWidth();
        int height = params.getHeight();
        int i = params.getI();
        int j = params.getJ();
        
        if(i<=0 || i>=width){
            throw new WMSException("GetFeatureInfo request contains invalid I value. ");
        }
        
        if(j<=0 || j>=height){
            throw new WMSException("GetFeatureInfo request contains invalid J value. ");
        }
        
        double[] bBox = params.getbBox();
        DataSourceFactory dsf = Services.getService(DataManager.class).getDataSourceFactory();
        Envelope env = getEnvelopeRequest(bBox,width, height, i, j);
        ILayer[] children = layers.getChildren();
        IndexManager im = dsf.getIndexManager();
        for(ILayer c : children){
            try {
                feedOutput(im, env, c, output);
            } catch (IOException e) {
                throw new WMSException("Problem while feeding the output stream", e);
            } catch (DriverException e) {
                throw new WMSException("Problem while reading the data", e);
            } catch (IndexQueryException e) {
                throw new WMSException("Problem while querying the index", e);
            } catch (NoSuchTableException e) {
                throw new WMSException("The requested table does not exist", e);
            } catch (IndexException e) {
                throw new WMSException("Problem while querying the index", e);
            }
        }
        wmsResponse.setContentType(PLAIN_TXT);
        wmsResponse.setResponseCode(200);
    }

    private void feedOutput(IndexManager im, Envelope env, ILayer l, OutputStream output) throws IOException,
            DriverException, IndexQueryException, NoSuchTableException, IndexException {
        if(l instanceof BeanLayer){
            BeanLayer bl = (BeanLayer) l;
            DataSource dataSource = bl.getDataSource();
            StringBuilder sb = new StringBuilder();
            sb.append(bl.getName());
            sb.append("\n");
            String geomField = dataSource.getFieldName(dataSource.getSpatialFieldIndex());
            if(!im.isIndexed(dataSource,geomField)){
                im.buildIndex(dataSource,geomField, new NullProgressMonitor());
            }
            int[] rows = im.queryIndex(dataSource, new DefaultSpatialIndexQuery(geomField, env));
            Metadata md = dataSource.getMetadata();
            dataSource.open();
            for(int i : rows){
                for(int j = 0; j<md.getFieldCount(); j++){
                    sb.append(md.getFieldName(j));
                    sb.append(": ");
                    sb.append(dataSource.getFieldValue(i,j).toString());
                    sb.append("\n");
                }
                sb.append("\n");
            }
            dataSource.close();
            output.write(sb.toString().getBytes());
        }

    }

    /**
     * Gets the approximate envelope of pixel (i,j) in an image of dimensions (width, height) where we draw the map
     * contained in the bounding box bBox.
     * @param bBox The bounding box of the map
     * @param width The width of the image
     * @param height The height of the image
     * @param i The i coordinate of the selected pixel in the image
     * @param j The j coordinate of the selected pixel in the image
     * @return The computed envelope
     */
    public Envelope getEnvelopeRequest(double[] bBox, int width, int height, int i, int j){
        double bBoxW = bBox[2] - bBox [0];
        double bBoxH = bBox[3] - bBox [1];
        double envW = bBoxW / width;
        double envH = bBoxH / height;
        double minX = bBox[0] + i * envW;
        double maxX = bBox[0] + (i+1) * envW;
        double maxY = bBox[3] - j * envH;
        double minY = bBox[3] - (j+1) * envH;
        return new Envelope(minX, maxX, minY, maxY);
    }
}
