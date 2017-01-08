/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.data;

import bayern.steinbrecher.gruen2.utility.URLUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.scene.image.Image;

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
     * The symbol used to separate the config key on the left side and the value
     * on the right side in gruen2.conf.
     */
    public static final String VALUE_SEPARATOR = ":";
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
    private static final String CONFIGFILE_FORMAT = ".conf";
    private static final String ORIGINATORFILE_FORMAT = ".properties";
    private static Optional<DataProvider> loadedProfile = Optional.empty();
    /**
     * The configurations found in gruen2.conf.
     */
    private final Map<ConfigKey, String> configurations;
    /**
     * {@code true} only if all allowed configurations are specified.
     */
    private final boolean allConfigurationsSet;
    /**
     * Representing a function for calculating whether a person with a specific
     * age gets notified.
     */
    private final IntFunction<Boolean> ageFunction;
    private final String profileName;
    /**
     * The path where the file containing information about the last valid
     * inserted data about an originator of a SEPA direct debit. (There does
     * need to be a file yet)
     */
    private final String originatorInfoPath;
    /**
     * The path to the configfile. (May not exist, yet)
     */
    private final String configFilePath;
    /**
     * The file to the configurations for Grün2.
     */
    private final File configFile;

    static {
        //Create configDir if not existing
        new File(DataProvider.APP_DATA_PATH).mkdir();
    }

    private static boolean isLoaded() {
        return loadedProfile.isPresent();
    }

    private static void checkLoaded() {
        if (isLoaded()) {
            throw new UnsupportedOperationException(
                    "There is already a profile loaded.");
        }
    }

    private void checkProfile() {
        if (!configFile.exists()) {
            throw new IllegalArgumentException(
                    "Profile " + profileName + " does not exist");
        }
    }

    private static Map<ConfigKey, String> readConfigs(File configFile) {
        Map<ConfigKey, String> configurations = new HashMap<>();

        String[] parts;
        try (Scanner sc = new Scanner(configFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                parts = line.split(VALUE_SEPARATOR);
                if (parts.length != 2) {
                    Logger.getLogger(DataProvider.class.getName())
                            .log(Level.WARNING, "\"{0}" + "\" has not exactly "
                                    + "two elements. It remains ignored.",
                                    line);
                } else {
                    configurations.put(ConfigKey.valueOf(
                            parts[0].toUpperCase()), parts[1]);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataProvider.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return configurations;
    }

    private static IntFunction<Boolean> readAgeFunction(
            String birthdayExpression) {
        Set<IntFunction<Boolean>> ageFunctionParts = new HashSet<>();
        for (String part : Arrays.asList(birthdayExpression.split(","))) {
            if (part.isEmpty()) {
                continue;
            }
            try {
                switch (part.charAt(0)) {
                    case '>':
                        switch (part.charAt(1)) {
                            case '=':
                                ageFunctionParts.add(age -> {
                                    return age
                                            >= new Integer(part.substring(2));
                                });
                                break;
                            default:
                                ageFunctionParts.add(age -> {
                                    return age > new Integer(part.substring(1));
                                });
                        }
                        break;
                    case '<':
                        switch (part.charAt(1)) {
                            case '=':
                                ageFunctionParts.add(age -> {
                                    return age
                                            <= new Integer(part.substring(2));
                                });
                                break;
                            default:
                                ageFunctionParts.add(age -> {
                                    return age < new Integer(part.substring(1));
                                });
                        }
                        break;
                    case '=':
                        ageFunctionParts.add(age -> {
                            return age == new Integer(part.substring(1));
                        });
                        break;
                    default:
                        Logger.getLogger(DataProvider.class.getName())
                                .log(Level.WARNING, "{0} gets skipped", part);
                }
            } catch (NumberFormatException ex) {
                Logger.getLogger(DataProvider.class.getName())
                        .log(Level.WARNING, "{0} gets skipped", part);
            }
        }

        if (ageFunctionParts.isEmpty()) {
            return age -> false;
        } else {
            return age -> ageFunctionParts.parallelStream()
                    .anyMatch(intFunc -> intFunc.apply(age));
        }
    }

    /**
     * Creates a {@code DataProvider} for given profile.
     *
     * @param profile The name of the profile.
     */
    private DataProvider(String profile) {
        checkLoaded();
        profileName = profile;
        originatorInfoPath
                = APP_DATA_PATH + "/" + profile + ORIGINATORFILE_FORMAT;
        configFilePath = APP_DATA_PATH + "/" + profile + CONFIGFILE_FORMAT;
        configFile = new File(configFilePath);
        checkProfile();

        configurations = readConfigs(configFile);
        ageFunction = readAgeFunction(
                getOrDefaultString(ConfigKey.BIRTHDAY_EXPRESSION, ""));
        allConfigurationsSet
                = configurations.size() == ConfigKey.values().length;
    }

    /**
     * Returns the value belonging to key {@code key} or {@code defaultValue} if
     * {@code key} could not be found or is not specified.
     *
     * @param key The key to search for.
     * @param defaultValue The value to return when {@code key} was not found.
     * @return The value belonging to key {@code key} or {@code defaultValue} if
     * {@code key} could not be found or is not specified.
     */
    public String getOrDefaultString(
            ConfigKey key, String defaultValue) {
        return configurations.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the value belonging to key {@code key} or {@code defaultValue} if
     * {@code key} could not be found or is not specified.
     *
     * @param key The key to search for.
     * @param defaultValue The value to return when {@code key} was not found.
     * @return The value belonging to key {@code key} or {@code defaultValue} if
     * {@code key} could not be found or is not specified.
     */
    public boolean getOrDefaultBoolean(
            ConfigKey key, boolean defaultValue) {
        if (configurations.containsKey(key)) {
            String value = configurations.get(key);
            /*FIXME Legacy checking for old config files containing "ja" instead
             * of "yes".
             */
            return value.equalsIgnoreCase("ja")
                    || value.equalsIgnoreCase("true");
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the value belonging to key {@code key} or {@code defaultValue} if
     * {@code key} could not be found or is not specified.
     *
     * @param key The key to search for.
     * @param defaultValue The value to return when {@code key} was not found.
     * @return The value belonging to key {@code key} or {@code defaultValue} if
     * {@code key} could not be found or is not specified.
     */
    public Charset getOrDefaultCharset(
            ConfigKey key, Charset defaultValue) {
        if (configurations.containsKey(key)) {
            return Charset.forName(configurations.get(key));
        } else {
            return defaultValue;
        }
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
    public static DataProvider getProfile() {
        if (loadedProfile.isPresent()) {
            return loadedProfile.get();
        } else {
            throw new IllegalStateException("No profile loaded yet.");
        }
    }

    public static DataProvider loadProfile(String profileName) {
        DataProvider profile = new DataProvider(profileName);
        loadedProfile = Optional.of(profile);
        return profile;
    }

    public static List<String> getAvailableProfiles() {
        return Arrays.stream(new File(APP_DATA_PATH)
                .list((dir, name) -> name.endsWith(CONFIGFILE_FORMAT)))
                .map(s -> s.substring(0, s.lastIndexOf(".")))
                .collect(Collectors.toList());
    }

    /**
     * Renames the profile if no other profile with name {@code newName} exists.
     * If this profile already is named {@code newName} nothing happens.
     *
     * @param newName The new profile name.
     */
    public synchronized void renameProfile(String newName) {
        if (!newName.equals(profileName)) {
            String newConfigPath
                    = APP_DATA_PATH + "/" + newName + CONFIGFILE_FORMAT;
            configFile.renameTo(new File(newConfigPath));

            String newOriginatorPath
                    = APP_DATA_PATH + "/" + newName + ORIGINATORFILE_FORMAT;
            new File(originatorInfoPath).renameTo(new File(newOriginatorPath));

            loadedProfile = Optional.empty();
            loadProfile(newName);
        }
    }

    /**
     * Check whether all configurations are set.
     *
     * @return {@code true} only if all configurations are set.
     */
    public boolean isAllConfigurationsSet() {
        return allConfigurationsSet;
    }

    /**
     * Returns the function for calculating whether a person of a certain age
     * has to be notified.
     *
     * @return The function for calculating whether a person of a certain age
     * has to be notified.
     */
    public IntFunction<Boolean> getAgeFunction() {
        return ageFunction;
    }

    /**
     * Returns the path of the file containing originator infos for SEPA Direct
     * Debits. NOTE: It is not garanteed that this file exists.
     *
     * @return The path of the file containing originator infos for SEPA Direct
     * Debits. NOTE: It is not garanteed that this file exists.
     */
    public String getOriginatorInfoPath() {
        return originatorInfoPath;
    }

    /**
     * Returns the path containing this configurations.
     *
     * @return The path containing this configurations.
     */
    public String getConfigFilePath() {
        return configFilePath;
    }

    /**
     * Returns the name of the loaded profile if any.
     *
     * @return The name of the loaded profile.
     */
    public String getProfileName() {
        return profileName;
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
        LINUX;
    }
}
