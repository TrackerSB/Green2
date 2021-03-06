package bayern.steinbrecher.green2.configurationDialog;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.elements.ProfileChoice;
import bayern.steinbrecher.green2.sharedBasis.utility.StagePreparer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a dialog for configuring Green2.
 *
 * @author Stefan Huber
 */
public class ConfigDialog extends Application implements StagePreparer {

    private static final Logger LOGGER = Logger.getLogger(ConfigDialog.class.getName());

    @Override
    public void start(Stage primaryStage) {
        ProfileChoice.askForProfile(true).ifPresent(profile -> {
            Stage stage = getPreparedStage();
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

    @Override
    public Collection<String> getRegisteredStylesheets() {
        Collection<String> stylesheets = new ArrayList<>(StagePreparer.super.getRegisteredStylesheets());
        stylesheets.add(
                getClass()
                        .getResource("/bayern/steinbrecher/green2/configurationDialog/configDialog.css")
                        .toExternalForm());
        return stylesheets;
    }

    /**
     * main-method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
