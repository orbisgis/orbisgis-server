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
import java.sql.Connection;

public class UserWorkspace {
    private Long id_user;
    private Long id_workspace;
    private Integer read = 0;
    private Integer write = 0;
    private Integer manageUser = 0;

    public UserWorkspace(Long id_user, Long id_workspace, Integer read, Integer write, Integer manageUser) {
        this.id_user = id_user;
        this.id_workspace = id_workspace;
        this.read = read;
        this.write = write;
        this.manageUser = manageUser;
    }

    public Long saveUserWorkspace(Connection con) {
        String value1 = MapCatalog.refactorToSQL(id_user);
        String value2 = MapCatalog.refactorToSQL(id_workspace);
        String value3 = MapCatalog.refactorToSQL(read);
        String value4 = MapCatalog.refactorToSQL(write);
        String value5 = MapCatalog.refactorToSQL(manageUser);

        String query = "INSERT INTO user_workspace (id_user,id_workspace,READ, WRITE, MANAGE_USER) " +
                "VALUES (" + value1 + "," + value2 + "," + value3 + "," + value4 + "," + value5 +");";
        return(MapCatalog.executeSQLupdate(con, query));
    }
}
