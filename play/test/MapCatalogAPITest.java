
/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 * <p/>
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * <p/>
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 * <p/>
 * This file is part of OrbisGIS.
 * <p/>
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * For more information, please consult: <http://www.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */

import org.junit.*;
import org.orbisgis.server.mapcatalog.MapCatalog;
import org.xml.sax.InputSource;
import play.Logger;
import play.Play;
import play.mvc.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import play.test.*;
import play.libs.F.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MapCatalogAPITest {
    private static FakeApplication app;
    private static MapCatalog mc;

    @BeforeClass
    public static void startApp() throws Exception {
        app = Helpers.fakeApplication();
        Helpers.start(app);
        mc = controllers.MapCatalogC.getMapCatalog();
        mc.setTestEnvironment();
    }

    @Test
    public void homeSimpleTest(){
        Result result = callAction(
                controllers.routes.ref.General.home()
        );
        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void listWorkspacesTest(){
        Result result = callAction(
                controllers.routes.ref.MapCatalogAPI.listWorkspaces()
        );
        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void getContextError(){
        Result result = callAction(
                controllers.routes.ref.MapCatalogAPI.getContext(null, "12")
        );
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void getContextTest(){
        Result result = callAction(
                controllers.routes.ref.MapCatalogAPI.getContext("1","1")
        );
        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void deleteContextTest(){
        Result result = callAction(
                controllers.routes.ref.MapCatalogAPI.deleteContext(null,"2")
        );
        assertThat(status(result)).isEqualTo(NO_CONTENT);
    }

    @Test
    public void listContextsTest(){
        Result result = callAction(
                controllers.routes.ref.MapCatalogAPI.listContexts("1")
        );
        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void addContextFromRootTest(){
        InputSource is = new InputSource(MapCatalog.class.getResourceAsStream("MaCarte.ows"));
        Result result = callAction(
                controllers.routes.ref.MapCatalogAPI.addContextFromRoot("1"),
                fakeRequest().withXmlBody(is)
        );
        assertThat(status(result)).isEqualTo(CREATED);
    }

    @AfterClass
    public static void stopApp() throws SQLException {
        mc.breakTestEnvironment();
        Helpers.stop(app);
    }
}
