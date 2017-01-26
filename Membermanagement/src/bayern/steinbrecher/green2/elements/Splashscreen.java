/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.Controller;
import bayern.steinbrecher.green2.View;
import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.utility.ServiceFactory;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        ImageView imageView = new ImageView();
        imageView.setImage((Locale.getDefault().equals(Locale.GERMAN)
                ? DataProvider.ImageSet.SPLASHSCREEN_DE : DataProvider.ImageSet.SPLASHSCREEN_EN).get());
        root.getChildren().add(imageView);

        Scene scene = new Scene(root);
        scene.setFill(null);
        stage.setScene(scene);
        stage.setTitle(DataProvider.getResourceValue("startingApplication"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(DataProvider.ImageSet.LOGO.get());
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
                Logger.getLogger(Splashscreen.class.getName())
                        .log(Level.WARNING, null, ex);
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
     *               {@code start(...)}).
     */
    public static void showSplashscreen(long millis, Stage stage) {
        try {
            Splashscreen s = new Splashscreen();
            s.start(stage);
            s.showSplashscreen(millis);
        } catch (Exception ex) {
            Logger.getLogger(Splashscreen.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
