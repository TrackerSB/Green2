/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.Controller;
import bayern.steinbrecher.green2.View;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.ServiceFactory;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.Application;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a splashscreen.
 *
 * @author Stefan Huber
 */
public class Splashscreen extends View<Controller> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Pane root = new Pane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0)");

        ImageView imageView = new ImageView();
        imageView.setImage((Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage())
                ? EnvironmentHandler.LogoSet.SPLASHSCREEN_DE : EnvironmentHandler.LogoSet.SPLASHSCREEN_EN).get());
        root.getChildren().add(imageView);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle(EnvironmentHandler.getResourceValue("startingApplication"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
    }

    /**
     * Shows this splashscreen for {@code millis} milliseconds and blocks until
     * the splashscreen closes.
     *
     * @param millis The time in milliseconds the screen has to be shown.
     */
    public void showSplashscreen(long millis) {
        ServiceFactory.createService(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                Logger.getLogger(Splashscreen.class.getName()).log(Level.WARNING, null, ex);
            }
            Platform.runLater(() -> stage.close());
            return null;
        }).start();
        showOnceAndWait();
    }

    /**
     * Creates, shows a splashscreen for {@code millis} milliseconds and blocks
     * until the splashscreen closes.
     *
     * @param millis The time in milliseconds the screen has to be shown.
     * @param stage  The stage to use for the window. (Used as param for
     *               {@link Application#start(Stage)}).
     */
    public static void showSplashscreen(long millis, Stage stage) {
        try {
            Splashscreen s = new Splashscreen();
            s.start(stage);
            s.showSplashscreen(millis);
        } catch (Exception ex) {
            Logger.getLogger(Splashscreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
