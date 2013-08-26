package utils;

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

import com.typesafe.plugin.*;

/**
 * this is a helper to send emails, via the typesafe plugin
 */
public class MailHelper {
    public String sender = "Test <test@gmail.com>";
    public String recipient = "Test <test@gmail.com>";
    public String subject = "";
    private String content ="";

    /**
     * Sets the content of the verification email with the right URL
     * @param URLverification The URL needed to verify the email of the user (see routes)
     */
    public void setContentAtSignUp(String URLverification) {
        this.content =
                    "Click here to verify your email for you " +
                    "subscription to orbisGIS services : " +
                    URLverification;
    }

    /**
     * Sets the content of the verification email with the right URL
     * @param URLResetPass The URL needed to reset the password of a user (see routes)
     */
    public void setContentAtForgotPass(String URLResetPass) {
        this.content =
                        "Click here to change your password for OrbisGis Services " +
                        URLResetPass;
    }

    /**
     * Sends a verification email email
     * to change the configuration (SMTP host, ssl, user, password...) Go to application.conf
     */
    public void SendMail() {
        MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
        mail.setSubject(subject); //Specify subject of the mail
        mail.addRecipient(recipient);
        mail.addFrom(sender);
        //sends html
        mail.send(content);
    }
}
