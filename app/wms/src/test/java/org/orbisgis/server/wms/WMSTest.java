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
import java.util.Collections;
import java.util.HashMap;
import org.gdms.source.SourceManager;
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
                wms.init(c, Collections.<String, Style>emptyMap(), Collections.<String, String[]>emptyMap());
                SourceManager sm = Services.getService(DataManager.class).getSourceManager();
                sm.register("cantons", new File("/Users/Croc/Desktop/OrbisWMS/Données/cantons.shp"));
        }

        /**
         *
         */
        @After
        public void tearDown() {
                wms.destroy();
                FileUtils.deleteDir(f);
        }

        /**
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

                //Check the error response for any missing parameter

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
                h.put("FORMAT", new String[]{"image/png"});

                wms.processRequests(h, out, r);
                assertEquals(400, r.responseCode);
                assertEquals("image/png", r.contentType);

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
