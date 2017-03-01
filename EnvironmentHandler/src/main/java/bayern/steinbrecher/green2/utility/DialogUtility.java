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

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
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

/**
 * Provides functions for creating different dialogs. NOTE: There are some functions like initOwner(...) which may
 * override the stylesheet which is set in these methods. So you should use the {@code owner} parameter if needed.
 *
 * @author Stefan Huber
 */
public final class DialogUtility {

    private static final int NUMBER_USED_PARAMETERS = 3;

    private DialogUtility() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    private static Alert initOwner(Alert alert, Window owner) {
        alert.initOwner(owner);
        return alert;
    }

    private static Alert addStyleAndIcon(Alert alert) {
        Scene scene = alert.getDialogPane().getScene();
        scene.getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
        Stage stage = (Stage) scene.getWindow();
        stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());

        Node graphic;
        switch (alert.getAlertType()) {
            case CONFIRMATION:
                graphic = EnvironmentHandler.ImageSet.CHECKED.getAsImageView();
                break;
            case ERROR:
                graphic = EnvironmentHandler.ImageSet.ERROR.getAsImageView();
                break;
            case INFORMATION:
                graphic = EnvironmentHandler.ImageSet.INFO.getAsImageView();
                break;
            case WARNING:
                graphic = EnvironmentHandler.ImageSet.WARNING.getAsImageView();
                break;
            default:
                graphic = null;
        }
        alert.setGraphic(graphic);

        return alert;
    }

    /**
     * Creates an {@link Alert} with given settings.
     *
     * @param alertType The type of the alert.
     * @param owner The owner of the alert or {@code null} if no owner has to be set.
     * @param args The arguments containing the content, title and the header. NOTE: The order is important. If you
     * specify less elements or an element is {@code null} these elements will have the default value according to
     * {@link Alert}. If you specify more elements they will be ignored.
     * @return The created {@link Alert}.
     */
    public static Alert createAlert(Alert.AlertType alertType, Window owner, String... args) {
        Alert alert = addStyleAndIcon(initOwner(new Alert(alertType), owner));
        int parameterCount = args.length > NUMBER_USED_PARAMETERS ? NUMBER_USED_PARAMETERS : args.length;
        if (parameterCount > NUMBER_USED_PARAMETERS) {
            Logger.getLogger(DialogUtility.class.getName())
                    .log(Level.WARNING, "You passed more than {0} parameters. Only the first {0} will be used.",
                            NUMBER_USED_PARAMETERS);
        }
        switch (parameterCount) {
            case 3:
                if (args[2] != null) {
                    alert.setHeaderText(args[2]);
                }
            case 2:
                if (args[1] != null) {
                    alert.setTitle(args[1]);
                }
            case 1:
                if (args[0] != null) {
                    alert.setContentText(args[0]);
                }
        }
        return alert;
    }

    /**
     * Creates an {@link Alert} which shows a stacktrace with given settings.
     *
     * @param owner The owner of the alert or {@code null} if no owner has to be set.
     * @param ex The exception to show.
     * @param args For details see {@link #createAlert(Alert.AlertType, Window, String...)}.
     * @return The created {@link Alert}.
     * @see #createAlert(Alert.AlertType, Window, String...)
     */
    public static Alert createStacktraceAlert(Window owner, Exception ex, String... args) {
        Alert alert = createAlert(Alert.AlertType.ERROR, owner, args);

        Label stacktraceLabel = new Label(EnvironmentHandler.getResourceValue("stacktraceLabel"));

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
     * Creates an {@link Alert} showing a warning with given settings.
     *
     * @param owner The owner of the alert or {@code null} if no owner has to be set.
     * @param args For details see {@link #createAlert(Alert.AlertType, Window, String...)}.
     * @return The created {@link Alert}.
     * @see #createAlert(Alert.AlertType, Window, String...)
     */
    public static Alert createWarningAlert(Window owner, String... args) {
        return createAlert(Alert.AlertType.WARNING, owner, args);
    }

    /**
     * Creates an {@link Alert} showing an error with given settings.
     *
     * @param owner The owner of the alert or {@code null} if no owner has to be set.
     * @param args For details see {@link #createAlert(Alert.AlertType, Window, String...)}.
     * @return The created {@link Alert}.
     * @see #createAlert(Alert.AlertType, Window, String...)
     */
    public static Alert createErrorAlert(Window owner, String... args) {
        return createAlert(Alert.AlertType.ERROR, owner, args);
    }

    /**
     * Creates an {@link Alert} showing information with given settings.
     *
     * @param owner The owner of the alert or {@code null} if no owner has to be set.
     * @param args For details see {@link #createAlert(Alert.AlertType, Window, String...)}.
     * @return The created {@link Alert}.
     * @see #createAlert(Alert.AlertType, Window, String...)
     */
    public static Alert createInfoAlert(Window owner, String... args) {
        return createAlert(Alert.AlertType.INFORMATION, owner, args);
    }

    /**
     * Creates an {@link Alert} showing a message with given settings.
     *
     * @param owner The owner of the alert or {@code null} if no owner has to be set.
     * @param message The message (multi line allowed) to show.
     * @param args For details see {@link #createAlert(Alert.AlertType, Window, String...)}.
     * @return The created {@link Alert}.
     * @see #createAlert(Alert.AlertType, Window, String...)
     */
    public static Alert createMessageAlert(Window owner, String message, String... args) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION, owner, args);

        TextArea messageArea = new TextArea(message);
        messageArea.setEditable(false);
        messageArea.setWrapText(true);

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
     * Creates an alert with custom buttons.
     *
     * @param type The type of the alert.
     * @param message The message to show.
     * @param buttons The buttons to show.
     * @return The created {@link Alert}.
     */
    public static Alert createAlert(Alert.AlertType type, String message, ButtonType... buttons) {
        return addStyleAndIcon(new Alert(type, message, buttons));
    }
}
