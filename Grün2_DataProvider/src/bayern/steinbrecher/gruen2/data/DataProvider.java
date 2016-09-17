package bayern.steinbrecher.gruen2.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
import java.util.function.IntFunction;
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
     * Representing a function for calculating whether a person with a specific
     * age gets notified.
     */
    public static final IntFunction<Boolean> AGE_FUNCTION;
    /**
     * The path of the folder where to put user specific data of the
     * application.
     */
    public static final String APP_DATA_PATH
            = HOME_DIR + (CURRENT_OS == OS.WINDOWS
                    ? "/AppData/Roaming/Grün2_Mitgliederverwaltung"
                    : "/.Grün2_Mitgliederverwaltung");
    /**
     * The path where the file containing information about the last valid
     * inserted data about an originator of a SEPA direct debit. (There does
     * need to be a file yet)
     */
    public static final String ORIGINATOR_INFO_PATH
            = APP_DATA_PATH + "/originator.properties";
    private static final String CONFIGFILE_NAME = "Grün2.conf";
    /**
     * The path to the configfile. (May not exist, yet)
     */
    public static final String CONFIGFILE_PATH
            = APP_DATA_PATH + "/" + CONFIGFILE_NAME;
    /**
     * The file to the configurations for Grün2.
     */
    private static final File CONFIGFILE = new File(CONFIGFILE_PATH);
    /**
     * The configurations found in gruen2.conf.
     */
    private static final Map<ConfigKey, String> CONFIGURATIONS
            = new HashMap<>();
    /**
     * {@code true} only if all allowed configurations are specified.
     */
    public static final boolean ALL_CONFIGURATIONS_SET;

    static {
        //Create configDir if not existing
        new File(DataProvider.APP_DATA_PATH).mkdir();

        String[] parts = null;
        try (Scanner sc = new Scanner(CONFIGFILE)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                parts = line.split(VALUE_SEPARATOR);
                if (parts.length != 2) {
                    System.err.println("\"" + line + "\" has not exactly "
                            + "two elements. It remains ignored.");
                } else {
                    CONFIGURATIONS.put(ConfigKey.valueOf(
                            parts[0].toUpperCase()), parts[1]);
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println(
                    "Configfile \"" + CONFIGFILE_NAME + "\" not found.");
        } catch (IllegalArgumentException ex) {
            System.err.println(parts[0] + " is no valid config attribute.");
        }

        ALL_CONFIGURATIONS_SET
                = CONFIGURATIONS.size() == ConfigKey.values().length;
    }

    static {
        Set<IntFunction<Boolean>> ageFunctionParts = new HashSet<>();
        String birthdayExpression
                = getOrDefault(ConfigKey.BIRTHDAY_EXPRESSION, "");
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
                            return age >= new Integer(part.substring(2));
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
                            return age <= new Integer(part.substring(2));
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
                    System.err.println(part + " gets skipped");
                }
            } catch (NumberFormatException ex) {
                System.err.println(part + " gets skipped");
            }
        }

        if (ageFunctionParts.isEmpty()) {
            AGE_FUNCTION = age -> false;
        } else {
            AGE_FUNCTION = age -> ageFunctionParts.parallelStream()
                    .anyMatch(intFunc -> intFunc.apply(age));
        }
    }

    /**
     * Prohibit construction of a new object.
     */
    private DataProvider() {
        throw new UnsupportedOperationException("Constructing an "
                + DataProvider.class.getSimpleName() + "is not allowed");
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
    public static String getOrDefault(ConfigKey key, String defaultValue) {
        return CONFIGURATIONS.getOrDefault(key, defaultValue);
    }

    /**
     * Checks whether to use SSH or not. (Default is yes)
     *
     * @return {@code true} only if the connections have to use SSH.
     */
    public static boolean useSsh() {
        return getOrDefault(ConfigKey.USE_SSH, "ja").equalsIgnoreCase("ja");
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
            System.err.println("No resource for \"" + key + "\" found.");
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
     * Contains enums representing supported operation systems.
     */
    public enum OS {
        WINDOWS, LINUX;
    }
}
