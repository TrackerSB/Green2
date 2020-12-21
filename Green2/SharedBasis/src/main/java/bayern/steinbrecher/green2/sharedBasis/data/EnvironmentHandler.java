package bayern.steinbrecher.green2.sharedBasis.data;

import bayern.steinbrecher.green2.sharedBasis.utility.IOStreamUtility;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * @author Stefan Huber
 */
public final class EnvironmentHandler {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentHandler.class.getName());
    /**
     * Containing translations for the system default language.
     */
    public static final ResourceBundle RESOURCE_BUNDLE
            = ResourceBundle.getBundle("bayern.steinbrecher.green2.sharedBasis.data.language.language");
    /**
     * The name of the folder containing the licenses of Green2.
     */
    private static final String LICENSES_FOLDER_NAME = "licenses";
    private static final String PREFERENCES_SUBKEY = "bayern/steinbrecher/green2";
    /**
     * The node of the user preferences where to put user specific settings of Green2.
     */
    public static final Preferences PREFERENCES_USER_NODE = Preferences.userRoot().node(PREFERENCES_SUBKEY);
    private static final String LAST_SAVE_PATH_KEY = "lastSavePath";
    /**
     * The os currently operating on. (Only supported os can be set)
     */
    public static final OS CURRENT_OS
            = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ? OS.WINDOWS : OS.LINUX;
    /**
     * The path to the home directory of the user.
     */
    private static final String HOME_DIR = System.getProperty("user.home").replaceAll("\\\\", "/");
    /**
     * The path where to save files the user wants.
     */
    private static final String DEFAULT_SAVE_PATH = CURRENT_OS == OS.WINDOWS ? HOME_DIR + "/Desktop" : HOME_DIR;
    /**
     * The path of the jar containing this class.
     */
    private static final Path CURRENT_JAR_PATH = resolveCurrentJarPath();
    /**
     * Saves {@code true} only if this jar is used as library and is not directly included.
     */
    public static final boolean IS_USED_AS_LIBRARY = isUsedAsLibrary();
    /**
     * The root path of Green2 application. It is the path of the current jar if this class is directly included in
     * Launcher (Launcher SingleJar version).
     */
    public static final Path APPLICATION_ROOT = resolveApplicationRoot();
    /**
     * The path of the folder containing the licenses of Green2.
     */
    private static final Path LICENSES_PATH = Paths.get(APPLICATION_ROOT.toString(), LICENSES_FOLDER_NAME);
    /**
     * The name of the folder which should contain the application.
     */
    public static final String APPLICATION_FOLDER_NAME = "Green2";
    /**
     * The root of the folder where to put user specific data of the application like profiles or information last time
     * inserted in the SEPA form.
     */
    public static final String APP_DATA_PATH = HOME_DIR + (CURRENT_OS == OS.WINDOWS
            ? "/AppData/Roaming/" : "/.config/") + APPLICATION_FOLDER_NAME;
    private static Property<Profile> loadedProfile = new SimpleObjectProperty<>();

    static {
        //Create configDir if not existing
        File appdataDir = new File(EnvironmentHandler.APP_DATA_PATH);
        if (!appdataDir.exists() && !appdataDir.mkdirs()) {
            LOGGER.log(Level.WARNING, "At least some of the directories of the path {0} could not be created.",
                    EnvironmentHandler.APP_DATA_PATH);
        }
    }

    private EnvironmentHandler() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    private static boolean isUsedAsLibrary() {
        Path root = CURRENT_JAR_PATH;
        Path rootFileName;
        do {
            rootFileName = root.getFileName();
            root = root.getParent();
        } while (!(root == null || rootFileName == null || rootFileName.toString().equals(APPLICATION_FOLDER_NAME)));

        return !(root == null || rootFileName == null);
    }

    private static Path resolveApplicationRoot() {
        Path applicationRoot;
        if (IS_USED_AS_LIBRARY) {
            Path root = CURRENT_JAR_PATH;
            Path rootFileName;
            do {
                root = root.getParent();
                if (root == null) {
                    break;
                } else {
                    rootFileName = root.getFileName();
                }
            } while (!(rootFileName == null || rootFileName.toString().equals(APPLICATION_FOLDER_NAME)));
            applicationRoot = root;
        } else {
            applicationRoot = CURRENT_JAR_PATH.getParent();
        }
        return applicationRoot;
    }

    private static Path resolveCurrentJarPath() {
        try {
            return Paths.get(EnvironmentHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toAbsolutePath();
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Could not resolve location of this jar.", ex);
        }
    }

    private static boolean isLoaded() {
        return loadedProfile != null;
    }

    /**
     * Returns the value behind {@code key} of the resource bundle inserted params.
     *
     * @param key The key to search for.
     * @param params The params to insert.
     * @return The value with inserted params.
     */
    public static String getResourceValue(String key, Object... params) {
        String resourceValue;
        if (RESOURCE_BUNDLE.containsKey(key)) {
            resourceValue = MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
        } else {
            LOGGER.log(Level.WARNING, "No resource for \"{0}\" found.", key);
            String paramsList = Arrays.stream(params).map(Object::toString).collect(Collectors.joining(", "));
            resourceValue = String.format("%s (%s)", key, paramsList);
        }
        return resourceValue;
    }

    /**
     * Returns a list of values behind {@code key} of the resource bundle and with inserted params.
     *
     * @param key The key to search for.
     * @param params The list of params to insert each in the value behind {@code key}.
     * @return The list of values with inserted params.
     */
    public static List<String> getResourceValues(String key, List<Object[]> params) {
        List<String> values = new ArrayList<>(params.size());
        params.stream().forEachOrdered(p -> values.add(getResourceValue(key, p)));
        return values;
    }

    /**
     * Returns the property holding the currently loaded profile.
     *
     * @return The property holding the currently loaded profile. Contains {@code null} if no profile is loaded.
     */
    public static Property<Profile> loadedProfileProperty() {
        return loadedProfile;
    }

    /**
     * Returns the currently loaded profile.
     *
     * @return The currently loaded profile. The returned value is never {@code null}.
     */
    public static Profile getProfile() {
        if (isLoaded()) {
            return loadedProfile.getValue();
        } else {
            throw new IllegalStateException("No profile loaded yet.");
        }
    }

    /**
     * Loads the profile with name {@code profileName}.
     *
     * @param profileName The name of the profile to load.
     * @param newProfile {@code true} if it is new and does not exist yet.
     * @return The loaded profile.
     */
    public static Profile loadProfile(String profileName, boolean newProfile) {
        return loadProfile(new Profile(profileName, newProfile));
    }

    /**
     * Loads the given profile.
     *
     * @param profile The profile to load.
     * @return The profile itself. (This may be used for chaining)
     */
    public static Profile loadProfile(Profile profile) {
        //TODO Does it cause trouble when changing the profile?
        loadedProfile.setValue(profile);
        return profile;
    }

    /**
     * Returns a list of all files of the licenses directory.
     *
     * @return The list of all files of the licenses directory.
     */
    public static List<File> getLicenses() {
        List<File> licences;
        try {
            licences = Files.list(LICENSES_PATH)
                    .map(path -> new File(path.toUri()))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not find licenses. Skip menu entry.", ex);
            licences = new ArrayList<>();
        }
        return licences;
    }

    /**
     * Opens a dialog asking the user to choose a directory based on the directory selected last time which is updated
     * on success.
     *
     * @param owner The owner of the dialog.
     * @param filePrefixKey The key of the name of the file which may be extended by a number if it already exists.
     * @param fileEnding The format of the file. NOTE: Without leading point.
     * @param filePrefixKeyArguments Arguments passed when resolving {@code filePrefixKey}.
     * @return The chosen directory or {@link Optional#empty()} if no directory was chosen.
     * @see IOStreamUtility#askForSavePath(javafx.stage.Stage, java.lang.String, java.lang.String, java.lang.String)
     * @see #getResourceValue(java.lang.String, java.lang.Object...)
     */
    public static Optional<File> askForSavePath(Stage owner, String filePrefixKey, String fileEnding,
            Object... filePrefixKeyArguments) {
        File initialDirectory = new File(PREFERENCES_USER_NODE.get(LAST_SAVE_PATH_KEY, DEFAULT_SAVE_PATH));
        if (!initialDirectory.exists()) {
            initialDirectory = new File(DEFAULT_SAVE_PATH);
        }

        String filePrefix = getResourceValue(filePrefixKey, filePrefixKeyArguments);
        Optional<File> chosenFile
                = IOStreamUtility.askForSavePath(owner, filePrefix, fileEnding, initialDirectory.getAbsolutePath());
        chosenFile.ifPresent(file -> {
            String parentDir = file.getParent();
            if (parentDir != null) {
                PREFERENCES_USER_NODE.put(LAST_SAVE_PATH_KEY, parentDir);
            }
        });
        return chosenFile;
    }

    /**
     * Contains supported operation systems.
     */
    public enum OS {
        /**
         * Representing Windows operating system.
         */
        WINDOWS,
        /**
         * Representing any Linux operating system.
         */
        LINUX;
    }

    /**
     * Contains enums representing pictures and icons used in Green2.
     */
    public enum ImageSet {

        /**
         * A plus.
         */
        ADD("add.png", false),
        /**
         * A pencil.
         */
        EDIT("edit.png", false),
        /**
         * A bin.
         */
        TRASH("trash.png", false),
        /**
         * An info sign.
         */
        INFO("info.png", true),
        /**
         * An info sign.
         */
        INFO_SMALL("info.png", false),
        /**
         * An error sign.
         */
        ERROR("error.png", true),
        /**
         * An error sign.
         */
        ERROR_SMALL("error.png", false),
        /**
         * A warning sign.
         */
        WARNING("warning.png", true),
        /**
         * A warning sign.
         */
        WARNING_SMALL("warning.png", false),
        /**
         * An image for confirmations or checklists.
         */
        CHECKED("checked.png", true),
        /**
         * A key.
         */
        KEY("key.png", false),
        /**
         * A locked lock.
         */
        LOCKED("locked.png", false),
        /**
         * An id card symol.
         */
        ID_CARD("id-card.png", false),
        /**
         * An arrow to the right.
         */
        NEXT("next.png", false),
        /**
         * An arrow to the left.
         */
        BACK("back.png", false),
        /**
         * A check mark.
         */
        SUCCESS("success.png", false),
        /**
         * A credit card symbol.
         */
        CREDIT_CARD("credit-card.png", false),
        /**
         * An image of a bank.
         */
        BANK("back.png", false);

        /**
         * Width/height when requiring a big version of an image.
         */
        public static final int BIG_SIZE = 50;
        /**
         * Width/height when requiring a small version of an image.
         */
        public static final int SMALL_SIZE = 15;
        private static final String BASIC_ICON_DIR_PATH = "/bayern/steinbrecher/green2/sharedBasis/icons/";
        private final Image image;

        ImageSet(String filename, boolean big) {
            int size = big ? BIG_SIZE : SMALL_SIZE;
            image = new Image(
                    getClass().getResource(BASIC_ICON_DIR_PATH + filename).toExternalForm(),
                    size, size, true, true);
        }

        /**
         * Returns the image this enum represents.
         *
         * @return The image this enum represents.
         */
        public Image get() {
            return image;
        }

        /**
         * Returns the image as {@link ImageView} of size of the image.
         *
         * @return The image as {@link ImageView}.
         */
        public ImageView getAsImageView() {
            ImageView imageView = new ImageView(get());
            imageView.setSmooth(true);
            return imageView;
        }
    }
}
