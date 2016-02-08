package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.data.DataProvider;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Represents a simple confirm dialog.
 *
 * @author Stefan Huber
 */
public class ConfirmDialog {

    public static void showConfirmDialog(String message, Window owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size:14px");
        Button okButton = new Button("OK");
        okButton.setOnAction(aevt -> stage.close());
        VBox dialogContent = new VBox();
        dialogContent.getChildren()
                .addAll(messageLabel, okButton);
        dialogContent.setStyle("-fx-spacing:10px");
        dialogContent.setAlignment(Pos.CENTER);

        stage.setMinWidth(250);
        stage.setMinHeight(100);
        Scene scene = new Scene(dialogContent);
        stage.setScene(scene);
        stage.setTitle("Info");
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.getIcon());
        stage.showAndWait();
    }
}
