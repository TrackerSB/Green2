package bayern.steinbrecher.gruen2.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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
    public static final String VALUE_SEPARATOR = "=";
    /**
     * The configurations found in gruen2.conf.
     */
    private static Map<ConfigKey, String> configs = null;

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
                    + "/AppData/Roaming/Gruen2_Mitgliederverwaltung";
        } else {
            return System.getProperty("user.home")
                    + "/.Gruen2_Mitgliederverwaltung";
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
            configs = new HashMap<>();
            try (Scanner sc = new Scanner(
                    new File(getAppDataPath() + "/gruen2.conf"))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    String[] parts = line.split(VALUE_SEPARATOR);
                    if (parts.length != 2) {
                        System.err.println("\"" + line + "\" has not exactly "
                                + "two elements. It remains ignored.");
                    } else {
                        configs.put(ConfigKey.valueOf(parts[0].toUpperCase()),
                                parts[1]);
                    }
                }
            } catch (FileNotFoundException ex) {
                System.err.println("Configfile \"gruen2.conf\" not found.");
            }
        }
        return configs.getOrDefault(key, defaultValue);
    }

    /**
     * Checks whether to use SSH or not.
     *
     * @return {@code true} only if the connections have to use SSH.
     */
    public static boolean useSsh() {
        return getOrDefault(ConfigKey.USE_SSH, "ja").equalsIgnoreCase("ja");
    }
}
