package bayern.steinbrecher.gruen2launcher;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Installs Grün2 and checks for updates.
 *
 * @author Stefan Huber
 */
public final class Gruen2Launcher {

    private static final String GRUEN2_HOST
            = "http://www.traunviertler-traunwalchen.de/programme";

    /**
     * Prohibit construction of an object.
     */
    private Gruen2Launcher() {
        throw new UnsupportedOperationException(
                "Construction of an object not allowed.");
    }

    /**
     * Returns the path of the version file.
     *
     * @return The path of the version file.
     */
    private static String getConfigDirPath() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getProperty("user.home").replaceAll("\\\\", "/")
                    + "/AppData/Roaming/Grün2_Mitgliederverwaltung";
        } else {
            return System.getProperty("user.home")
                    + "/.Grün2_Mitgliederverwaltung";
        }
    }

    /**
     * Downloads and installs Grün2.
     */
    private static void downloadAndInstallGruen2(String newVersion)
            throws IOException {
        File tempFile = Files.createTempFile(null, ".zip", new FileAttribute[0])
                .toFile();
        Path tempDir = Files.createTempDirectory(null, new FileAttribute[0]);

        try {
            //Download
            URL downloadUrl = new URL(GRUEN2_HOST + "/Gruen2.zip");
            ReadableByteChannel rbc
                    = Channels.newChannel(downloadUrl.openStream());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }

            try {
                //Unzip
                ZipFile zipFile = new ZipFile(tempFile.getAbsolutePath());
                zipFile.extractAll(tempDir.toString());
            } catch (ZipException ex) {
                Logger.getLogger(Gruen2Launcher.class.getName())
                        .log(Level.SEVERE, null, ex);
            }

            //Install
            Runtime.getRuntime()
                    .exec("cmd /C \"" + tempDir.toString() + "/install.bat\"")
                    .waitFor();

            //Update version.txt
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(getConfigDirPath() + "/version.txt"),
                    "UTF-8"))) {
                //To make no UTF-8 without BOM but with BOM.
                bw.append(newVersion);
            } catch (IOException ex) {
                Logger.getLogger(Gruen2Launcher.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        } catch (MalformedURLException | FileNotFoundException |
                InterruptedException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private static String readOnlineVersion() {
        try {
            URL onlineVersionUrl = new URL(GRUEN2_HOST + "/version.txt");
            Scanner sc = new Scanner(onlineVersionUrl.openStream());
            return sc.nextLine();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gruen2Launcher.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * The starting point of the hole application.
     *
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args)
            throws IOException, InterruptedException {
        File localVersionfile = new File(getConfigDirPath() + "/version.txt");
        String onlineVersion = readOnlineVersion();
        if (!localVersionfile.exists()) {
            downloadAndInstallGruen2(onlineVersion);
            Desktop.getDesktop()
                    .open(new File(getConfigDirPath() + "/Grün2.conf"));
        } else {
            try (Scanner sc = new Scanner(localVersionfile)) {
                String localVersion = sc.nextLine();
                if (!localVersion.equalsIgnoreCase(onlineVersion)) {
                    downloadAndInstallGruen2(onlineVersion);
                }
            }
            Runtime.getRuntime()
                    .exec("cmd /C java -jar Grün2_Mitgliederverwaltung.jar")
                    .waitFor();
        }
    }
}
