/*
 * Copyright (C) 2018 Stefan Huber
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
package bayern.steinbrecher.green2.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class contains methods for handling zip files.
 *
 * @author Stefan Huber
 */
public final class ZipUtility {

    /**
     * The charset used when unzipping binary files.
     */
    private static final Charset BINARY_CHARSET = StandardCharsets.ISO_8859_1;
    /**
     * The {@link Charset}s which are fixed for certain file formats in order to guarantee that these are
     * extracted/compressed correctly.
     */
    public static final Map<String, Charset> SPECIAL_CHARSETS
            = Map.of("jar", BINARY_CHARSET,
                    "ico", BINARY_CHARSET,
                    "png", BINARY_CHARSET,
                    "vbs", Charset.forName("Windows-1252"),
                    "pdf", BINARY_CHARSET);

    private ZipUtility() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    /**
     * Unzips the given file into the given folder.
     *
     * @param zippedFile The file to unzip.
     * @param outputDir The directory where to put the unzipped files.
     * @param charset The charset of the zip and its files. NOTE: For some files like vbs, jar, ico, png and pdf always
     * a special charset is taken which may defer from the given one in order to guarantee the correctness of the
     * unzipped files.
     * @throws IOException Thrown only if no temporary directory could be created.
     * @see #SPECIAL_CHARSETS
     */
    public static void unzip(File zippedFile, File outputDir, Charset charset) throws IOException {
        String outDirPath = outputDir.getAbsolutePath();
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zippedFile.toPath()), charset);
                InputStreamReader isr = new InputStreamReader(zis, charset);) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String zipEntryName = zipEntry.getName();
                String[] zipEntryNameParts = zipEntryName.split("\\.");
                String zipEntryNameFormat = zipEntryNameParts[zipEntryNameParts.length - 1];

                InputStreamReader currentIsr;
                Charset currentCharset;
                if (SPECIAL_CHARSETS.containsKey(zipEntryNameFormat)) {
                    currentCharset = SPECIAL_CHARSETS.get(zipEntryNameFormat);
                    currentIsr = new InputStreamReader(zis, currentCharset);
                } else {
                    currentCharset = charset;
                    currentIsr = isr;
                }

                File unzippedFile = new File(outDirPath + "/" + zipEntryName);
                //NOTE The file may be in some subdirectory
                File unzippedFileParent = unzippedFile.getParentFile();
                if (unzippedFileParent.exists() || unzippedFileParent.mkdirs()) {
                    try (OutputStreamWriter osw
                            = new OutputStreamWriter(Files.newOutputStream(unzippedFile.toPath()), currentCharset)) {
                        IOStreamUtility.transfer(currentIsr, osw);
                    }
                } else {
                    throw new IOException("The directory where to place the extracted files ("
                            + outputDir.getAbsolutePath() + ") could not be created or at least one of its ancestors.");
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }
}
