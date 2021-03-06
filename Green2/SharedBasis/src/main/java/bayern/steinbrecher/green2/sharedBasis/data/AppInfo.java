package bayern.steinbrecher.green2.sharedBasis.data;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * @author Stefan Huber
 * @since 2u14
 */
public final class AppInfo {

    private static final Logger LOGGER = Logger.getLogger(AppInfo.class.getName());
    public static final String APP_NAME = "Grün2";
    public static final String VERSION = "2u14-RC.1";
    public static final String UPDATE_NAME = "The real update";

    private AppInfo() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }

    /**
     * Returns the date when this jar was compiled. It currrently returns the date of the manifest file of the jar in
     * which the class file of {@link AppInfo} is contained.
     *
     * @return The date when this jar was compiled.
     */
    public static Optional<LocalDateTime> getCompilationDate() {
        String classFileName = AppInfo.class
                .getName()
                .replace('.', '/')
                + ".class";
        Optional<LocalDateTime> compilationDate;
        try {
            URLConnection baseConnection = AppInfo.class
                    .getClassLoader()
                    .getResource(classFileName)
                    .openConnection();
            JarURLConnection jarConnection = (JarURLConnection) baseConnection;
            ZipEntry manifestEntry = jarConnection.getJarFile()
                    .getEntry("META-INF/MANIFEST.MF");
            compilationDate = Optional.ofNullable(manifestEntry == null ? null : manifestEntry.getTimeLocal());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "The compilation date could not be determined.", ex);
            compilationDate = Optional.empty();
        }
        return compilationDate;
    }
}
