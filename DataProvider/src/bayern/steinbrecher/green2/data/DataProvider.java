/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bayern.steinbrecher.green2.data;

import bayern.steinbrecher.green2.utility.URLUtility;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Delivers access to different application wide useful paths, icons, etc.
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
     * The path of the file containing all styles.
     */
    public static final String STYLESHEET_PATH = "styles.css";
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
     * @param key    The key to search for.
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
     * @param key    The key to search for.
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

    /**
     * Loads the profile with name {@code profileName}.
     *
     * @param profileName The name of the profile to load.
     * @param newProfile  {@code true} if it is new and does not exist yet.
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
        loadedProfile = profile;
        return profile;
    }

    /**
     * Returns a list of all files of the licenses directory.
     *
     * @return The list of all files of the licenses directory.
     */
    public static List<File> getLicenses() {
        try {
            return Files.list(Paths.get(PROGRAMFOLDER_PATH_LOCAL + "/licenses")).map(path -> new File(path.toUri()))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
        SPLASHSCREEN_DE("splashscreen_de.png", SPLASHSCREEN_PREFFERED_WIDTH, Double.MAX_VALUE, true, true),
        /**
         * An image showing a plus sign used to symbol &bdquo;add&ldquo;.
         */
        ADD("icons/add.png"),
        /**
         * An image for editing.
         */
        EDIT("icons/edit.png"),
        /**
         * An image for deleting.
         */
        TRASH("icons/trash.png"),
        /**
         * An image for information.
         */
        INFO("icons/info.png"),
        /**
         * An image for errors.
         */
        ERROR("icons/error.png"),
        /**
         * An image for warnings.
         */
        WARNING("icons/warning.png"),
        /**
         * An image for confirmations or checklists.
         */
        CHECKED("icons/checked.png"),
        /**
         * An image for showing moving forward.
         */
        FAST_FORWARD("icons/fast-forward.png"),
        /**
         * An image for saving.
         */
        SAVE("icons/save.png"),
        /**
         * An image for a secret.
         */
        KEY("icons/save.png"),
        /**
         * An image for passwords.
         */
        LOCKED("icons/locked.png"),
        /**
         * An image for names/logins.
         */
        ID_CARD("icons/id-card.png"),
        /**
         * An image for next.
         */
        NEXT("icons/next.png");

        /**
         * A CSS class which can be set to a control when it uses one of these images as glyphicon.
         */
        public static final String CSS_CLASS_GLYPHICON = "glyphicon";
        private static final String BASIC_ICON_DIR_PATH = "/";
        private Image image;

        ImageSet(String filename) {
            image = new Image(BASIC_ICON_DIR_PATH + filename);
        }

        ImageSet(String filename, double requestedWidth, double requestedHeight, boolean preserveRatio,
                 boolean smooth) {
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

        /**
         * Returns the image as {@link ImageView} of size 15x15.
         *
         * @returns The image as {@link ImageView}.
         */
        public ImageView getAsSmallImageView() {
            ImageView imageView = new ImageView(get());
            imageView.setFitHeight(15);
            imageView.setFitWidth(15);
            return imageView;
        }

        /**
         * Returns the image as {@link ImageView} of size 50x50.
         *
         * @returns The image as {@link ImageView}.
         */
        public ImageView getAsBigImageView() {
            ImageView imageView = new ImageView(get());
            imageView.setSmooth(true);
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            return imageView;
        }
    }
}
