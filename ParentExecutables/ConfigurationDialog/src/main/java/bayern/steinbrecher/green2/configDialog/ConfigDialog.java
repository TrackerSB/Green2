/* 
 * Copyright (C) 2018 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.configDialog;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.ProfileChoice;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
                EnvironmentHandler.loadProfile(profile);

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConfigDialog.fxml"));
                fxmlLoader.setResources(EnvironmentHandler.RESOURCE_BUNDLE);
                Parent root = fxmlLoader.load();
                root.getStylesheets().addAll(EnvironmentHandler.DEFAULT_STYLESHEET,
                        "/bayern/steinbrecher/green2/styles/configDialog.css");

                ConfigDialogController controller = fxmlLoader.getController();
                controller.setStage(stage);

                stage.showingProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal && profile.isNewProfile()) {
                        try {
                            profile.deleteProfile();
                        } catch (IOException ex) {
                            Logger.getLogger(ConfigDialog.class.getName())
                                    .log(Level.SEVERE, "The profile temporary generated for creating a new one "
                                            + "could not be deleted again.", ex);
                        }
                    }
                });
                stage.setResizable(false);
                stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
                stage.setScene(new Scene(root));
                stage.setTitle(EnvironmentHandler.getResourceValue("configureApplication") + ": " + profile.getProfileName());
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
