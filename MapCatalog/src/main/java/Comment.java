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
import java.sql.*;

public class Comment {
    private Long id_writer = null;
    private Long id_map = null;
    private String content = "";

    public Comment(Long id_writer, Long id_map, String content) {
        this.id_writer = id_writer;
        this.id_map = id_map;
        this.content = content;
    }

    public Long saveComment(Connection con) {
        String value1 = MapCatalog.refactorToSQL(id_writer);
        String value2 = MapCatalog.refactorToSQL(id_map);
        String value3 = MapCatalog.refactorToSQL(content);

        String query = "INSERT INTO comment (id_writer,id_map,content) " +
                "VALUES (" + value1 + "," + value2 + "," + value3 + ");";
        return(MapCatalog.executeSQLupdate(con, query));
    }
}
