package bayern.steinbrecher.gruen2.elements;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.utility.ServiceFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
public class Splashscreen extends View {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Pane root = new Pane();
        ImageView imageView = new ImageView();
        imageView.setImage(
                new Image("bayern/steinbrecher/gruen2/data/splashscreen.jpg"));
        root.getChildren().add(imageView);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(DataProvider.getIcon());
        stage.setScene(new Scene(root));
    }

    public void showSplashscreen(long millis) {
        ServiceFactory.createService(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                Logger.getLogger(Splashscreen.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            Platform.runLater(() -> stage.close());
            return null;
        }).start();
        onlyShowOnce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userConfirmed() {
        throw new UnsupportedOperationException(
                "Splashscreen has no data to confirm.");
    }
}
