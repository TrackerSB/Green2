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
        scene.getStylesheets().add(DataProvider.STYLESHEET_PATH);
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(owner);
        stage.setTitle("Info");
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
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
     * Creates a dialog with given message.
     *
     * @param owner The owner of the dialog.
     * @param s The stage to use.
     * @param message The messge to show to the user.
     * @return The created dialog.
     */
    public static ConfirmDialog createDialog(Stage s, Window owner,
            String message) {
        ConfirmDialog dialog = new ConfirmDialog(message, owner);
        dialog.initDialog(s);
        return dialog;
    }

    /**
     * Creates a dialog telling the user to check whether the given address is
     * reachable and configs are valid.
     *
     * @param address The address of the connection which was not found.
     * @param owner The owner of the dialog.
     * @param s The stage to use.
     * @return The created dialog.
     */
    public static ConfirmDialog createCheckConnectionDialog(String address,
            Stage s, Window owner) {
        ConfirmDialog dialog = new ConfirmDialog(
                DataProvider.getResourceValue("checkConnection") + "\n"
                + address
                + DataProvider.getResourceValue("notReachable"), owner);
        dialog.initDialog(s);
        return dialog;
    }
}
