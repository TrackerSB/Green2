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

package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.DataProvider;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Provides functions for creating different dialogs. NOTE: There are some functions like initOwner(...) which may
 * override the stylesheet which is set in these methods. So you should use the {@code owner} parameter if needed.
 *
 * @author Stefan Huber
 */
public final class DialogUtility {

    private DialogUtility() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
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

    private static Alert createAlert(Alert.AlertType type, Window owner) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        return addStyleAndIcon(alert);
    }

    private static Alert createAlert(Alert.AlertType type, String message) {
        Alert alert = createAlert(type);
        alert.setContentText(message);

        return alert;
    }

    private static Alert createAlert(Alert.AlertType type, String message, Window owner) {
        Alert alert = createAlert(type, owner);
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
     * @param owner   The owner of this alert.
     * @return The created alert.
     */
    public static Alert createErrorAlert(String message, Window owner) {
        return createAlert(Alert.AlertType.ERROR, message, owner);
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

    private static Alert addStacktrace(Alert alert, Exception ex) {
        Label stacktraceLabel = new Label(DataProvider.getResourceValue("stacktraceLabel"));

        StringWriter stacktrace = new StringWriter();
        PrintWriter stacktracePw = new PrintWriter(stacktrace);
        ex.printStackTrace(stacktracePw);
        TextArea stacktraceArea = new TextArea(stacktrace.toString());
        stacktraceArea.setEditable(false);

        GridPane grid = new GridPane();
        grid.addColumn(0, stacktraceLabel, stacktraceArea);
        GridPane.setHgrow(stacktraceArea, Priority.ALWAYS);
        GridPane.setVgrow(stacktraceArea, Priority.ALWAYS);

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
        return addStacktrace(createAlert(Alert.AlertType.ERROR), ex);
    }

    /**
     * Crates an error alert showing a stacktrace of an exception.
     *
     * @param ex    The exception to show.
     * @param header The title of the window.
     * @return The created alert.
     */
    public static Alert createStacktraceAlert(Exception ex, String header) {
        Alert alert = createStacktraceAlert(ex);
        alert.setHeaderText(header);
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
     * @param owner   The owner of the alert.
     * @return The created alert.
     */
    public static Alert createInfoAlert(String message, Window owner) {
        return createAlert(Alert.AlertType.INFORMATION, message, owner);
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
        TextArea messageArea = new TextArea(message);
        messageArea.setEditable(false);

        GridPane grid = new GridPane();
        grid.addColumn(0, messageArea);
        GridPane.setHgrow(messageArea, Priority.ALWAYS);
        GridPane.setVgrow(messageArea, Priority.ALWAYS);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setExpandableContent(grid);
        dialogPane.setExpanded(true);

        return alert;
    }

    /**
     * Crates a message alert. This alert shows the message in a non-editable {@link TextArea}.
     *
     * @param message The message to display.
     * @return The created alert.
     */
    public static Alert createMessageAlert(String message) {
        return addMessageTextArea(createAlert(Alert.AlertType.INFORMATION), message);
    }

    /**
     * Crates a message alert. This alert shows the message in a non-editable {@link TextArea}.
     *
     * @param message The message to display.
     * @param owner   The owner of the alert.
     * @return The created alert.
     */
    public static Alert createMessageAlert(String message, Window owner) {
        return addMessageTextArea(createAlert(Alert.AlertType.INFORMATION, owner), message);
    }

    /**
     * Crates a message alert. This alert shows the message in a non-editable {@link TextArea}.
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
     * Crates a message alert. This alert shows the message in a non-editable {@link TextArea}.
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
