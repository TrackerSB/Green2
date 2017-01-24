/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.data;

import bayern.steinbrecher.gruen2.utility.URLUtility;
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
            = ResourceBundle.getBundle(
                    "bayern.steinbrecher.gruen2.data.language.language");
    /**
     * The default icon for stages.
     */
    public static final Image DEFAULT_ICON
            = new Image("bayern/steinbrecher/gruen2/data/icon.png");
    private static final int SPLASHSCREEN_PREFFERED_WIDTH = 800;
    /**
     * The image containing the splash screen used when the application is
     * started.
     */
    public static final Image SPLASHSCREEN
            = new Image("bayern/steinbrecher/gruen2/data/splashscreen.png",
                    SPLASHSCREEN_PREFFERED_WIDTH, Double.MAX_VALUE, true, true);
    /**
     * The path of the file containing all stylings.
     */
    public static final String STYLESHEET_PATH
            = "bayern/steinbrecher/gruen2/data/styles.css";
    /**
     * The os currently operating on. (Only supported os can be set)
     */
    public static final OS CURRENT_OS
            = System.getProperty("os.name").toLowerCase().contains("win")
            ? OS.WINDOWS : OS.LINUX;
    /**
     * The path to the home directory of the user.
     */
    private static final String HOME_DIR
            = System.getProperty("user.home").replaceAll("\\\\", "/");
    /**
     * The path where to save files the user wants.
     */
    public static final String SAVE_PATH
            = CURRENT_OS == OS.WINDOWS ? HOME_DIR + "/Desktop" : HOME_DIR;
    /**
     * The path of the folder where to put user specific data of the
     * application.
     */
    public static final String APP_DATA_PATH
            = HOME_DIR + (CURRENT_OS == OS.WINDOWS
                    ? "/AppData/Roaming/Grün2_Mitgliederverwaltung"
                    : "/.Grün2_Mitgliederverwaltung");
    /**
     * The path of the local folder where to save the application itself.
     */
    public static final String PROGRAMFOLDER_PATH_LOCAL
            = CURRENT_OS == OS.WINDOWS
                    ? System.getenv("ProgramFiles").replaceAll("\\\\", "/")
                    + "/Grün2_Mitgliederverwaltung"
                    : "/opt/Grün2_Mitgliederverwaltung";
    /**
     * The path of the local version file.
     */
    public static final String VERSIONFILE_PATH_LOCAL
            = DataProvider.APP_DATA_PATH + "/version.txt";
    /**
     * The URL of the online repository containing the installation files.
     */
    public static final String PROGRAMFOLDER_PATH_ONLINE
            = URLUtility.resolveURL(
                    "https://traunviertler-traunwalchen.de/programme")
                    .orElse("");
    /**
     * The URL of the file containing the used charset of the zip and its files.
     */
    public static final String CHARSET_PATH_ONLINE
            = PROGRAMFOLDER_PATH_ONLINE + "/charset.txt";
    /**
     * The URL of the version file describing the version of the files at
     * {@code PROGRAMFOLDER_PATH_ONLINE}.
     */
    public static final String VERSIONFILE_PATH_ONLINE
            = PROGRAMFOLDER_PATH_ONLINE + "/version.txt";
    /**
     * The URL of the zip containing the installation files of the application.
     */
    public static final String GRUEN2_ZIP_URL
            = PROGRAMFOLDER_PATH_ONLINE + "/Gruen2.zip";
    private static Profile loadedProfile;

    static {
        //Create configDir if not existing
        new File(DataProvider.APP_DATA_PATH).mkdir();
    }

    private static boolean isLoaded() {
        return loadedProfile != null;
    }

    private DataProvider() {
        throw new UnsupportedOperationException(
                "Construction of an object not allowed.");
    }

    /**
     * Returns the value behind {@code key} of the resource bundle inserted
     * params.
     *
     * @param key The key to serach for.
     * @param params The params to insert.
     * @return The value with inserted params.
     */
    public static String getResourceValue(String key, Object... params) {
        if (RESOURCE_BUNDLE.containsKey(key)) {
            return MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
        } else {
            Logger.getLogger(DataProvider.class.getName())
                    .log(Level.INFO, "No resource for \"{0}\" found.", key);
            return key;
        }
    }

    /**
     * Returns a list of values behind {@code key} of the resource bundle and
     * with inserted params.
     *
     * @param key The key to serach for.
     * @param params The list of params to insert each in the value behind
     * {@code key}.
     * @return The list of values with inserted params.
     */
    public static List<String> getResourceValues(
            String key, List<Object[]> params) {
        List<String> values = new ArrayList<>(params.size());
        params.stream().forEachOrdered(p -> {
            values.add(getResourceValue(key, p));
        });
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
}
