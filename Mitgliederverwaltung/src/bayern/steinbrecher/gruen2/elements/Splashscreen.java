/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.utility.ServiceFactory;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        imageView.setImage(DataProvider.SPLASHSCREEN);
        root.getChildren().add(imageView);

        Scene scene = new Scene(root);
        scene.setFill(null);
        stage.setScene(scene);
        stage.setTitle(DataProvider.getResourceValue("startingApplication"));
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
