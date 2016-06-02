package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Represents a simple confirm dialog.
 *
 * @author Stefan Huber
 */
public class ConfirmDialog extends View {

    public static final String BAD_CONFIGS = "Es fehlen Konfigurations"
            + "einstellungen oder die Konfigurationsdatei konnte nicht "
            + "gefunden werden.\n"
            + "Frage bei stefan.huber.niedling@outlook.com nach.";
    public static final String CHECK_CONNECTION = "Prüfe, ob die Datenbank "
            + "erreichbar ist, und ob du Grün2 richtig konfiguriert hast.";
    public static final String CHECK_INPUT = "Prüfe deine Eingaben.";
    public static final String NO_SEPA_DEBIT = "Sepalastschrift konnte nicht "
            + "erstellt werden";
    public static final String CORRECT_IBANS = "Alle IBANs haben eine korrekte "
            + "Prüfsumme";
    public static final String UNEXPECTED_ABBORT = "Die Verbindung wurde "
            + "zurückgewiesen oder unterbrochen. Starten Sie das Programm neu.";
    private static final int MIN_WIDTH = 250;
    private static final int MIN_HEIGHT = 100;
    private String message;
    private Window owner;
    private boolean userConfirmed = false;

    /**
     * Creates a new dialog showing {@code message} and a single button for
     * confirmation.
     *
     * @param message The message to display.
     * @param owner The window making unaccessible until this dialog is closed.
     */
    public ConfirmDialog(String message, Window owner) {
        this.message = message;
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size:14px");

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setOnAction(aevt -> {
            userConfirmed = true;
            stage.close();
        });

        VBox dialogContent = new VBox();
        dialogContent.setStyle("-fx-margin: 10px");
        dialogContent.getChildren()
                .addAll(messageLabel, okButton);
        dialogContent.setStyle("-fx-spacing:10px");
        dialogContent.setAlignment(Pos.CENTER);

        Scene scene = new Scene(dialogContent);
        scene.getStylesheets().add(DataProvider.getStylesheetPath());
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(owner);
        stage.setTitle("Info");
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.getIcon());
    }

    private void initDialog() {
        Platform.runLater(() -> {
            try {
                start(new Stage());
            } catch (Exception ex) {
                Logger.getLogger(ConfirmDialog.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
    }

    /**
     * Shows this dialog only once and blocks until it is closed.
     */
    public void showOnceAndWait() {
        onlyShowOnce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userConfirmed() {
        return userConfirmed;
    }

    public static ConfirmDialog createBadConfigsDialog(Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(BAD_CONFIGS, owner);
        dialog.initDialog();
        return dialog;
    }

    public static ConfirmDialog createCheckInputDialog(Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(CHECK_INPUT, owner);
        dialog.initDialog();
        return dialog;
    }

    public static ConfirmDialog createCheckConnectionDialog(Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(CHECK_CONNECTION, owner);
        dialog.initDialog();
        return dialog;
    }

    public static ConfirmDialog createUnexpectedAbbortDialog(Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(UNEXPECTED_ABBORT, owner);
        dialog.initDialog();
        return dialog;
    }

    public static ConfirmDialog createNoSepaDebitDialog(Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(NO_SEPA_DEBIT, owner);
        dialog.initDialog();
        return dialog;
    }

    public static ConfirmDialog createCorrectIbansDialog(Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(CORRECT_IBANS, owner);
        dialog.initDialog();
        return dialog;
    }
}
