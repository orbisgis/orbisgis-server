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
import java.sql.*;


public class Workspace {
    private Long id_creator = null;
    private String name = "default";
    private Integer isPublic = new Integer(0); // 0 or 1

    /**
     * Constructor of the workspace
     * @param id_creator
     * @param name
     * @param aPublic
     */
    public Workspace(Long id_creator, String name, Integer aPublic) {
        this.id_creator = id_creator;
        this.name = name;
        isPublic = aPublic;
    }

    /**
     * Saves the instantiated workspace into database
     * @param con The connection to the database
     */
    public Long saveWorkspace(Connection con) {
        String value1 = MapCatalog.refactorToSQL(id_creator);;
        String value2 = MapCatalog.refactorToSQL(name);
        String value3 = MapCatalog.refactorToSQL(isPublic);

        String query = "INSERT INTO workspace (id_creator,name,isPublic) " +
                        "VALUES (" + value1 + "," + value2 + "," + value3 + ");";
        return(MapCatalog.executeSQLupdate(con, query));
    }
}
