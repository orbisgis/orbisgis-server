/*
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

import org.junit.Before;
import org.junit.Test;
import org.orbisgis.core.renderer.se.Style;
import org.orbisgis.core.workspace.CoreWorkspace;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

/**
 * Tests on the SLD class.
 * @author Alexis Guéganno
 */
public class SLDTest  {

        public static final String path = "/src/test/resources/org/orbisgis/server/wms/lines.sld";

        @Before
        public void setUp() throws Exception {
                File f = File.createTempFile("wms", null);
                f.delete();
                CoreWorkspace c = new CoreWorkspace();
                c.setWorkspaceFolder(f.getAbsolutePath());
                WMS wms = new WMS();
                WMSProperties props = new WMSProperties();
                props.putProperty(WMSProperties.TITLE,"test");
                wms.init(c, Collections.<String, Style>emptyMap(), Collections.<String, String[]>emptyMap(), props);
        }

        @Test
        public void testSLD() throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append("file://");
                sb.append(System.getProperty("user.dir"));
                sb.append(path);
                SLD sld = new SLD(sb.toString());
                assertTrue(sld.getSLDLayers().size()==1);
        }

}
