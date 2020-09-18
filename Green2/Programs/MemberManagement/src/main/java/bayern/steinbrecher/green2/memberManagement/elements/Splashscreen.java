package bayern.steinbrecher.green2.memberManagement.elements;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Represents a splashscreen.
 *
 * @author Stefan Huber
 */
public class Splashscreen extends Application {

    private static final Logger LOGGER = Logger.getLogger(Splashscreen.class.getName());
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Pane root = new Pane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0)");

        ImageView imageView = new ImageView();
        EnvironmentHandler.LogoSet splashscreenImage;
        if (Locale.getDefault().equals(Locale.GERMANY)) {
            splashscreenImage = EnvironmentHandler.LogoSet.SPLASHSCREEN_DE;
        } else {
            splashscreenImage = EnvironmentHandler.LogoSet.SPLASHSCREEN_EN;
        }
        imageView.setImage(splashscreenImage.get());
        root.getChildren().add(imageView);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle(EnvironmentHandler.getResourceValue("startingApplication"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
    }

    /**
     * Shows this splashscreen for {@code millis} milliseconds and blocks until the splashscreen closes.
     *
     * @param millis The time in milliseconds the screen has to be shown.
     */
    public void showSplashscreen(long millis) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {

            }
            Platform.runLater(() -> stage.close());
        })
                .exceptionally(ex -> {
                    LOGGER.log(Level.SEVERE, "The splashscreen was interrupted.", ex);
                    return null;
                });
        stage.showAndWait();
    }

    /**
     * Creates, shows a splashscreen for {@code millis} milliseconds and blocks until the splashscreen closes.
     *
     * @param millis The time in milliseconds the screen has to be shown.
     * @param stage The stage to use for the window. (Used as param for
     * {@link javafx.application.Application#start(Stage)}).
     */
    public static void showSplashscreen(long millis, Stage stage) {
        Splashscreen splashscreen = new Splashscreen();
        splashscreen.start(stage);
        splashscreen.showSplashscreen(millis);
    }
}
