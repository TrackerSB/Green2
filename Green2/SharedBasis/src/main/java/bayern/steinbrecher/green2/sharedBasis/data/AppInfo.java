package bayern.steinbrecher.green2.sharedBasis.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 2u14
 */
public final class AppInfo {

    private static final Logger LOGGER = Logger.getLogger(AppInfo.class.getName());
    /**
     * Containing the version of this current program.
     */
    public static final String VERSION = "2u14";
    /**
     * The name of the update associated with the {@link #VERSION}.
     */
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
    public static final Optional<LocalDateTime> getCompilationDate() {
        Optional<LocalDateTime> compilationDate;
        compilationDate = Optional.empty();
        String classPath = AppInfo.class
                .getResource(AppInfo.class.getSimpleName() + ".class")
                .toString();
        if (classPath.startsWith("jar")) {
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                    + "/META-INF/MANIFEST.MF";
            try {
                Manifest manifest = new Manifest(new URL(manifestPath).openStream());
                System.out.println(manifest.getEntries());
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, "Could not find compilation date.", ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Could not find compilation date.", ex);
            }
        }
//        String classFileName = AppInfo.class
//                .getName()
//                .replace('.', '/')
//                + ".class";
//        try {
//            URLConnection baseConnection = AppInfo.class
//                    .getClassLoader()
//                    .getResource(classFileName)
//                    .openConnection();
//            JarURLConnection jarConnection = (JarURLConnection) baseConnection;
//            ZipEntry manifestEntry = jarConnection.getJarFile()
//                    .getEntry("META-INF/MANIFEST.MF");
//            compilationDate = Optional.ofNullable(manifestEntry == null ? null : manifestEntry.getTimeLocal());
//        } catch (IOException ex) {
//            LOGGER.log(Level.WARNING, "The compilation date could not be determined.", ex);
//            compilationDate = Optional.empty();
//        }
        return compilationDate;
    }
}
