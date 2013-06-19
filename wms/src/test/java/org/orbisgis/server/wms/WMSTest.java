/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * For more information, please consult: <http://www.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */
package org.orbisgis.server.wms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import com.vividsolutions.jts.geom.Geometry;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.MetadataUtilities;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.source.SourceManager;
import org.gdms.sql.function.spatial.geometry.crs.ST_Transform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.core.workspace.CoreWorkspace;
import org.orbisgis.utils.FileUtils;
import static org.junit.Assert.*;

/**
 * Tests for WMS.
 */
public class WMSTest {

    private File f;
    private File fshp;
    private File fshx;
    private File fdbf;
    private File fprj;
    WMS wms = new WMS();

    /**
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        f = File.createTempFile("wms", null);
        f.delete();
        CoreWorkspace c = new CoreWorkspace();
        c.setWorkspaceFolder(f.getAbsolutePath());
        WMSProperties props = new WMSProperties();
        props.putProperty(WMSProperties.TITLE, "test");
        wms.init(c, Collections.<String, Style>emptyMap(), Collections.<String, String[]>emptyMap(), props);

        fshp = File.createTempFile("gdms", ".shp");
        fshp.delete();
        FileUtils.copy(WMSTest.class.getResourceAsStream("cantons.shp"), fshp);
        String name = FileUtils.getFileNameWithoutExtensionU(fshp);
        fdbf = new File(fshp.getParentFile(), name + ".dbf");
        FileUtils.copy(WMSTest.class.getResourceAsStream("cantons.dbf"), fdbf);
        fprj = new File(fshp.getParentFile(), name + ".prj");
        FileUtils.copy(WMSTest.class.getResourceAsStream("cantons.prj"), fprj);
        fshx = new File(fshp.getParentFile(), name + ".shx");
        FileUtils.copy(WMSTest.class.getResourceAsStream("cantons.shx"), fshx);

        SourceManager sm = Services.getService(DataManager.class).getSourceManager();
        sm.register("cantons", fshp);
    }

    /**
     *
     */
    @After
    public void tearDown() {
        wms.destroy();
        FileUtils.deleteDir(f);
        FileUtils.deleteDir(fshp);
        FileUtils.deleteDir(fdbf);
        FileUtils.deleteDir(fshx);
        FileUtils.deleteDir(fprj);
    }

    @Test
    public void testReprojection() throws Exception {
        DummyResponse r = new DummyResponse("http://localhost:9000/wms/wms");
        HashMap<String, String[]> h = new HashMap<String, String[]>();
        final String toCRS = "EPSG:4326";
        h.put("REQUEST", new String[]{"GetMap"});
        h.put("SERVICE", new String[]{"WMS"});
        h.put("LAYERS", new String[]{"cantons"});
        h.put("STYLES", new String[]{""});
        h.put("CRS", new String[]{toCRS});
        h.put("BBOX", new String[]{"-5.372757617915", "9.326100042301633", "41.3630420705024", "51.089386147807105"});
        h.put("WIDTH", new String[]{"874"});
        h.put("HEIGHT", new String[]{"593"});
        h.put("FORMAT", new String[]{"image/png"});
        h.put("VERSION", new String[]{"1.3.0"});
        h.put("TRANSPARENT", new String[]{"TRUE"});
        // Get the original source
        DataSource source = wms.getContext().getDataManager().getDataSource("cantons");
        source.open();
        Value geom;
        try {
            int geomIndex = MetadataUtilities.getGeometryFieldIndex(source.getMetadata());
            geom = source.getFieldValue(0, geomIndex);
        } finally {
            source.close();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(new File("target/testReprojection.png"));
        wms.processRequests(h, fileOutputStream, r);
        // Get the projection source name
        String sourceName = GetMapHandler.getProjectionSourceName("cantons", toCRS);
        DataSource projSource = wms.getContext().getDataManager().getDataSource(sourceName);
        projSource.open();
        try {
            int geomIndex = MetadataUtilities.getGeometryFieldIndex(projSource.getMetadata());
            Geometry projGeom = projSource.getFieldValue(0, geomIndex).getAsGeometry();
            ST_Transform transformFunction = new ST_Transform();
            Value res = transformFunction.evaluate(wms.getContext().getDataSourceFactory(), geom, ValueFactory.createValue(toCRS));
            assertTrue(res.getAsGeometry().equals(projGeom));
        } finally {
            projSource.close();
        }

    }

    /**
     * Checks the error response for any missing parameter;
     *
     * @throws Exception
     */
    @Test
    public void testParameterErrors() throws Exception {
        DummyResponse r = new DummyResponse("http://localhost:9000/wms/wms");
        HashMap<String, String[]> h = new HashMap<String, String[]>();

        h.put("REQUEST", new String[]{"GetMap"});
        h.put("SERVICE", new String[]{"WMS"});
        h.put("LAYERS", new String[]{"cantons"});
        h.put("STYLES", new String[]{""});
        h.put("CRS", new String[]{"EPSG:27582"});
        h.put("BBOX", new String[]{"2677441.0", "1197822.0", "1620431.0", "47680.0"});
        h.put("WIDTH", new String[]{"874"});
        h.put("HEIGHT", new String[]{"593"});
        h.put("FORMAT", new String[]{"image/png"});

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        wms.processRequests(h, out, r);
        assertEquals(400, r.responseCode);
        assertEquals("text/xml;charset=UTF-8", r.contentType);
        h.put("VERSION", new String[]{"1.3.0"});

        h.remove("REQUEST");
        wms.processRequests(h, out, r);
        assertEquals(400, r.responseCode);
        assertEquals("text/xml;charset=UTF-8", r.contentType);
        h.put("REQUEST", new String[]{"GetMap"});

        h.remove("SERVICE");
        wms.processRequests(h, out, r);
        assertEquals(400, r.responseCode);
        assertEquals("text/xml;charset=UTF-8", r.contentType);
        h.put("SERVICE", new String[]{"WMS"});

        h.remove("CRS");
        wms.processRequests(h, out, r);
        assertEquals(400, r.responseCode);
        assertEquals("text/xml;charset=UTF-8", r.contentType);
        h.put("CRS", new String[]{"EPSG:27582"});

        h.remove("BBOX");
        wms.processRequests(h, out, r);
        assertEquals(400, r.responseCode);
        assertEquals("text/xml;charset=UTF-8", r.contentType);
        h.put("BBOX", new String[]{"2677441.0,1197822.0,1620431.0,47680.0"});

        h.remove("WIDTH");
        wms.processRequests(h, out, r);
        assertEquals(400, r.responseCode);
        assertEquals("text/xml;charset=UTF-8", r.contentType);
        h.put("WIDTH", new String[]{"874"});

        h.remove("HEIGHT");
        wms.processRequests(h, out, r);
        assertEquals(400, r.responseCode);
        assertEquals("text/xml;charset=UTF-8", r.contentType);
        h.put("HEIGHT", new String[]{"593"});

        h.remove("FORMAT");
        wms.processRequests(h, out, r);
        assertEquals(400, r.responseCode);
        assertEquals("text/xml;charset=UTF-8", r.contentType);
    }

    /**
     * Checking the ability to display a map in any supported output format
     *
     * @throws Exception
     */
    @Test
    public void testImageFormat() throws Exception {
        DummyResponse r = new DummyResponse("http://localhost:9000/wms/wms");
        HashMap<String, String[]> h = new HashMap<String, String[]>();

        DataSourceFactory dsf = Services.getService(DataManager.class).getDataSourceFactory();
        dsf.getDataSource("cantons").open();

        h.put("REQUEST", new String[]{"GetMap"});
        h.put("VERSION", new String[]{"1.3.0"});
        h.put("SERVICE", new String[]{"WMS"});
        h.put("LAYERS", new String[]{"cantons"});
        h.put("STYLES", new String[]{""});
        h.put("CRS", new String[]{"EPSG:27582"});
        h.put("BBOX", new String[]{"2677441.0", "1197822.0", "1620431.0", "47680.0"});
        h.put("WIDTH", new String[]{"874"});
        h.put("HEIGHT", new String[]{"593"});
        h.put("FORMAT", new String[]{"image/png"});

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        wms.processRequests(h, out, r);
        assertEquals(200, r.responseCode);
        assertEquals("image/png", r.contentType);

        h.put("FORMAT", new String[]{"image/jpeg"});
        wms.processRequests(h, out, r);
        assertEquals(200, r.responseCode);
        assertEquals("image/jpeg", r.contentType);

        h.put("FORMAT", new String[]{"image/tiff"});
        wms.processRequests(h, out, r);
        assertEquals(200, r.responseCode);
        assertEquals("image/tiff", r.contentType);

    }

    private static class DummyResponse implements WMSResponse {

        private String contentType;
        private String requestUrl;
        private int responseCode;

        DummyResponse(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        @Override
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public String getRequestUrl() {
            return requestUrl;
        }

        @Override
        public void setResponseCode(int code) {
            responseCode = code;
        }
    }
}
