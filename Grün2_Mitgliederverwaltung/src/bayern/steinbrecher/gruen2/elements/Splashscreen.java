/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.utility.ServiceFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        imageView.setImage(DataProvider.SPLASHSCREEN);
        root.getChildren().add(imageView);

        Scene scene = new Scene(root);
        scene.setFill(null);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(DataProvider.DEFAULT_ICON);
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
     * @param stage The stage to use for the window. (Used as param for
     * {@code start(...)}).
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
