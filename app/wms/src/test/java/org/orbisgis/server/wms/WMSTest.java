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

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.core.workspace.CoreWorkspace;

import static org.junit.Assert.*;

/**
 * Tests for WMS.
 */
public class WMSTest {

        WMS wms = new WMS();
        
        @Before
        public void setUp() throws Exception {
                CoreWorkspace c = new CoreWorkspace();
                wms.init(c, Collections.<String, Style>emptyMap(), Collections.<String, String[]>emptyMap());
        }
        
        @After
        public void tearDown() {
                wms.destroy();
        }
        
        @Test
        public void testParameterErrors() throws Exception {
                DummyResponse r = new DummyResponse("http://localhost/");
                HashMap<String, String[]> h = new HashMap<String, String[]>();
                h.put("hello", new String[] { "hi" });
                
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                wms.processRequests(h, out, r);
                
                assertEquals(400, r.responseCode);
                assertEquals("text/xml;charset=UTF-8", r.contentType);
        }
        
        private static class DummyResponse implements WMSResponse {
                
                private String contentType;
                private String requestUrl;
                private int responseCode;

                public DummyResponse(String requestUrl) {
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
