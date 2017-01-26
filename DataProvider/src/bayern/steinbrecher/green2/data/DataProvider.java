/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.data;

import bayern.steinbrecher.green2.utility.URLUtility;
import javafx.scene.image.Image;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delivers access to diffrent application wide usefull paths, icons, etc.
 *
 * @author Stefan Huber
 */
public final class DataProvider {

    /**
     * Containing translations for the system default language.
     */
    public static final ResourceBundle RESOURCE_BUNDLE
            = ResourceBundle.getBundle("bayern.steinbrecher.green2.data.language.language");
    private static final int SPLASHSCREEN_PREFFERED_WIDTH = 800;
    /**
     * The path of the file containing all stylings.
     */
    public static final String STYLESHEET_PATH = "bayern/steinbrecher/green2/data/resources/styles.css";
    /**
     * The os currently operating on. (Only supported os can be set)
     */
    public static final OS CURRENT_OS
            = System.getProperty("os.name").toLowerCase().contains("win") ? OS.WINDOWS : OS.LINUX;
    /**
     * The path to the home directory of the user.
     */
    private static final String HOME_DIR = System.getProperty("user.home").replaceAll("\\\\", "/");
    /**
     * The path where to save files the user wants.
     */
    public static final String SAVE_PATH = CURRENT_OS == OS.WINDOWS ? HOME_DIR + "/Desktop" : HOME_DIR;
    /**
     * The path of the folder where to put user specific data of the
     * application.
     */
    public static final String APP_DATA_PATH = HOME_DIR + (CURRENT_OS == OS.WINDOWS
            ? "/AppData/Roaming/Green2" : "/.Green2");
    /**
     * The path of the local folder where to save the application itself.
     */
    public static final String PROGRAMFOLDER_PATH_LOCAL = CURRENT_OS == OS.WINDOWS
            ? System.getenv("ProgramFiles").replaceAll("\\\\", "/") + "/Green2"
            : "/opt/Green2";
    /**
     * The path of the local version file.
     */
    public static final String VERSIONFILE_PATH_LOCAL = DataProvider.APP_DATA_PATH + "/version.txt";
    /**
     * The URL of the online repository containing the installation files.
     */
    public static final String PROGRAMFOLDER_PATH_ONLINE
            = URLUtility.resolveURL("https://traunviertler-traunwalchen.de/programme")
            .orElse("");
    /**
     * The URL of the file containing the used charset of the zip and its files.
     */
    public static final String CHARSET_PATH_ONLINE = PROGRAMFOLDER_PATH_ONLINE + "/charset.txt";
    /**
     * The URL of the version file describing the version of the files at
     * {@code PROGRAMFOLDER_PATH_ONLINE}.
     */
    public static final String VERSIONFILE_PATH_ONLINE = PROGRAMFOLDER_PATH_ONLINE + "/version.txt";
    /**
     * The URL of the zip containing the installation files of the application.
     */
    public static final String GREEN2_ZIP_URL = PROGRAMFOLDER_PATH_ONLINE + "/Green2.zip";
    private static Profile loadedProfile;

    static {
        //Create configDir if not existing
        new File(DataProvider.APP_DATA_PATH).mkdir();
    }

    private static boolean isLoaded() {
        return loadedProfile != null;
    }

    private DataProvider() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    /**
     * Returns the value behind {@code key} of the resource bundle inserted
     * params.
     *
     * @param key    The key to serach for.
     * @param params The params to insert.
     * @return The value with inserted params.
     */
    public static String getResourceValue(String key, Object... params) {
        if (RESOURCE_BUNDLE.containsKey(key)) {
            return MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
        } else {
            Logger.getLogger(DataProvider.class.getName()).log(Level.INFO, "No resource for \"{0}\" found.", key);
            return key;
        }
    }

    /**
     * Returns a list of values behind {@code key} of the resource bundle and
     * with inserted params.
     *
     * @param key    The key to serach for.
     * @param params The list of params to insert each in the value behind
     *               {@code key}.
     * @return The list of values with inserted params.
     */
    public static List<String> getResourceValues(String key, List<Object[]> params) {
        List<String> values = new ArrayList<>(params.size());
        params.forEach(p -> values.add(getResourceValue(key, p)));
        return values;
    }

    /**
     * Returns the loaded profile.
     *
     * @return Returns the loaded profile.
     */
    public static Profile getProfile() {
        if (isLoaded()) {
            return loadedProfile;
        } else {
            throw new IllegalStateException("No profile loaded yet.");
        }
    }

    public static Profile loadProfile(String profileName, boolean newProfile) {
        return loadProfile(new Profile(profileName, newProfile));
    }

    public static Profile loadProfile(Profile profile) {
        loadedProfile = profile;
        return profile;
    }

    /**
     * Contains enums representing supported operation systems.
     */
    public enum OS {
        /**
         * Representing Windows operating system.
         */
        WINDOWS,
        /**
         * Representing any Linux operating system.
         */
        LINUX
    }

    /**
     * Contains enums representing pictures and icons used in Green2.
     */
    public enum ImageSet {
        /**
         * The Green2 logo.
         */
        LOGO("logo.png"),
        /**
         * The splashscreen of the english version of Green2.
         */
        SPLASHSCREEN_EN("splashscreen_en.png", SPLASHSCREEN_PREFFERED_WIDTH, Double.MAX_VALUE, true, true),
        /**
         * The splashscreen of the german version of Green2.
         */
        SPLASHSCREEN_DE("splashscreen_de.png", SPLASHSCREEN_PREFFERED_WIDTH, Double.MAX_VALUE, true, true);

        private static final String BASIC_ICON_DIR_PATH = "bayern/steinbrecher/green2/data/resources/";
        private Image image;

        ImageSet(String filename) {
            image = new Image(BASIC_ICON_DIR_PATH + filename);
        }

        ImageSet(String filename, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
            image = new Image(BASIC_ICON_DIR_PATH + filename, requestedWidth, requestedHeight, preserveRatio, smooth);
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
}
