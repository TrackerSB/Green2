package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.green2.sharedBasis.data.SupportedOS;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathUtility {
    public static final String APPLICATION_FOLDER_NAME = "Green2";
    /**
     * The root folder in case this application is actually installed.
     * NOTE In case this application is executed during development (i.e. without being installed) the path is not
     * guaranteed to exist.
     */
    public static final Path INSTALL_ROOT = Paths.get(
            switch (SupportedOS.CURRENT) {
                case LINUX -> "/opt";
                case WINDOWS -> System.getenv("ProgramFiles").replaceAll("\\\\", "/");
            }, APPLICATION_FOLDER_NAME);
    public static final Path EXECUTABLES_ROOT = INSTALL_ROOT.resolve("bin");
    public static final String LICENSES_FOLDER_NAME = "licenses";
    public static final Path LICENSES_PATH = INSTALL_ROOT.resolve(LICENSES_FOLDER_NAME);

    private PathUtility() {
        throw new UnsupportedOperationException("Construction of instances is prohibited");
    }
}
