package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
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
}
