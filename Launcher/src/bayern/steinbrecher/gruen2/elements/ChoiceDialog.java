/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.data.DataProvider;
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
        Label message
                = new Label(DataProvider.getResourceValue("installUpdates"));

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
