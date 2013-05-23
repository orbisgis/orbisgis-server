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
import org.junit.*;
import java.sql.*;

/**
 * Test class for MapCatalog
 * @author Mario Jothy
 */
public class MapCatalogTest {

    @Test
    public void workspaceCreation () throws SQLException{

        //Creation of the workspace
        Long id_workspace = MapCatalog.createWorkspace(null, "bbb", 0);
        String query = "SELECT id_creator , name , isPublic FROM workspace WHERE id_workspace=" + id_workspace;
        String[] value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value[0]==null          &&
                        value[1].equals("bbb")  &&
                        value[2].equals("0")
        );
        //Deletion of the workspace
        MapCatalog.deleteWorkspace(id_workspace);
        query = "SELECT * FROM workspace WHERE id_workspace="+id_workspace;
        value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value[0]==null
        );
    }

    @Test
    public void folderCreation () throws SQLException{
        //Creation of the folder
        Long id_folder = MapCatalog.createFolder(new Long(1), null, "aaa");
        String query = "SELECT id_root , id_parent , name FROM folder WHERE id_folder=" + id_folder;
        String[] value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value[0].equals("1")    &&
                        value[1] == null        &&
                        value[2].equals("aaa")
        );
        //Deletion of the folder
        MapCatalog.deleteFolder(id_folder);
        query = "SELECT * FROM folder WHERE id_folder="+id_folder;
        value = MapCatalog.executeSQLselect(MapCatalog.getConnection(), query);
        Assert.assertTrue(
                        value[0]==null
        );
    }
}
