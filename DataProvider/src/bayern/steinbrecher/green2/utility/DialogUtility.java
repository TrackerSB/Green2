/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.DataProvider;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Provides functions for creating different dialogs.
 *
 * @author Stefan Huber
 */
public class DialogUtility {

    private DialogUtility() {
        throw new UnsupportedOperationException(
                "Construction of an object not allowed.");
    }

    private static Alert addStyleAndIcon(Alert alert) {
        Scene scene = alert.getDialogPane().getScene();
        scene.getStylesheets().add(DataProvider.STYLESHEET_PATH);
        Stage stage = (Stage) scene.getWindow();
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());

        return alert;
    }

    private static Alert createAlert(Alert.AlertType type) {
        return addStyleAndIcon(new Alert(type));
    }

    private static Alert createAlert(Alert.AlertType type, String message) {
        Alert alert = createAlert(type);
        alert.setContentText(message);

        return alert;
    }

    private static Alert createAlert(Alert.AlertType type, String message, String header) {
        Alert alert = createAlert(type, message);
        alert.setHeaderText(header);
        return alert;
    }

    private static Alert createAlert(Alert.AlertType type, String message, String header, String title) {
        Alert alert = createAlert(type, message, header);
        alert.setTitle(title);
        return alert;
    }

    /**
     * Crates an error alert.
     *
     * @param message The message to display.
     * @return The created alert.
     */
    public static Alert createErrorAlert(String message) {
        return createAlert(Alert.AlertType.ERROR, message);
    }

    /**
     * Crates an error alert.
     *
     * @param message The message to display.
     * @param header  The header to show.
     * @return The created alert.
     */
    public static Alert createErrorAlert(String message, String header) {
        return createAlert(Alert.AlertType.ERROR, message, header);
    }

    /**
     * Crates an error alert.
     *
     * @param message The message to display.
     * @param header  The header to show.
     * @param title   The title of the window.
     * @return The created alert.
     */
    public static Alert createErrorAlert(String message, String header, String title) {
        return createAlert(Alert.AlertType.ERROR, message, header, title);
    }

    private static Alert addStracktrace(Alert alert, Exception ex) {
        String stacktraceResourceBundle = DataProvider.getResourceValue("stacktraceLabel");
        alert.setHeaderText(stacktraceResourceBundle);
        Label stacktraceLabel = new Label(stacktraceResourceBundle);

        StringWriter stacktrace = new StringWriter();
        PrintWriter stacktracePw = new PrintWriter(stacktrace);
        ex.printStackTrace(stacktracePw);
        TextArea stacktraceArea = new TextArea(stacktrace.toString());
        stacktraceArea.setEditable(false);

        GridPane grid = new GridPane();
        grid.addColumn(0, stacktraceLabel, stacktraceArea);
        GridPane.setHgrow(stacktraceArea, Priority.ALWAYS);
        GridPane.setVgrow(stacktraceArea, Priority.ALWAYS);

        alert.setWidth(500);
        alert.setHeight(350);
        alert.getDialogPane().setExpandableContent(grid);

        return alert;
    }

    /**
     * Crates an error alert showing a stacktrace of an exception.
     *
     * @param ex The exception to show.
     * @return The created alert.
     */
    public static Alert createStacktraceAlert(Exception ex) {
        return addStracktrace(createAlert(Alert.AlertType.ERROR), ex);
    }

    /**
     * Crates an error alert showing a stacktrace of an exception.
     *
     * @param ex    The exception to show.
     * @param title The title of the window.
     * @return The created alert.
     */
    public static Alert createStacktraceAlert(Exception ex, String title) {
        Alert alert = createStacktraceAlert(ex);
        alert.setTitle(title);
        return alert;
    }

    /**
     * Crates a warning alert.
     *
     * @param message The message to display.
     * @return The created alert.
     */
    public static Alert createWarningAlert(String message) {
        return createAlert(Alert.AlertType.WARNING, message);
    }

    /**
     * Crates a warning alert.
     *
     * @param message The message to display.
     * @param header  The header to show.
     * @return The created alert.
     */
    public static Alert createWarningAlert(String message, String header) {
        return createAlert(Alert.AlertType.WARNING, message, header);
    }

    /**
     * Crates a warning alert.
     *
     * @param message The message to display.
     * @param header  The header to show.
     * @param title   The title of the window.
     * @return The created alert.
     */
    public static Alert createWarningAlert(String message, String header, String title) {
        return createAlert(Alert.AlertType.WARNING, message, header, title);
    }

    /**
     * Crates an information alert.
     *
     * @param message The message to display.
     * @return The created alert.
     */
    public static Alert createInfoAlert(String message) {
        return createAlert(Alert.AlertType.INFORMATION, message);
    }

    /**
     * Crates an information alert.
     *
     * @param message The message to display.
     * @param header  The header to show.
     * @return The created alert.
     */
    public static Alert createInfoAlert(String message, String header) {
        return createAlert(Alert.AlertType.INFORMATION, message, header);
    }

    /**
     * Crates an information alert.
     *
     * @param message The message to display.
     * @param header  The header to show.
     * @param title   The title of the window.
     * @return The created alert.
     */
    public static Alert createInfoAlert(String message, String header, String title) {
        return createAlert(Alert.AlertType.INFORMATION, message, header, title);
    }

    private static Alert addMessageTextArea(Alert alert, String message) {
        TextArea stacktraceArea = new TextArea(message);
        stacktraceArea.setEditable(false);

        GridPane grid = new GridPane(); //TODO GridPane really needed?
        grid.addColumn(0, stacktraceArea);
        GridPane.setHgrow(stacktraceArea, Priority.ALWAYS);
        GridPane.setVgrow(stacktraceArea, Priority.ALWAYS);

        alert.setWidth(500); //FIXME Resize correctly
        alert.setHeight(350);
        alert.getDialogPane().setExpandableContent(grid);

        return alert;
    }

    /**
     * Crates a message alert. This alert shows the message in a non-editable {@code TextArea}.
     *
     * @param message The message to display.
     * @return The created alert.
     */
    public static Alert createMessageAlert(String message) {
        return addMessageTextArea(createAlert(Alert.AlertType.INFORMATION), message);
    }

    /**
     * Crates a message alert. This alert shows the message in a non-editable {@code TextArea}.
     *
     * @param message The message to display.
     * @param header  The header to show.
     * @return The created alert.
     */
    public static Alert createMessageAlert(String message, String header) {
        Alert messageAlert = createMessageAlert(message);
        messageAlert.setHeaderText(header);
        return messageAlert;
    }

    /**
     * Crates a message alert. This alert shows the message in a non-editable {@code TextArea}.
     *
     * @param message The message to display.
     * @param header  The header to show.
     * @param title   The title of the window.
     * @return The created alert.
     */
    public static Alert createMessageAlert(String message, String header, String title) {
        Alert messageAlert = createMessageAlert(message, header);
        messageAlert.setTitle(title);
        return messageAlert;
    }

    /**
     * Creates an alert with given type, given message, given buttons and with default stylesheet and icon.
     *
     * @param type    The type of the alert.
     * @param message The message to display.
     * @param buttons The buttons to show.
     * @return The created alert.
     */
    public static Alert createAlert(Alert.AlertType type, String message, ButtonType... buttons) {
        return addStyleAndIcon(new Alert(type, message, buttons));
    }
}