/*
 * Copyright (C) 2017 Stefan Huber
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

import bayern.steinbrecher.gruen2.utility.IOStreamUtility;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains all settings of a profile.
 *
 * @author Stefan Huber
 */
public class Profile {

    private static final String CONFIGFILE_FORMAT = ".conf";
    private static final String ORIGINATORFILE_FORMAT = ".properties";
    /**
     * The symbol used to separate the config key on the left side and the value
     * on the right side in gruen2.conf.
     */
    public static final String VALUE_SEPARATOR = ":";
    /**
     * The configurations found in gruen2.conf.
     */
    //FIXME Java 9: Replace Property<String> with Property<?>
    private ObservableMap<ConfigKey, Property<String>> configurations = FXCollections.observableHashMap();
    /**
     * {@code true} only if all allowed configurations are specified.
     */
    private boolean allConfigurationsSet;
    /**
     * Representing a function for calculating whether a person with a specific
     * age gets notified.
     */
    private IntFunction<Boolean> ageFunction;
    private StringProperty profileName = new SimpleStringProperty();
    private boolean newProfile;
    /**
     * The path where the file containing information about the last valid
     * inserted data about an originator of a SEPA direct debit. (There does
     * need to be a file yet)
     */
    private StringExpression originatorInfoPath
            = new SimpleStringProperty(DataProvider.APP_DATA_PATH)
            .concat("/")
            .concat(profileName)
            .concat(ORIGINATORFILE_FORMAT);
    private Property<File> originatorInfoFile = new SimpleObjectProperty<>();
    /**
     * The path to the configfile. (May not exist, yet)
     */
    private StringExpression configFilePath
            = new SimpleStringProperty(DataProvider.APP_DATA_PATH)
            .concat("/")
            .concat(profileName)
            .concat(CONFIGFILE_FORMAT);
    /**
     * The file to the configurations for Grün2.
     */
    private Property<File> configFile = new SimpleObjectProperty<>();
    private boolean deleted = false;

    public Profile(String profileName, boolean newProfile) {
        configurations.addListener((InvalidationListener) listener -> {
            allConfigurationsSet = configurations.size() >= ConfigKey.values().length;
        });
        configFile.addListener((obs, oldVal, newVal) -> {
            configurations.putAll(readConfigs(newVal));
            ageFunction = readAgeFunction(configurations.getOrDefault(
                    ConfigKey.BIRTHDAY_EXPRESSION, new SimpleStringProperty("")).getValue());
        });
        configFilePath.addListener((obs, oldVal, newVal) -> configFile.setValue(new File(newVal)));
        originatorInfoPath.addListener((obs, oldVal, newVal) -> originatorInfoFile.setValue(new File(newVal)));

        this.profileName.setValue(profileName);
        this.newProfile = newProfile;

        checkProfile();
        try {
            configFile.getValue().createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    private static ObservableMap<ConfigKey, Property<String>> readConfigs(File configFile) {
        Map<ConfigKey, Property<String>> configurations = new HashMap<>();

        String[] parts;
        try (Scanner sc = new Scanner(configFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                parts = line.split(VALUE_SEPARATOR);
                if (parts.length != 2) {
                    Logger.getLogger(DataProvider.class.getName())
                            .log(Level.WARNING, "\"{0}\" has not exactly two elements. It remains ignored.", line);
                } else {
                    ConfigKey key = ConfigKey.valueOf(parts[0].toUpperCase());
                    Property<String> value = new SimpleObjectProperty<>(parts[1]);
                    if (key.isValid(value.getValue())) {
                        configurations.put(key, value);
                    } else {
                        Logger.getLogger(Profile.class.getName())
                                .log(Level.WARNING, key + " has an invalid value. It is skipped.");
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return FXCollections.observableMap(configurations);
    }

    private static IntFunction<Boolean> readAgeFunction(String birthdayExpression) {
        Set<IntFunction<Boolean>> ageFunctionParts = Arrays.stream(birthdayExpression.split(","))
                .filter(s -> !s.isEmpty())
                .map(part -> {
                    IntFunction<Boolean> functionPart = age -> false;
                    try {
                        switch (part.charAt(0)) {
                            case '>':
                                switch (part.charAt(1)) {
                                    case '=':
                                        functionPart = age -> age >= new Integer(part.substring(2));
                                        break;
                                    default:
                                        functionPart = age -> age > new Integer(part.substring(1));
                                }
                                break;
                            case '<':
                                switch (part.charAt(1)) {
                                    case '=':
                                        functionPart = age -> age <= new Integer(part.substring(2));
                                        break;
                                    default:
                                        functionPart = age -> age < new Integer(part.substring(1));
                                }
                                break;
                            case '=':
                                functionPart = age -> age == new Integer(part.substring(1));
                                break;
                            default:
                                Logger.getLogger(DataProvider.class.getName())
                                        .log(Level.WARNING, "{0} gets skipped", part);
                        }
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(Profile.class.getName()).log(Level.WARNING, "{0} gets skipped", part);
                    }
                    return functionPart;
                })
                .collect(Collectors.toSet());

        if (ageFunctionParts.isEmpty()) {
            return age -> false;
        } else {
            return age -> ageFunctionParts.parallelStream()
                    .anyMatch(intFunc -> intFunc.apply(age));
        }
    }

    private void checkProfile() {
        if (newProfile) {
            if (configFile.getValue().exists()) {
                throw new IllegalArgumentException(
                        "Profile " + profileName.get() + " already exists");
            }
        } else {
            if (!configFile.getValue().exists()) {
                throw new IllegalArgumentException(
                        "Profile " + profileName.get() + " does not exist");
            }
        }
    }

    private void checkDeleted() {
        if (deleted) {
            throw new IllegalStateException("Profile was deleted.");
        }
    }

    private String generateLine(ConfigKey key) {
        return key.name() + VALUE_SEPARATOR + key.getStringFromValue(configurations.get(key).getValue()) + "\n";
    }

    /**
     * Writes the current settings into configuration file.
     */
    public void saveSettings() {
        if (!isAllConfigurationsSet()) {
            throw new IllegalStateException("You have to set all configurations first");
        }
        newProfile = false;

        String out = Arrays.stream(ConfigKey.values())
                .map(this::generateLine)
                .collect(Collectors.joining("\n"));
        IOStreamUtility.printContent(out, configFilePath.get(), false);
    }

    /**
     * Renames the profile if no other profile with name {@code newName} exists.
     * If this profile already is named {@code newName} nothing happens. This
     * method will affect the profile immediatly.
     *
     * @param newName The new profile name.
     */
    public synchronized void renameProfile(String newName) {
        checkDeleted();
        if (!newName.equals(profileName.get())) {
            String newConfigPath = DataProvider.APP_DATA_PATH
                    + "/" + newName + CONFIGFILE_FORMAT;
            File newConfigFile = new File(newConfigPath);
            String newOriginatorPath = DataProvider.APP_DATA_PATH
                    + "/" + newName + ORIGINATORFILE_FORMAT;
            File newOriginatorFile = new File(newOriginatorPath);

            if (newConfigFile.exists() || newOriginatorFile.exists()) {
                throw new IllegalArgumentException(
                        "Can't rename profile. Profile \""
                                + newName + "\" already exists.");
            }
            configFile.getValue().renameTo(newConfigFile);
            originatorInfoFile.getValue().renameTo(newOriginatorFile);
        }
    }

    public synchronized void deleteProfile() {
        checkDeleted();
        configFile.getValue().delete();
        originatorInfoFile.getValue().delete();
        deleted = true;
    }

    public static List<String> getAvailableProfiles() {
        return Arrays.stream(new File(DataProvider.APP_DATA_PATH)
                .list((dir, name) -> name.endsWith(CONFIGFILE_FORMAT)))
                .map(s -> s.substring(0, s.length() - CONFIGFILE_FORMAT.length()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the value belonging to key {@code key} or {@code defaultValue} if
     * {@code key} could not be found or is not specified.
     *
     * @param <T>          The type of the value {@code key} holds.
     * @param key          The key to search for.
     * @param defaultValue The value to return when {@code key} was not found.
     * @return The value belonging to key {@code key} or {@code defaultValue} if
     * {@code key} could not be found or is not specified.
     */
    public <T> T getOrDefault(ConfigKey key, T defaultValue) {
        //FIXME Wait for JDK 9 in order to use generic enums
        if (!key.getValueClass().isAssignableFrom(defaultValue.getClass())) {
            throw new IllegalArgumentException("Type of defaultValue and the type of the value key represents have to "
                    + "be the same. (Still waiting for generic enums to fix this... :-( )");
        }
        if (configurations.containsKey(key)) {
            String value = (String) configurations.get(key).getValue();
            if (defaultValue instanceof String) {
                return (T) value;
            } else if (defaultValue instanceof Boolean) {
                return (T) Boolean.valueOf(value.equalsIgnoreCase("ja") || value.equalsIgnoreCase("true"));
            } else if (defaultValue instanceof Charset) {
                return (T) Charset.forName(value);
            } else {
                throw new UnsupportedOperationException("Type \""
                        + defaultValue.getClass().getSimpleName()
                        + "\" not supported.");
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the property holding the value of {@code key} or {@code null} if there's no entry (yet) for {@code key}.
     *
     * @param key The key to search for.
     * @return The property holding the value of {@code key} or {@code null} if there's no entry (yet) for {@code key}.
     */
    public ReadOnlyProperty<String> getProperty(ConfigKey key) {
        //FIXME Wait for JDK 9 in order to use generic enums
        return configurations.get(key);
    }

    /**
     * Sets a configuration.
     *
     * @param key   The key of the configuration to set.
     * @param value The value to set for {@code key}.
     * @param <T>   The type of the value.
     */
    public <T> void set(ConfigKey key, T value) {
        //FIXME Wait for JDK 9 in order to use generic enums
        if (!key.isValid(value)) {
            throw new IllegalArgumentException("The given value is not valid for the given key");
        }
        configurations.putIfAbsent(key, new SimpleObjectProperty<>());
        configurations.get(key).setValue(value.toString());
    }

    /**
     * Check whether all configurations are set.
     *
     * @return {@code true} only if all configurations are set.
     */
    public boolean isAllConfigurationsSet() {
        checkDeleted();
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
        checkDeleted();
        return ageFunction;
    }

    /**
     * Returns the path of the file containing originator infos for SEPA Direct
     * Debits. NOTE: It is not guaranteed that this file exists.
     *
     * @return The path of the file containing originator infos for SEPA Direct
     * Debits. NOTE: It is not guaranteed that this file exists.
     */
    public String getOriginatorInfoPath() {
        checkDeleted();
        return originatorInfoPath.get();
    }

    /**
     * Returns the path containing this configurations.
     *
     * @return The path containing this configurations.
     */
    public String getConfigFilePath() {
        checkDeleted();
        return configFilePath.get();
    }

    /**
     * Returns the name of the loaded profile if any.
     *
     * @return The name of the loaded profile.
     */
    public String getProfileName() {
        checkDeleted();
        return profileName.get();
    }

    /**
     * Checks whether this profile is marked as new profile.
     *
     * @return {@code true} only if this profile is marked as new.
     */
    public boolean isNewProfile() {
        checkDeleted();
        return newProfile;
    }
}
