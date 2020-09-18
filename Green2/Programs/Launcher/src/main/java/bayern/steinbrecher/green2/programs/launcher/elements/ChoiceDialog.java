package bayern.steinbrecher.green2.programs.launcher.elements;

import java.util.Optional;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Represents a dialog showing &bdquo;YES&ldquo; and &bdquo;NO&ldquo;.
 *
 * @author Stefan Huber
 */
public class ChoiceDialog extends Application {

    private Optional<Boolean> installUpdates = Optional.empty();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        Label message = new Label(EnvironmentHandler.getResourceValue("installUpdates"));

        Button yesButton = new Button(EnvironmentHandler.getResourceValue("yes"));
        yesButton.setDefaultButton(true);
        yesButton.setOnAction(evt -> {
            installUpdates = Optional.of(true);
            stage.close();
        });

        Button noButton = new Button(EnvironmentHandler.getResourceValue("no"));
        noButton.setOnAction(evt -> {
            installUpdates = Optional.of(false);
            stage.close();
        });

        Hyperlink hyperlink = new Hyperlink("https://github.com/TrackerSB/Green2/releases/latest");
        hyperlink.setOnAction(aevt -> {
            getHostServices().showDocument("https://github.com/TrackerSB/Green2/releases/latest");
        });
        VBox vbox = new VBox(message, hyperlink, new HBox(yesButton, noButton));
        vbox.getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);

        stage.setScene(new Scene(vbox));
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
    public static Optional<Boolean> askForUpdate() {
        ChoiceDialog choiceDialog = new ChoiceDialog();
        choiceDialog.start(new Stage());
        return choiceDialog.installUpdates;
    }
}
