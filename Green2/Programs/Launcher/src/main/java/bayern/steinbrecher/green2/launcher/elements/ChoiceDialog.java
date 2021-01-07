package bayern.steinbrecher.green2.launcher.elements;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.utility.StagePreparer;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * Represents a dialog showing &bdquo;YES&ldquo; and &bdquo;NO&ldquo;.
 *
 * @author Stefan Huber
 */
public class ChoiceDialog implements StagePreparer {

    private Boolean installUpdates;

    public void embedContentIntoAndWait(Stage stage, HostServices hostServices) {
        Label message = new Label(EnvironmentHandler.getResourceValue("installUpdates"));

        Button yesButton = new Button(EnvironmentHandler.getResourceValue("yes"));
        yesButton.setDefaultButton(true);
        yesButton.setOnAction(evt -> {
            installUpdates = true;
            stage.close();
        });

        Button noButton = new Button(EnvironmentHandler.getResourceValue("no"));
        noButton.setOnAction(evt -> {
            installUpdates = false;
            stage.close();
        });

        Hyperlink hyperlink = new Hyperlink("https://github.com/TrackerSB/Green2/releases/latest");
        hyperlink.setOnAction(aevt -> {
            hostServices.showDocument("https://github.com/TrackerSB/Green2/releases/latest");
        });
        VBox vbox = new VBox(message, hyperlink, new HBox(yesButton, noButton));

        stage.getScene()
                .setRoot(vbox);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.setTitle(EnvironmentHandler.getResourceValue("installUpdates"));
        stage.showAndWait();
    }

    /**
     * This method opens a window asking the user whether to install updates, blocks until the user closes the window or
     * presses a button and returns the users choice.
     *
     * @return {@link Optional#empty()} only if the user closed the window without clicking yes or no.
     */
    public static Optional<Boolean> askForUpdate(HostServices hostServices) {
        ChoiceDialog choiceDialog = new ChoiceDialog();
        Stage choiceDialogStage = choiceDialog.getPreparedStage();
        choiceDialog.embedContentIntoAndWait(choiceDialogStage, hostServices);
        return Optional.ofNullable(choiceDialog.installUpdates);
    }
}
