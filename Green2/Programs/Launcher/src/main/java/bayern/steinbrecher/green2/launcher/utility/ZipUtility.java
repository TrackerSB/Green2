package bayern.steinbrecher.green2.launcher.utility;

import bayern.steinbrecher.green2.sharedBasis.utility.IOStreamUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
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
        try (ZipInputStream zipEntryStream = new ZipInputStream(Files.newInputStream(zippedFile.toPath()), charset)) {
            //Cache InputStreamReader for charsets needed for unzipping using the correct encoding.
            Map<Charset, InputStreamReader> cachedReaders = new HashMap<>();

            ZipEntry zipEntry = zipEntryStream.getNextEntry();
            while (zipEntry != null) {
                String zipEntryName = zipEntry.getName();
                String[] zipEntryNameParts = zipEntryName.split("\\.");
                String zipEntryNameFormat = zipEntryNameParts[zipEntryNameParts.length - 1];

                InputStreamReader currentReader;
                Charset currentCharset = SPECIAL_CHARSETS.getOrDefault(zipEntryNameFormat, charset);
                if (cachedReaders.containsKey(currentCharset)) {
                    currentReader = cachedReaders.get(currentCharset);
                } else {
                    //At most as many inpustreams are created as differenz charsets are needed.
                    currentReader = new InputStreamReader(zipEntryStream, currentCharset); //NOPMD
                    cachedReaders.put(currentCharset, currentReader);
                }

                File unzippedFile = new File(outDirPath + "/" + zipEntryName);
                //NOTE The file may be in some subdirectory
                File unzippedFileParent = unzippedFile.getParentFile();
                if (unzippedFileParent.exists() || unzippedFileParent.mkdirs()) {
                    //Each file needs to be written separately.
                    try (OutputStreamWriter osw = new OutputStreamWriter(
                            Files.newOutputStream(unzippedFile.toPath()), currentCharset)) {
                        IOStreamUtility.transfer(currentReader, osw);
                    }
                } else {
                    throw new IOException("The directory where to place the extracted files ("
                            + outputDir.getAbsolutePath() + ") could not be created or at least one of its ancestors.");
                }
                zipEntry = zipEntryStream.getNextEntry();
            }
        }
    }
}
