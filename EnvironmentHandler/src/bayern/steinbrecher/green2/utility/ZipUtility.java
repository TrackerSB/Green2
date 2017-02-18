/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class contains methods for handling zip files.
 *
 * @author Stefan Huber
 */
public final class ZipUtility {

    private static final Charset VBS_CHARSET = Charset.forName("Windows-1252");
    private static final Charset JAR_ICO_PNG_CHARSET = StandardCharsets.ISO_8859_1;

    private ZipUtility() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    /**
     * Unzips the given file into the given folder.
     *
     * @param zippedFile The file to unzip.
     * @param outputDir The directory where to put the unzipped files.
     * @param charset The charset of the zip and its files. NOTE: VBS files will be unzipped using Windows-1252 and JAR,
     * PNG and ICO using ISO-8859-1.
     * @throws IOException Thrown only if no temporary directory could be created.
     */
    public static void unzip(File zippedFile, File outputDir, Charset charset) throws IOException {
        String outDirPath = outputDir.getAbsolutePath();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zippedFile), charset);
                InputStreamReader isr = new InputStreamReader(zis, charset);
                InputStreamReader isrVBS = new InputStreamReader(zis, VBS_CHARSET);
                InputStreamReader isrLatin = new InputStreamReader(zis, JAR_ICO_PNG_CHARSET)) {
            ZipEntry zipEntry;
            boolean vbs;
            boolean jar;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String zipEntryName = zipEntry.getName();
                jar = zipEntryName.endsWith(".jar") || zipEntryName.endsWith(".ico") || zipEntryName.endsWith(".png");
                vbs = zipEntryName.endsWith(".vbs");
                File unzippedFile = new File(outDirPath + "/" + zipEntryName);
                unzippedFile.getParentFile().mkdirs();
                try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(unzippedFile),
                        vbs ? VBS_CHARSET : (jar ? JAR_ICO_PNG_CHARSET : charset))) {
                    IOStreamUtility.transfer(vbs ? isrVBS : (jar ? isrLatin : isr), osw);
                }
            }
        }
    }
}
