package org.orbisgis.server.mapcatalog; /**
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
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.security.MessageDigest;

/**
 * Java model of the table User
 * @author Mario Jothy
 */
public class User {
    private static MapCatalog MC = new MapCatalog();
    private String name = "";
    private String email = "";
    private String password = "";
    private String avatar; //todo reminder
    private String location = "";

    /**
     * Constructor
     * @param name
     * @param email
     * @param password
     * @param location
     */
    public User(String name, String email, String password, String location) throws NoSuchAlgorithmException {
        this.name = name;
        this.email = email;
        //hashing the password
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte byteData[] = md.digest();
        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        this.password = sb.toString();
        this.location = location;
    }

    /**
     * Method that saves a instantiated User into database. Handles SQL injections.
     * @return The ID of the User just created (primary key)
     */
    public  Long save() {
        Long last = null;
        try{
            String query = "INSERT INTO user (name,email,password,location) VALUES (? , ? , ? , ?);";
            PreparedStatement pstmt = MC.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, location);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next()){
                last = rs.getLong(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return last;
    }

    /**
     * Deletes a user from database
     * @param id_user The primary key of the user
     */
    public static void delete(Long id_user) {
        String query = "DELETE FROM user WHERE id_user = ? ;";
        try{
            PreparedStatement stmt = MC.getConnection().prepareStatement(query);
            stmt.setLong(1, id_user);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
