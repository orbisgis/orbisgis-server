/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.


 * Team leader : Erwan Bocher, scientific researcher,

 * User support leader : Gwendall Petit, geomatic engineer.

 * Previous computer developer : Pierre-Yves FADET, computer engineer, Thomas LEDUC,
 * scientific researcher, Fernando GONZALEZ CORTES, computer engineer.

 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC

 * Copyright (C) 2010 Erwan BOCHER, Alexis GUEGANNO, Maxence LAURENT, Antoine GOURLAY

 * Copyright (C) 2012 Erwan BOCHER, Antoine GOURLAY

 * This file is part of OrbisGIS.

 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.

 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.

 * For more information, please consult: <http://www.orbisgis.org/>

 * or contact directly:
 * info@orbisgis.org
 */
package org.orbisgis.server.wms;

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.media.jai.JAI;

/**
 * This class is used to write the image into the output stream
 *
 * @author maxence, Tony MARTIN
 */
public final class MapImageWriter {

        //We store here the formats currently supported. 
        private static final String[] FORMATS = {"image/jpeg", "image/png"};

        private MapImageWriter() {
        }

        /**
         * The write class selects the encoding parameter considering the image
         * format requested. Then, the correct writer will encode the img given
         * by the render into the servlet output stream
         *
         * @param wmsResponse
         * @param output
         * @param format desired image format
         * @param img
         * @param pixelSize
         * @throws UnsupportedEncodingException If the requested format can not
         * be provided by the server
         * @throws IOException If a problem has been encountered while handling
         * the output stream.
         */
        public static void write(WMSResponse wmsResponse, OutputStream output,
                String format, BufferedImage img, double pixelSize) throws IOException {

                if (format.equalsIgnoreCase(FORMATS[0])) {
                        writeJPEG(wmsResponse, output, img, pixelSize);
                } else if (format.equalsIgnoreCase(FORMATS[1])) {
                        writePNG(wmsResponse, output, img, pixelSize);
                } else {
                        throw new UnsupportedEncodingException("Unsupported Format (" + format + ")");
                }
        }

        private static void writeJPEG(WMSResponse wmsResponse, OutputStream output,
                BufferedImage img, double pixelSize) throws IOException {
                wmsResponse.setContentType("image/jpeg");

                JPEGEncodeParam jenc = new JPEGEncodeParam();
                JAI.create("Encode", img, output, "JPEG", jenc);

                output.close();
        }

        private static void writePNG(WMSResponse wmsResponse, OutputStream output,
                BufferedImage img, double pixelSize) throws IOException {
                wmsResponse.setContentType("image/png");

                int dpm = (int) (1000 / pixelSize + 1);
                PNGEncodeParam penc = PNGEncodeParam.getDefaultEncodeParam(img);
                penc.setPhysicalDimension(dpm, dpm, 1);
                JAI.create("Encode", img, output, "PNG", penc);

                output.close();
        }
}