
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
import play.Logger;
import play.mvc.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import play.test.*;
import play.libs.F.*;
import utils.TestUnitHelper;

import java.util.HashMap;
import java.util.Map;

public class FunctionalTest {
    private static FakeApplication app;

    @Before
    public void startApp() throws Exception {
        // Set up connection to test database, different from main database. Config better should be used instead of hard-coding.
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("db.default.url","jdbc:h2:~/testunit");
        settings.put("db.default.user","sa");
        settings.put("db.default.password","");
        app = Helpers.fakeApplication(settings);
        Helpers.start(app);
    }


    @Test
    public void homeSimpleTest(){
        Result result = callAction(
                controllers.routes.ref.General.home()
        );
        assertThat(status(result)).isEqualTo(OK);
    }

    @After
    public void stopApp() {
        Helpers.stop(app);
    }
}
