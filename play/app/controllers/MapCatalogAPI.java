package controllers;

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

import play.mvc.*;
import org.orbisgis.server.mapcatalog.*;
import java.util.ArrayList;
import java.util.List;

import csp.ContentSecurityPolicy;

/**
 * Class for REST APi, this will changes when the API will be rewritten with database
 */
@ContentSecurityPolicy
public class MapCatalogAPI extends Controller {
    private static MapCatalog MC = MapCatalogC.getMapCatalog();

    /**
     * Returns a context
     * @param id the id of the context
     * @return
     */
    public static Result getContext(String id){
        String[] attributes = {"id_owscontext"};
        String[] values = {id};
        List<OWSContext> list = OWSContext.page(MC, attributes, values);
        return ok(list.get(0).getContent());
    }

    /**
     * Deletes a context
     * @param id the id of the context
     * @return
     */
    public static Result deleteContext(String id){
        OWSContext.delete(MC, Long.valueOf(id));
        return noContent();
    }

    /**
     * Get the list of workspaces
     * @return
     */
    public static Result listWorkspaces(){
        return ok(MC.getWorkspaceList());
    }

    /**
     * Get the context list of a workspace
     * @param id_workspace the id of the workspace
     * @return
     */
    public static Result listContexts(String id_workspace){
        return ok(MC.getContextList(id_workspace));
    }
}
