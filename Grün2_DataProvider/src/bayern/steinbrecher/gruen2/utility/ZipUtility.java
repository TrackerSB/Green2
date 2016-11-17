/*
 * Copyright (C) 2016 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.gruen2.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class contains methods for handling zip files.
 *
 * @author Stefan Huber
 */
public final class ZipUtility {

    private ZipUtility() {
        throw new UnsupportedOperationException(
                "Constructtion of an object not allowed.");
    }

    /**
     * Unzips the given file into a temp folder in the systems default temp
     * folder.
     *
     * @param zippedFile The file to unzip.
     * @param outputDir The directory where to put the unzipped files.
     * @param charset The charset of the zip.
     * @throws IOException Thrown only if no temporary directory could be
     * created.
     */
    public static void unzip(File zippedFile, File outputDir, Charset charset)
            throws IOException {
        String outDirPath = outputDir.getAbsolutePath();
        try (ZipInputStream zis = new ZipInputStream(
                new FileInputStream(zippedFile), charset);
                InputStreamReader isr
                = new InputStreamReader(zis, charset)) {
            ZipEntry zipEntry;
            char[] buffer = new char[1024];
            while ((zipEntry = zis.getNextEntry()) != null) {
                File unzippedFile
                        = new File(outDirPath + "/" + zipEntry.getName());
                unzippedFile.getParentFile().mkdirs();
                try (OutputStreamWriter ows = new OutputStreamWriter(
                        new FileOutputStream(unzippedFile), charset)) {
                    int bytesRead;
                    while ((bytesRead = isr.read(buffer)) > 0) {
                        ows.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }
}
