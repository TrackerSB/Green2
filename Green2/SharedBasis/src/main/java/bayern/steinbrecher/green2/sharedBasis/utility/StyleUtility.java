package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class StyleUtility {

    private static final Logger LOGGER = Logger.getLogger(StyleUtility.class.getName());
    private static final String LOGO_PATH = StyleUtility.class
            .getResource("/bayern/steinbrecher/green2/sharedBasis/icons/logo.png")
            .toExternalForm();
    private static final Image LOGO = new Image(LOGO_PATH);

    private StyleUtility() {
        throw new UnsupportedOperationException("The construction of instances is prohibited");
    }

    public static void prepare(Stage stage) {
        stage.getIcons()
                .add(LOGO);
        Scene scene = stage.getScene();
        if (scene == null) {
            LOGGER.log(Level.WARNING, "Cannot attach stylesheet to a stage with no scene");
        } else {
            stage.getScene()
                    .getStylesheets()
                    .add(EnvironmentHandler.DEFAULT_STYLESHEET);
        }
    }

    public static void prepare(Pane pane) {
        pane.getStylesheets()
                .add(EnvironmentHandler.DEFAULT_STYLESHEET);
        Scene scene = pane.getScene();
        if (scene == null) {
            LOGGER.log(Level.WARNING, "Cannot attach logo since this pane is in no scene");
        } else {
            Window window = scene.getWindow();
            if (window == null) {
                LOGGER.log(Level.WARNING,
                        "Cannot attach logo since the scene this pane contains is not attached to a stage");
            } else {
                ((Stage) window)
                        .getIcons()
                        .add(LOGO);
            }
        }
    }
}
