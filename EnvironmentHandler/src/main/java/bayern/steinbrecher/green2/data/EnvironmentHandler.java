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
package bayern.steinbrecher.green2.data;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Delivers access to different application wide useful paths, icons, etc.
 *
 * @author Stefan Huber
 */
public final class EnvironmentHandler {

    /**
     * Containing the version of this current program.
     */
    public static final String VERSION = "2u13";
    /**
     * Containing translations for the system default language.
     */
    public static final ResourceBundle RESOURCE_BUNDLE
            = ResourceBundle.getBundle("bayern.steinbrecher.green2.data.language.language");
    private static final int SPLASHSCREEN_PREFFERED_WIDTH = 800;
    private static final String RESOURCE_BASE_PATH = "/bayern/steinbrecher/green2/";
    /**
     * The path of the file containing all styles.
     */
    public static final String DEFAULT_STYLESHEET = RESOURCE_BASE_PATH + "styles/styles.css";
    private static final String BASIC_ICON_DIR_PATH = RESOURCE_BASE_PATH + "icons/";
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
            ? "/AppData/Roaming/" : "/.") + APPLICATION_FOLDER_NAME;
    private static Property<Profile> loadedProfile = new SimpleObjectProperty<>();

    static {
        //Create configDir if not existing
        File appdataDir = new File(EnvironmentHandler.APP_DATA_PATH);
        if (!appdataDir.exists() && !appdataDir.mkdirs()) {
            Logger.getLogger(EnvironmentHandler.class.getName())
                    .log(Level.WARNING, "At least some of the directories of the path {0} could not be created.",
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
            return root;
        } else {
            return CURRENT_JAR_PATH.getParent();
        }
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
        if (RESOURCE_BUNDLE.containsKey(key)) {
            return MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
        } else {
            Logger.getLogger(EnvironmentHandler.class.getName()).log(Level.INFO, "No resource for \"{0}\" found.", key);
            return key;
        }
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
        try {
            return Files.list(LICENSES_PATH)
                    .map(path -> new File(path.toUri()))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            Logger.getLogger(EnvironmentHandler.class.getName()).log(Level.WARNING, null, ex);
            return new ArrayList<>();
        }
    }

    /**
     * Opens a dialog asking the user to choose a directory.
     *
     * @param owner The owner of the dialog.
     * @param filePrefix The name of the file which may be extended by a number if it already exists.
     * @param fileEnding The format of the file. NOTE: Without leading point.
     * @return The chosen directory or {@link Optional#empty()} if no directory was chosen.
     */
    public static Optional<File> askForSavePath(Stage owner, String filePrefix, String fileEnding) {
        File initialDirectory = new File(PREFERENCES_USER_NODE.get(LAST_SAVE_PATH_KEY, DEFAULT_SAVE_PATH));
        if (!initialDirectory.exists()) {
            initialDirectory = new File(DEFAULT_SAVE_PATH);
        }
        File initialFile = new File(initialDirectory, filePrefix + "." + fileEnding);
        Random random = new Random();
        while (initialFile.exists()) {
            initialFile = new File(initialDirectory, filePrefix + "_" + random.nextInt(1000) + "." + fileEnding);
        }

        FileChooser saveDialog = new FileChooser();
        saveDialog.setTitle(getResourceValue("save"));
        saveDialog.setInitialDirectory(initialDirectory);
        saveDialog.setInitialFileName(initialFile.getName());
        FileChooser.ExtensionFilter givenExtensionFilter
                = new FileChooser.ExtensionFilter(fileEnding.toUpperCase(Locale.ROOT), "*." + fileEnding);
        saveDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(EnvironmentHandler.getResourceValue("allFiles"), "*.*"),
                givenExtensionFilter);
        saveDialog.setSelectedExtensionFilter(givenExtensionFilter);
        Optional<File> chosenFile = Optional.ofNullable(saveDialog.showSaveDialog(owner));
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
     * Contains logos and splashscreens.
     */
    public enum LogoSet {
        /**
         * The Green2 logo.
         */
        LOGO("logo.png"),
        /**
         * The splashscreen of the english version of Green2.
         */
        SPLASHSCREEN_EN("splashscreen_en.png", SPLASHSCREEN_PREFFERED_WIDTH, Double.MAX_VALUE),
        /**
         * The splashscreen of the german version of Green2.
         */
        SPLASHSCREEN_DE("splashscreen_de.png", SPLASHSCREEN_PREFFERED_WIDTH, Double.MAX_VALUE);

        private final Image image;

        private LogoSet(String filename) {
            image = new Image(BASIC_ICON_DIR_PATH + filename);
        }

        private LogoSet(String filename, double requestedWidth, double requestedHeight) {
            image = new Image(BASIC_ICON_DIR_PATH + filename, requestedWidth, requestedHeight, true, true);
        }

        /**
         * Returns the image this enum represents.
         *
         * @return The image this enum represents.
         */
        public Image get() {
            return image;
        }
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
         * An error sign.
         */
        ERROR("error.png", true),
        /**
         * A warning sign.
         */
        WARNING("warning.png", true),
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
        private final Image image;

        private ImageSet(String filename, boolean big) {
            int size = big ? BIG_SIZE : SMALL_SIZE;
            image = new Image(BASIC_ICON_DIR_PATH + filename, size, size, true, true);
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
