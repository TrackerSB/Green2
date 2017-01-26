/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.data.DataProvider;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    private boolean installUpdates = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        Label message = new Label(DataProvider.getResourceValue("installUpdates"));

        Button yesButton = new Button(DataProvider.getResourceValue("yes"));
        yesButton.setDefaultButton(true);
        yesButton.setOnAction(evt -> {
            installUpdates = true;
            stage.close();
        });

        Button noButton = new Button(DataProvider.getResourceValue("no"));
        noButton.setOnAction(evt -> stage.close());

        HBox hbox = new HBox(yesButton, noButton);
        hbox.setSpacing(10);

        VBox vbox = new VBox(message, hbox);
        vbox.setSpacing(10);
        vbox.getStylesheets().add(DataProvider.STYLESHEET_PATH);

        stage.setScene(new Scene(vbox));
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.setTitle(DataProvider.getResourceValue("installUpdates"));
        stage.showAndWait();
    }

    /**
     * This method opens a window asking the user whether to install updates,
     * blocks until the user closes the window or presses a button and returns
     * the users choice.
     *
     * @return {@code true} only if the user presses "YES".
     */
    public static boolean askForUpdate() {
        ChoiceDialog cd = new ChoiceDialog();
        cd.start(new Stage());
        return cd.installUpdates;
    }
}
