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
package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.util.Optional;
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

        HBox hbox = new HBox(yesButton, noButton);
        hbox.setSpacing(10);

        VBox vbox = new VBox(message, hbox);
        vbox.setSpacing(10);
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
        ChoiceDialog cd = new ChoiceDialog();
        cd.start(new Stage());
        return cd.installUpdates;
    }
}
