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
package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Provides functions for creating different dialogs. When using this class all alerts should be created on the FX
 * application thread otherwise the creation may fail. NOTE: There are some functions like initOwner(...) which may
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

    private static void addDefaultStyle(Scene scene) {
        scene.getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
    }

    private static void addDefaultIcon(Stage stage) {
        stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
    }

    private static Alert addStyleAndIcon(Alert alert) {
        Scene scene = alert.getDialogPane().getScene();
        addDefaultStyle(scene);
        Stage stage = (Stage) scene.getWindow();
        addDefaultIcon(stage);

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
     * Returns a new {@link Alert}. This method may be called on any {@link Thread}. If it is not called on the FX
     * application thread it passes the creation to the FX application thread and waits for it.
     *
     * @return The newly created {@link Alert}.
     */
    private static Alert getAlert(Callable<Alert> alertCreation) {
        Alert alert = null;
        if (Platform.isFxApplicationThread()) {
            try {
                alert = alertCreation.call();
            } catch (Exception ex) {
                Logger.getLogger(DialogUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            FutureTask<Alert> alertCreationTask = new FutureTask<>(alertCreation);
            Platform.runLater(alertCreationTask);
            try {
                alert = alertCreationTask.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(DialogUtility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    public static Alert createAlert(Alert.AlertType alertType, Window owner, String... args) {
        Alert alert = addStyleAndIcon(initOwner(getAlert(() -> new Alert(alertType)), owner));
        int parameterCount = args.length > NUMBER_USED_PARAMETERS ? NUMBER_USED_PARAMETERS : args.length;
        if (parameterCount > NUMBER_USED_PARAMETERS) {
            Logger.getLogger(DialogUtility.class.getName())
                    .log(Level.WARNING, "You passed more than {0} parameters. Only the first {0} will be used.",
                            NUMBER_USED_PARAMETERS);
        }
        //CHECKSTYLE.OFF: MagicNumber - The JavaDoc explicitely describes these three possible parameters
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
                break;
            case 0:
                //No op
                break;
            default:
                throw new IllegalArgumentException(
                        "This number of parameters can not be handled. How could that happen? Scary!");
        }
        //CHECKSTYLE.ON: MagicNumber
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
        return addStyleAndIcon(getAlert(() -> new Alert(type, message, buttons)));
    }

    /**
     * Calls {@link Alert#showAndWait()} making sure it is called on the FX application thread.
     *
     * @param alert The alert to call {@link Alert#showAndWait()} on.
     * @return The result of {@link Alert#showAndWait()}.
     */
    public static Optional<ButtonType> showAndWait(Alert alert) {
        Optional<ButtonType> result;
        if (Platform.isFxApplicationThread()) {
            result = alert.showAndWait();
        } else {
            FutureTask<Optional<ButtonType>> showAndWaitTask = new FutureTask<>(() -> alert.showAndWait());
            Platform.runLater(showAndWaitTask);
            try {
                result = showAndWaitTask.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(DialogUtility.class.getName()).log(Level.SEVERE, null, ex);
                result = Optional.empty();
            }
        }
        return result;
    }

    /**
     * Creates a dialog showing a list of {@link TitledPane}s containing messages.
     *
     * @param owner The owner of this dialog.
     * @param stage The stage to create the dialog for.
     * @param reports The {@link Map} associating message headlines with a list of messages.
     */
    public static void createCheckReportDialog(Window owner, Stage stage, Map<String, List<String>> reports) {
        VBox reportsBox = new VBox();
        reports.entrySet().stream()
                .map(entry -> {
                    VBox messagesBox = new VBox();
                    List<String> messages = entry.getValue();
                    messages.stream()
                            .map(message -> new Label(message))
                            .forEach(messagesBox.getChildren()::add);
                    TitledPane reportPane = new TitledPane(entry.getKey() + " (" + messages.size() + ")", messagesBox);
                    reportPane.setCollapsible(!messages.isEmpty());
                    reportPane.setExpanded(false);
                    return reportPane;
                })
                .forEach(reportsBox.getChildren()::add);

        stage.initOwner(owner);
        stage.initStyle(StageStyle.UTILITY);
        addDefaultIcon(stage);
        ScrollPane scrollableReportsBox = new ScrollPane(reportsBox);
        HBox.setHgrow(scrollableReportsBox, Priority.ALWAYS);
        Scene scene = new Scene(scrollableReportsBox);
        addDefaultStyle(scene);
        stage.setScene(scene);
    }
}
