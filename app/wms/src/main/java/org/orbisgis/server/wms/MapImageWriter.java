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
package org.orbisgis.server.wms;

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.media.jai.JAI;

/**
 * This class is used to write the image into the output stream
 *
 * @author maxence, Tony MARTIN
 */
public final class MapImageWriter {

        /**
         * Supported image formats
         */
        static final String[] FORMATS = {"image/jpeg", "image/png", "image/tiff"};

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
         * @param pixelSize be provided by the server
         * @throws IOException If a problem has been encountered while handling
         * the output stream.
         */
        public static void write(WMSResponse wmsResponse, OutputStream output,
                String format, BufferedImage img, double pixelSize) throws IOException {

                if (format.equalsIgnoreCase(ImageFormats.JPEG.toString())) {
                        writeJPEG(wmsResponse, output, img);
                        wmsResponse.setResponseCode(200);
                } else if (format.equalsIgnoreCase(ImageFormats.PNG.toString())) {
                        writePNG(wmsResponse, output, img, pixelSize);
                        wmsResponse.setResponseCode(200);                
                } else if (format.equalsIgnoreCase(ImageFormats.TIFF.toString())) {
                        writeTIFF(wmsResponse, output, img, pixelSize);
                        wmsResponse.setResponseCode(200);
                }
                else {
                        WMS.exceptionDescription(wmsResponse, output, "The format requested is invalid. Please check the server capabilities to ask for a supported format.");
                }
        }

        /**
         * This method permits to write the result image from the wms request in a jpeg format
         * @param wmsResponse
         * @param output
         * @param img
         * @throws IOException 
         */
        private static void writeJPEG(WMSResponse wmsResponse, OutputStream output,
                BufferedImage img) throws IOException {
                wmsResponse.setContentType(ImageFormats.JPEG.toString());

                JPEGEncodeParam jenc = new JPEGEncodeParam();
                JAI.create("Encode", img, output, "JPEG", jenc);

                output.close();
        }

        /**
         * This method permits to write the result image from the wms request in a png format
         * @param wmsResponse
         * @param output
         * @param img
         * @param pixelSize
         * @throws IOException 
         */
        private static void writePNG(WMSResponse wmsResponse, OutputStream output,
                BufferedImage img, double pixelSize) throws IOException {
                wmsResponse.setContentType(ImageFormats.PNG.toString());

                int dpm = (int) (1000 / pixelSize + 1);
                PNGEncodeParam penc = PNGEncodeParam.getDefaultEncodeParam(img);
                penc.setPhysicalDimension(dpm, dpm, 1);
                JAI.create("Encode", img, output, "PNG", penc);

                output.close();
        }
        
        /**
         * This method permits to write the result image from the wms request in a tiff format
         * @param wmsResponse
         * @param output
         * @param img
         * @throws IOException 
         */
        private static void writeTIFF(WMSResponse wmsResponse, OutputStream output,
                BufferedImage img, double pixelSize) throws IOException {
                wmsResponse.setContentType(ImageFormats.TIFF.toString());
                int XRES_TAG = 282;
                int YRES_TAG = 283;
                int dpm = (int) (1000 / pixelSize + 1);
                long[] resolution = { dpm, 1 };
                
                TIFFField xRes = new TIFFField(XRES_TAG,
                TIFFField.TIFF_RATIONAL, 1, new long[][] { resolution });
                TIFFField yRes = new TIFFField(YRES_TAG,
                TIFFField.TIFF_RATIONAL, 1, new long[][] { resolution });
                TIFFEncodeParam tep = new TIFFEncodeParam();
                tep.setExtraFields(new TIFFField[] { xRes, yRes });                 
                JAI.create("Encode", img, output, "TIFF", tep);
                output.close();
        }
}