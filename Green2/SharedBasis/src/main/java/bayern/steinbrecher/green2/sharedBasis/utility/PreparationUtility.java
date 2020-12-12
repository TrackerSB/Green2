package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PreparationUtility {

    private static final Logger LOGGER = Logger.getLogger(PreparationUtility.class.getName());
    private static final String LOGO_PATH = PreparationUtility.class
            .getResource("/bayern/steinbrecher/green2/sharedBasis/icons/logo.png")
            .toExternalForm();
    private static final Image LOGO = new Image(LOGO_PATH);

    private PreparationUtility() {
        throw new UnsupportedOperationException("The construction of instances is prohibited");
    }

    public static <T extends Scene> T addStyle(T scene) {
        Objects.requireNonNull(scene, "Cannot attach style to a null scene")
                .getStylesheets()
                .add(EnvironmentHandler.DEFAULT_STYLESHEET);
        return scene;
    }

    public static <T extends Parent> T addStyle(T pane) {
        Objects.requireNonNull(pane, "Cannot attach style to a null pane")
                .getStylesheets()
                .add(EnvironmentHandler.DEFAULT_STYLESHEET);
        return pane;
    }

    public static <T extends Dialog<?>> T addStyle(T dialog) {
        addStyle(Objects.requireNonNull(dialog).getDialogPane());
        return dialog;
    }

    public static <T extends Stage> T addLogo(T stage) {
        Objects.requireNonNull(stage, "Cannot attach logo to a null stage")
                .getIcons()
                .add(LOGO);
        return stage;
    }

    public static <T extends Dialog<?>> T addLogo(T dialog) {
        addLogo((Stage) dialog.getDialogPane()
                .getScene()
                .getWindow());
        return dialog;
    }

    public static Stage getPreparedStage() {
        AtomicReference<Stage> stage = new AtomicReference<>();
        if (Platform.isFxApplicationThread()) {
            stage.set(new Stage());
        } else {
            Platform.runLater(() -> {
                stage.set(new Stage());
                stage.notifyAll();
            });
            while (stage.get() == null) {
                try {
                    stage.wait();
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO, "Waiting for FX main thread to return a stage was interrupted", ex);
                }
            }
        }
        addLogo(stage.get());
        return stage.get();
    }
}
