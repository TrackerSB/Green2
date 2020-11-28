package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class StyleUtility {

    private static final String LOGO_PATH = StyleUtility.class
            .getResource("/bayern/steinbrecher/green2/sharedBasis/icons/logo.png")
            .toExternalForm();
    private static final Image LOGO = new Image(LOGO_PATH);

    private StyleUtility() {
        throw new UnsupportedOperationException("The construction of instances is prohibited");
    }

    public static void prepare(Stage stage) {
        stage.getScene()
                .getStylesheets()
                .add(EnvironmentHandler.DEFAULT_STYLESHEET);
        stage.getIcons()
                .add(LOGO);
    }

    public static void prepare(Pane pane) {
        pane.getStylesheets()
                .add(EnvironmentHandler.DEFAULT_STYLESHEET);
        ((Stage) pane.getScene()
                .getWindow())
                .getIcons()
                .add(LOGO);
    }
}
