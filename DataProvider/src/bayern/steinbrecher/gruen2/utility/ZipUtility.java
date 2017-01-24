/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        throw new UnsupportedOperationException(
                "Constructtion of an object not allowed.");
    }

    /**
     * Unzips the given file into the given folder.
     *
     * @param zippedFile The file to unzip.
     * @param outputDir The directory where to put the unzipped files.
     * @param charset The charset of the zip and its files. NOTE: VBS files will
     * be unzipped using Windows-1252 and JAR, PNG and ICO using ISO-8859-1.
     * @throws IOException Thrown only if no temporary directory could be
     * created.
     */
    public static void unzip(File zippedFile, File outputDir, Charset charset)
            throws IOException {
        String outDirPath = outputDir.getAbsolutePath();
        try (ZipInputStream zis = new ZipInputStream(
                new FileInputStream(zippedFile), charset);
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
                File unzippedFile
                        = new File(outDirPath + "/" + zipEntryName);
                unzippedFile.getParentFile().mkdirs();
                try (OutputStreamWriter osw = new OutputStreamWriter(
                        new FileOutputStream(unzippedFile),
                        vbs ? VBS_CHARSET : (jar ? JAR_ICO_PNG_CHARSET : charset))) {
                    IOStreamUtility.transfer(vbs ? isrVBS : (jar ? isrLatin : isr), osw);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ZipUtility.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
