/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.login.standard;

import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.login.Login;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Represents a login without SSH.
 *
 * @author Stefan Huber
 */
public class DefaultLogin extends Login {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("DefaultLogin.fxml"));
        fxmlLoader.setResources(DataProvider.RESOURCE_BUNDLE);
        Parent root = fxmlLoader.load();
        root.getStylesheets().add(DataProvider.STYLESHEET_PATH);

        controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle(DataProvider.getResourceValue("loginTitle"));
        stage.setResizable(false);
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());
    }
}
