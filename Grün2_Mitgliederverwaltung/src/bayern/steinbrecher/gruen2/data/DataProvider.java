package bayern.steinbrecher.gruen2.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.IntFunction;
import javafx.scene.image.Image;

/**
 * Delivers access to diffrent application wide usefull paths, icons, etc.
 *
 * @author Stefan Huber
 */
public class DataProvider {

    /**
     * The symbol used to separate the config key on the left side and the value
     * on the right side in gruen2.conf.
     */
    public static final String VALUE_SEPARATOR = ":";
    /**
     * The file to the configurations for Grün2.
     */
    private static File configFile = new File(getAppDataPath() + "/Grün2.conf");
    /**
     * The configurations found in gruen2.conf.
     */
    private static Map<ConfigKey, String> configs = null;
    /**
     * Representing a function for calculating whether a person with a specific
     * age gets notified.
     */
    private static IntFunction<Boolean> ageFunction = null;
    /**
     * Containing all parts of the configfile which are relevant to calculate
     * the ageFunction.
     */
    private static Set<IntFunction<Boolean>> ageFunctionParts = new HashSet<>();

    /**
     * Prohibit construction of a new object.
     */
    private DataProvider() {
        throw new UnsupportedOperationException("Constructing an "
                + DataProvider.class.getSimpleName() + "is not allowed");
    }

    /**
     * Returns the default icon.
     *
     * @return The default icon.
     */
    public static Image getIcon() {
        return new Image("bayern/steinbrecher/gruen2/data/icon.png");
    }

    /**
     * Returns the css file containing application wide styles.
     *
     * @return The css file containing application wide styles.
     */
    public static String getStylesheetPath() {
        return "bayern/steinbrecher/gruen2/data/styles.css";
    }

    /**
     * Returns the path of the directory to save results.
     *
     * @return The path of the directory to save results.
     */
    public static String getSavepath() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getProperty("user.home").replaceAll("\\\\", "/")
                    + "/Desktop";
        } else {
            return System.getProperty("user.home");
        }
    }

    /**
     * Returns the path of the directory for saving user data.
     *
     * @return The path of the directory for saving user data.
     */
    public static String getAppDataPath() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getProperty("user.home").replaceAll("\\\\", "/")
                    + "/AppData/Roaming/Grün2_Mitgliederverwaltung";
        } else {
            return System.getProperty("user.home")
                    + "/.Grün2_Mitgliederverwaltung";
        }
    }

    private static void readConfigs() {
        String[] parts = null;
        configs = new HashMap<>();
        try (Scanner sc = new Scanner(configFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                parts = line.split(VALUE_SEPARATOR);
                if (parts.length != 2) {
                    System.err.println("\"" + line + "\" has not exactly "
                            + "two elements. It remains ignored.");
                } else {
                    configs.put(ConfigKey.valueOf(parts[0].toUpperCase()),
                            parts[1]);
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Configfile \"Grün2.conf\" not found.");
        } catch (IllegalArgumentException ex) {
            System.err.println(parts[0] + " is no valid configattribute.");
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
    public static String getOrDefault(ConfigKey key, String defaultValue) {
        if (configs == null) {
            readConfigs();
        }
        return configs.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the function that checks whether a person with a certain age has
     * to be notified on birthday according to the configured criteria.
     *
     * @return The function for calculating whether a person has to be notified
     * on birthday.
     */
    public static synchronized IntFunction<Boolean> getAgeFunction() {
        if (ageFunction == null) {
            String birthdayExpression
                    = getOrDefault(ConfigKey.BIRTHDAY_EXPRESSION, "");
            ageFunction = age -> false;
            for (String part : Arrays.asList(birthdayExpression.split(","))) {
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
            ageFunction = age -> ageFunctionParts.parallelStream()
                    .anyMatch(intFunc -> intFunc.apply(age));
        }
        return ageFunction;
    }

    /**
     * Checks whether to use SSH or not.
     *
     * @return {@code true} only if the connections have to use SSH.
     */
    public static boolean useSsh() {
        return getOrDefault(ConfigKey.USE_SSH, "ja").equalsIgnoreCase("ja");
    }

    /**
     * Checks whether all allowed configurations are specified.
     *
     * @return {@code true} only if all allowed configurations are specified.
     * @see ConfigKey
     */
    public static boolean hasAllConfigs() {
        if (configs == null) {
            readConfigs();
        }
        return configs.size() == ConfigKey.values().length;
    }
}
