package bayern.steinbrecher.green2.configurationDialog;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.elements.ProfileChoice;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a dialog for configuring Green2.
 *
 * @author Stefan Huber
 */
public class ConfigDialog extends Application {

    private static final Logger LOGGER = Logger.getLogger(ConfigDialog.class.getName());

    @Override
    public void start(Stage primaryStage) {
        ProfileChoice.askForProfile(true).ifPresent(profile -> {
            Stage stage = EnvironmentHandler.STAGE_FACTORY.create();
            try {
                EnvironmentHandler.loadProfile(profile);

                FXMLLoader fxmlLoader = new FXMLLoader(
                        getClass().getResource("ConfigDialog.fxml"), EnvironmentHandler.RESOURCE_BUNDLE);
                Parent root = fxmlLoader.load();
                stage.getScene()
                        .setRoot(root);
                ((ConfigDialogController) fxmlLoader.getController())
                        .setStage(stage);

                stage.showingProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal && profile.isNewProfile()) {
                        try {
                            profile.deleteProfile();
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE, "The profile temporary generated for creating a new one "
                                    + "could not be deleted again.", ex);
                        }
                    }
                });
                stage.setResizable(false);
                stage.show();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
