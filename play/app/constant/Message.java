package constant;

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

/**
 * Stored constants String for messages, use this class to quicly implement an Error of Information message
 */
public class Message {
    //Errors
    public static final String ERROR_GENERAL = "An error as occurred, try again later, or contact a moderator";
    public static final String ERROR_LOGIN = "Email or password invalid";
    public static final String ERROR_EMAIL_USED = "Email already used";
    public static final String ERROR_ALREADY_LOGGED = "You must log out to create another account";
    public static final String ERROR_UNAUTHORIZED_WORKSPACE = "You don't have the necessary rights in this workspace to do that, monitor it to demand them";
    public static final String ERROR_ALREADY_MONITORING = "You are already monitoring this workspace";
    public static final String ERROR_FILE_INVALID = "The file is missing or invalid";

    //Info
    public static final String INFO_WORKSPACE_CREATED = "You successfully created a workspace!";
    public static final String INFO_FOLDER_CREATED = "You successfully created a folder!";
    public static final String INFO_WORKSPACE_MONITORED = "You are now monitoring this workspace";
    public static final String INFO_FOLDER_DELETED = "The folder has been successfully deleted";
    public static final String INFO_WORKSPACE_DELETED = "The workspace has been successfully deleted";
    public static final String INFO_OWS_CREATED = "You successfully uploaded an OWS Context";
    public static final String INFO_OWS_DELETED = "You successfully deleted an OWS Context";
    public static final String INFO_STOP_MONITORING = "You are no longer monitoring this workspace";
}
