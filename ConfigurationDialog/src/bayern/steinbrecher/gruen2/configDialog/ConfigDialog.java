/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.configDialog;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.elements.ProfileChoice;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a dialog for configuring Grün2.
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
                stage.getIcons().add(DataProvider.DEFAULT_ICON);
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
