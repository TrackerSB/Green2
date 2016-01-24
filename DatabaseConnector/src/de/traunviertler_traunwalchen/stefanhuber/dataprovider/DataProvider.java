package de.traunviertler_traunwalchen.stefanhuber.dataprovider;

import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.image.Image;

/**
 * Diese Klasse erm&ouml;glicht das Abfragen von Daten/Informationen, die alle
 * Applikationen betreffen, die den Login des DatabaseConnector-Projekts
 * benötigen.
 *
 * @author Stefan Huber
 */
public class DataProvider {

    public static final String MISSING_INPUT_STYLECLASS_NAME = "missingInput",
            BAD_INPUTLENGTH_STYLECLASS_NAME = "badInputLength",
            BAD_INPUT_STYLECLASS_NAME = "badInput";

    /**
     * Um zu verhindern, dass Objekte dieser Klasse erstellt werden.
     */
    private DataProvider() {
    }

    /**
     * Liefert das Standard-Icon.
     *
     * @return Das Standard-Icon
     */
    public static Image getIcon() {
        return new Image("de/traunviertler_traunwalchen/stefanhuber/data"
                + "/iconAllg.png");
    }

    /**
     * Liefert die Standard-CSS-Datei.
     *
     * @return Die Standard-CSS-Datei
     */
    public static String getStylesheetPath() {
        return "de/traunviertler_traunwalchen/stefanhuber/data/gtevStyle.css";
    }

    /**
     * Liefert den Standard-Speicherort.
     *
     * @return Bei Windows den Pfad zum Desktop, sonst das home-Verzeichnis
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
     * Liefert den Speicherort der Daten für das Programm.
     *
     * @return Unter Windows: %AppData%/Mitgliederverwaltung Sonst:
     * "home-directory"/Mitgliederverwaltung
     */
    public static String getAppDataPath() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getProperty("user.home").replaceAll("\\\\", "/")
                    + "/AppData/Roaming/Mitgliederverwaltung";
        } else {
            return System.getProperty("user.home") + "/Mitgliederverwaltung";
        }
    }

    /**
     * Setzt oder entfernt die Style-Class
     * <code>MISSING_INPUT_STYLECLASS_NAME</code>
     *
     * @param control Das Control-Element, dessen Styleklassen verändert werden
     * sollen.
     * @param set true, wenn die Styleklasse gesetzt werden soll; false, wenn
     * diese entfernt werden soll.
     */
    public static void changeMissingInputStyleClass(Control control,
            boolean set) {
        changeStyleClass(control, set, MISSING_INPUT_STYLECLASS_NAME);
    }

    /**
     * Setzt oder entfernt die Style-Class
     * <code>BAD_INPUTLENGTH_STYLECLASS_NAME</code>
     *
     * @param control Das Control-Element, dessen Styleklassen verändert werden
     * sollen.
     * @param set true, wenn die Styleklasse gesetzt werden soll; false, wenn
     * diese entfernt werden soll.
     */
    public static void changeBadInputLengthStyleClass(Control control,
            boolean set) {
        changeStyleClass(control, set, BAD_INPUTLENGTH_STYLECLASS_NAME);
    }

    /**
     * Setzt oder entfernt die Style-Class
     * <code>BAD_INPUT_STYLECLASS_NAME</code>
     *
     * @param control Das Control-Element, dessen Styleklassen verändert werden
     * sollen.
     * @param set true, wenn die Styleklasse gesetzt werden soll; false, wenn
     * diese entfernt werden soll.
     */
    public static void changeBadInputStyleClass(Control control, boolean set) {
        changeStyleClass(control, set, BAD_INPUT_STYLECLASS_NAME);
    }

    private static void changeStyleClass(Control control, boolean set,
            String styleClass) {
        ObservableList<String> styleClasses = control.getStyleClass();
        if (set) {
            if (!styleClasses.contains(styleClass)) {
                styleClasses.add(styleClass);
            }
        } else {
            styleClasses.remove(styleClass);
        }
    }

}
