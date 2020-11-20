package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler.LogoSet;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class StyleUtility {
    private StyleUtility() {
        throw new UnsupportedOperationException("The construction of instances is prohibited");
    }

    public static void prepare(Stage stage) {
        stage.getScene()
                .getStylesheets()
                .add(EnvironmentHandler.DEFAULT_STYLESHEET);
        stage.getIcons()
                .add(EnvironmentHandler.LogoSet.LOGO.get());
    }

    public static void prepare(Pane pane) {
        pane.getStylesheets()
                .add(EnvironmentHandler.DEFAULT_STYLESHEET);
        ((Stage) pane.getScene()
                .getWindow())
                .getIcons()
                .add(LogoSet.LOGO.get());
    }
}
