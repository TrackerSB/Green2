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

import bayern.steinbrecher.green2.utility.IOStreamUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * Contains all settings of a profile.
 *
 * @author Stefan Huber
 */
public class Profile {

    private static final String CONFIGFILE_FORMAT = ".conf";
    private static final String ORIGINATORFILE_FORMAT = ".properties";
    /**
     * The symbol used to separate the config key on the left side and the value on the right side of profile files.
     */
    public static final String VALUE_SEPARATOR = ":";
    /**
     * The configurations found in a profile file.
     */
    //TODO Think about how to force equallity of these two question marks
    private ObservableMap<ProfileSettings<?>, Property<?>> configurations = FXCollections.observableHashMap();
    /**
     * {@code true} only if all allowed configurations are specified.
     */
    private boolean allConfigurationsSet;
    /**
     * Representing a function for calculating whether a person with a specific age gets notified.
     */
    private IntFunction<Boolean> ageFunction;
    private StringProperty profileName = new SimpleStringProperty();
    private boolean newProfile;
    /**
     * The path where the file containing information about the last valid inserted data about an originator of a SEPA
     * direct debit. (There does need to be a file yet)
     */
    private StringExpression originatorInfoPath
            = new SimpleStringProperty(EnvironmentHandler.APP_DATA_PATH)
                    .concat("/")
                    .concat(profileName)
                    .concat(ORIGINATORFILE_FORMAT);
    private Property<File> originatorInfoFile = new SimpleObjectProperty<>();
    /**
     * The path to the configfile. (May not exist, yet)
     */
    private StringExpression configFilePath
            = new SimpleStringProperty(EnvironmentHandler.APP_DATA_PATH)
                    .concat("/")
                    .concat(profileName)
                    .concat(CONFIGFILE_FORMAT);
    /**
     * The file to the configurations for Green2.
     */
    private Property<File> configFile = new SimpleObjectProperty<>();
    private boolean deleted = false;

    public Profile(String profileName, boolean newProfile) {
        configurations.addListener((InvalidationListener) listener -> {
            allConfigurationsSet = configurations.size() >= ProfileSettings.values().length;
        });
        configFile.addListener((obs, oldVal, newVal) -> {
            configurations.putAll(readConfigs(newVal));
            ageFunction = readAgeFunction((String) configurations.getOrDefault(
                    ProfileSettings.BIRTHDAY_EXPRESSION, new SimpleStringProperty("")).getValue());
        });
        configFilePath.addListener((obs, oldVal, newVal) -> configFile.setValue(new File(newVal)));
        originatorInfoPath.addListener((obs, oldVal, newVal) -> originatorInfoFile.setValue(new File(newVal)));

        this.profileName.setValue(profileName);
        this.newProfile = newProfile;

        if (newProfile) {
            try {
                if (configFile.getValue().exists()) {
                    throw new ProfileRenamingException("Profile " + profileName + " already exists");
                } else if (!configFile.getValue().createNewFile()) {
                    throw new ProfileRenamingException("Profile " + profileName + " could not be created.");
                }
            } catch (IOException ex) {
                throw new ProfileRenamingException("New profile could not be created.", ex);
            }
        } else {
            if (!configFile.getValue().exists()) {
                throw new ProfileRenamingException("Profile " + profileName + " does not exist");
            }
        }
    }

    private static ObservableMap<ProfileSettings<?>, Property<?>> readConfigs(File configFile) {
        Map<ProfileSettings<?>, Property<?>> configurations = new HashMap<>();

        String[] parts;
        try (Scanner sc = new Scanner(configFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.contains(VALUE_SEPARATOR)) {
                    parts = line.split(VALUE_SEPARATOR, 2);
                    //NOTE At this point using raw type is necessary
                    ProfileSettings key = ProfileSettings.valueOf(parts[0].toUpperCase());
                    Object value = key.parse(parts.length < 2 ? "" : parts[1]);
                    if (key.isValid(value)) {
                        configurations.put(key, new SimpleObjectProperty<>(value));
                    } else {
                        Logger.getLogger(Profile.class.getName())
                                .log(Level.WARNING, "\"{0}\" has an invalid value. It is skipped.", key);
                    }
                } else {
                    Logger.getLogger(Profile.class.getName())
                            .log(Level.SEVERE, "Line \"{0}\" contains no \"" + VALUE_SEPARATOR + "\"", line);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EnvironmentHandler.class.getName()).log(Level.SEVERE, null, ex);
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
                                Logger.getLogger(EnvironmentHandler.class.getName())
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

    private void checkDeleted() {
        if (deleted) {
            throw new IllegalStateException("Profile was deleted.");
        }
    }

    private <T> String generateLine(ProfileSettings<T> key) {
        return ProfileSettings.name(key) + VALUE_SEPARATOR + key.toString((T) configurations.get(key).getValue());
    }

    /**
     * Writes the current settings into configuration file.
     */
    public void saveSettings() {
        if (!isAllConfigurationsSet()) {
            throw new IllegalStateException("You have to set all configurations first");
        }
        newProfile = false;

        String out = Arrays.stream(ProfileSettings.values())
                .map(this::generateLine)
                .collect(Collectors.joining("\n"));
        IOStreamUtility.printContent(out, configFile.getValue(), false);
    }

    /**
     * Renames the profile if no other profile with name {@code newName} exists. If this profile already is named
     * {@code newName} nothing happens. This method will affect the profile immediately. NOTE: When calling this method
     * the renamed profile is read in again, so unsaved changes to this profile are lost.
     *
     * @param newName The new profile name.
     */
    public synchronized void renameProfile(String newName) {
        checkDeleted();
        if (!newName.equals(profileName.get())) {
            File oldConfigFile = configFile.getValue();
            String newConfigPath = EnvironmentHandler.APP_DATA_PATH + "/" + newName + CONFIGFILE_FORMAT;
            File newConfigFile = new File(newConfigPath);

            String newOriginatorPath = EnvironmentHandler.APP_DATA_PATH + "/" + newName + ORIGINATORFILE_FORMAT;
            File newOriginatorFile = new File(newOriginatorPath);

            if (newConfigFile.exists() || newOriginatorFile.exists()) {
                throw new ProfileRenamingException("Can't rename profile. Profile \"" + newName + "\" already exists.");
            }
            if (configFile.getValue().renameTo(newConfigFile)) {
                File originatorInfoFileValue = originatorInfoFile.getValue();
                if (originatorInfoFileValue.exists() && !originatorInfoFileValue.renameTo(newOriginatorFile)) {
                    configFile.getValue().renameTo(oldConfigFile);
                    throw new ProfileRenamingException(
                            "Renaming the profile was undone. Originator settings couldn't be renamed.");
                }
            } else {
                throw new ProfileRenamingException("Profile couldn't be renamed.");
            }

            profileName.set(newName);
        }
    }

    public synchronized void deleteProfile() {
        checkDeleted();
        configFile.getValue().delete();
        originatorInfoFile.getValue().delete();
        deleted = true;
    }

    public static List<String> getAvailableProfiles() {
        String[] configFiles = new File(EnvironmentHandler.APP_DATA_PATH)
                .list((dir, name) -> name.endsWith(CONFIGFILE_FORMAT));
        if (configFiles == null) {
            return new ArrayList<>();
        } else {
            return Arrays.stream(configFiles)
                    .map(s -> s.substring(0, s.length() - CONFIGFILE_FORMAT.length()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Returns the value belonging to the key {@code key} or throws a {@link NoSuchElementException} if {@code key}
     * could not be found.
     *
     * @param <T> The type of the value hold by the given setting.
     * @param key The key to search for.
     * @return The value belonging to the key {@code key} or throws a {@link NoSuchElementException} if {@code key}
     * could not be found.
     */
    public <T> T get(ProfileSettings<T> key) {
        if (configurations.containsKey(key)) {
            return (T) configurations.get(key).getValue();
        } else {
            throw new NoSuchElementException("The currently loaded profile does not specify a value for " + key);
        }
    }

    /**
     * Returns the value belonging to key {@code key} or {@code defaultValue} if {@code key} could not be found or is
     * not specified.
     *
     * @param <T> The type of the value {@code key} holds.
     * @param key The key to search for.
     * @param defaultValue The value to return when {@code key} was not found.
     * @return The value belonging to key {@code key} or {@code defaultValue} if {@code key} could not be found or is
     * not specified.
     */
    public <T> T getOrDefault(ProfileSettings<T> key, T defaultValue) {
        if (configurations.containsKey(key)) {
            return (T) configurations.get(key).getValue();
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
    public ReadOnlyProperty<?> getProperty(ProfileSettings<?> key) {
        return (Property<?>) configurations.get(key);
    }

    /**
     * Sets a configuration.
     *
     * @param key The key of the configuration to set.
     * @param value The value to set for {@code key}.
     * @param <T> The type of the value.
     */
    public <T> void set(ProfileSettings<T> key, T value) {
        if (!key.isValid(value)) {
            throw new IllegalArgumentException("The given value is not valid for the given key");
        }
        configurations.putIfAbsent(key, new SimpleObjectProperty<>());
        //NOTE Using raw type is necessary
        Property valueProperty = configurations.get(key);
        valueProperty.setValue(value);
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
     * Returns the function for calculating whether a person of a certain age has to be notified.
     *
     * @return The function for calculating whether a person of a certain age has to be notified.
     */
    public IntFunction<Boolean> getAgeFunction() {
        checkDeleted();
        return ageFunction;
    }

    /**
     * Returns the property holding the file containing the originator infos.
     *
     * @return The property holding the file containing the originator infos.
     */
    public ReadOnlyProperty<File> originatorInfoFile() {
        return originatorInfoFile;
    }

    /**
     * Returns the file containing originator infos for SEPA Direct Debits. NOTE: It is not guaranteed that this file
     * exists.
     *
     * @return The file containing originator infos for SEPA Direct Debits. NOTE: It is not guaranteed that this file
     * exists.
     */
    public File getOriginatorInfoFile() {
        checkDeleted();
        return originatorInfoFile.getValue();
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
