package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    /**
     * Message indicating that the configurations of the user are invalid.
     */
    public static final String BAD_CONFIGS = "Es fehlen Konfigurations"
            + "einstellungen oder die Konfigurationsdatei konnte nicht "
            + "gefunden werden.\n"
            + "Frage bei stefan.huber.niedling@outlook.com nach.";
    /**
     * Telling the user to check whether the database is reachable.
     */
    public static final String CHECK_CONNECTION = "Prüfe, ob die Datenbank "
            + "erreichbar ist, und ob du Grün2 richtig konfiguriert hast.";
    /**
     * Telling the user to check his input.
     */
    public static final String CHECK_INPUT = "Prüfe deine Eingaben.";
    /**
     * Indicating that a SEPA debit could not be created.
     */
    public static final String NO_SEPA_DEBIT = "Sepalastschrift konnte nicht "
            + "erstellt werden";
    /**
     * Telling the user that every IBAN has a correct checksum.
     */
    public static final String CORRECT_IBANS = "Alle IBANs haben eine korrekte "
            + "Prüfsumme";
    /**
     * Indicating that the connection broke or was not able to establish.
     */
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

    private void initDialog(Stage s) {
        try {
            start(s);
        } catch (Exception ex) {
            Logger.getLogger(ConfirmDialog.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userConfirmed() {
        return userConfirmed;
    }

    /**
     * Creates a dialog indicating that the configurations are bad.
     *
     * @param owner The owner of the dialog.
     * @param s The stage to use.
     * @return The created dialog.
     */
    public static ConfirmDialog createBadConfigsDialog(Stage s, Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(BAD_CONFIGS, owner);
        dialog.initDialog(s);
        return dialog;
    }

    /**
     * Creates a dialog telling the user to check his input.
     *
     * @param owner The owner of the dialog.
     * @param s The stage to use.
     * @return The created dialog.
     */
    public static ConfirmDialog createCheckInputDialog(Stage s, Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(CHECK_INPUT, owner);
        dialog.initDialog(s);
        return dialog;
    }

    /**
     * Creates a dialog telling the user to check whether the database is
     * reachable.
     *
     * @param owner The owner of the dialog.
     * @param s The stage to use.
     * @return The created dialog.
     */
    public static ConfirmDialog createCheckConnectionDialog(Stage s,
            Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(CHECK_CONNECTION, owner);
        dialog.initDialog(s);
        return dialog;
    }

    /**
     * Creates a dialog indicating that the connection broke or was not able to
     * establish.
     *
     * @param owner The owner of the dialog.
     * @param s The stage to use.
     * @return The created dialog.
     */
    public static ConfirmDialog createUnexpectedAbbortDialog(Stage s,
            Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(UNEXPECTED_ABBORT, owner);
        dialog.initDialog(s);
        return dialog;
    }

    /**
     * Creates a dialog indicating that a SEPA debit could not be created.
     *
     * @param owner The owner of the dialog.
     * @param s The stage to use.
     * @return The created dialog.
     */
    public static ConfirmDialog createNoSepaDebitDialog(Stage s, Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(NO_SEPA_DEBIT, owner);
        dialog.initDialog(s);
        return dialog;
    }

    /**
     * Creates a dialog telling the user that every IBAN has a correct checksum.
     *
     * @param owner The owner of the dialog.
     * @param s The stage to use.
     * @return The created dialog.
     */
    public static ConfirmDialog createCorrectIbansDialog(Stage s,
            Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(CORRECT_IBANS, owner);
        dialog.initDialog(s);
        return dialog;
    }
}
