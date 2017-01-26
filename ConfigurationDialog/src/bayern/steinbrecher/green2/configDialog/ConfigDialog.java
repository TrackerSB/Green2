/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.configDialog;

import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.elements.ProfileChoice;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        ProfileChoice.askForProfile(true).ifPresent(profile -> {
            try {
                DataProvider.loadProfile(profile);

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConfigDialog.fxml"));
                fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
                Parent root = fxmlLoader.load();
                root.getStylesheets().add(DataProvider.STYLESHEET_PATH);

                ConfigDialogController controller = fxmlLoader.getController();
                controller.setStage(stage);

                stage.showingProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal && profile.isNewProfile()) {
                        profile.deleteProfile();
                    }
                });
                stage.setResizable(false);
                stage.getIcons().add(DataProvider.ImageSet.LOGO.get());
                stage.setScene(new Scene(root));
                stage.setTitle(DataProvider.getResourceValue("configureApplication") + ": " + profile.getProfileName());
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(ConfigDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
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
