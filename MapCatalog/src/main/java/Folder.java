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

public class Folder {
    private Long id_root = new Long(0);
    private Long id_parent = null;
    private String name =  "default";

    /**
     * The constructor of the Folder
     * @param id_root
     * @param id_parent Null if there is no parent folder (note: A workspace is NOT a folder)
     * @param name
     */
    public Folder(Long id_root, Long id_parent, String name) {
        this.id_root = id_root;
        this.id_parent = id_parent;
        this.name = name;
    }

    /**
     * Saves the instantiated folder in database
     * @param con
     */
    public Long saveFolder(Connection con){
        String value1 = MapCatalog.refactorToSQL(id_root);
        String value2 = MapCatalog.refactorToSQL(id_parent);
        String value3 = MapCatalog.refactorToSQL(name);

        String query = "INSERT INTO folder (id_root,id_parent,name) " +
                        "VALUES (" + value1 +","+ value2 +","+ value3 +");";
        return(MapCatalog.executeSQLupdate(con, query));
    }
}
