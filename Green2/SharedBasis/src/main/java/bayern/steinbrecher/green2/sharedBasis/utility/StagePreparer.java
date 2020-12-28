package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.wizard.StandaloneWizardPage;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardPage;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NOTE This interface should only be implemented by classes that may actually provide {@link Stage}s like classes that
 * inherit {@link StandaloneWizardPage} or do not inherit at all. If a class e.g. inherits from {@link WizardPage} it
 * should not implement this interface since the stylesheets returned by {@link #getRegisteredStylesheets()} are not
 * applied automatically if a {@link WizardPage} is embedded into a {@link Wizard}.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public interface StagePreparer {
    Logger LOGGER = Logger.getLogger(StagePreparer.class.getName());
    String LOGO_PATH = StagePreparer.class
            .getResource("/bayern/steinbrecher/green2/sharedBasis/icons/logo.png")
            .toExternalForm();
    Image LOGO = new Image(LOGO_PATH);
    String DEFAULT_STYLESHEET = StagePreparer.class
            .getResource("/bayern/steinbrecher/green2/sharedBasis/styles/styles.css")
            .toExternalForm();

    /**
     * Return a sequence of paths of all stylesheets that have to be applied on stage level, i.e. to all components.
     * NOTE Every path should be in external form.
     *
     * @see Class#getResource(String)
     * @see URL#toExternalForm()
     */
    default Collection<String> getRegisteredStylesheets() {
        return List.of(DEFAULT_STYLESHEET);
    }

    private static void addLogo(Stage stage) {
        stage.getIcons()
                .add(LOGO);
    }

    private void addStyles(Scene scene) {
        scene.getStylesheets()
                .addAll(getRegisteredStylesheets());
    }

    private <T> T runLaterBlocking(Callable<T> actions) {
        AtomicReference<T> resultRef = new AtomicReference<>();
        Runnable exceptionSafeExecution = () -> {
            try {
                resultRef.set(actions.call());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Actions on FXAppThread failed", ex);
                resultRef.set(null);
            }
        };
        if (Platform.isFxApplicationThread()) {
            exceptionSafeExecution.run();
        } else {
            Platform.runLater(() -> {
                exceptionSafeExecution.run();
                synchronized (resultRef) {
                    resultRef.notifyAll();
                }
            });
            while (resultRef.get() == null) {
                try {
                    synchronized (resultRef) {
                        resultRef.wait();
                    }
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO,
                            "Waiting for FX main thread to execute the given actions was interrupted", ex);
                }
            }
        }
        return resultRef.get();
    }

    /**
     * <ul>
     *     <li>Create a {@link Stage}</li>
     *     <li>Attach the application logo to it</li>
     *     <li>Add an empty {@link Scene}</li>
     *     <li>Attach registered stylesheet to it</li>
     * </ul>
     * NOTE This method may be called from any thread.
     */
    default Stage getPreparedStage() {
        Stage stage = runLaterBlocking(Stage::new);
        if (stage == null) {
            throw new IllegalStateException("Couldn't create a stage");
        } else {
            addLogo(stage);
            Scene scene = new Scene(new Label("The scene is empty"));
            addStyles(scene);
            runLaterBlocking(() -> {
                stage.setScene(scene);
                return null;
            })
            return stage;
        }
    }

    /**
     * Returns a prepared {@link Stage} which has the application logo and an empty {@link Scene} to which the default
     * stylesheet is attached to.
     *
     * @see #getPreparedStage()
     */
    static Stage getDefaultPreparedStage() {
        // @formatter: off
        return new StagePreparer() {}.getPreparedStage();
        // @formatter: on
    }

    static <T extends Alert> T prepare(T alert) {
        Scene scene = alert.getDialogPane()
                .getScene();
        if (scene == null) {
            LOGGER.log(Level.WARNING, "Neither styles nor logo can be attached to the alert since it has no scene");
        } else {
            scene.getStylesheets()
                    .add(DEFAULT_STYLESHEET);
            Window window = scene.getWindow();
            if (window instanceof Stage) {
                addLogo((Stage) window);
            } else {
                LOGGER.log(Level.WARNING,
                        "Cannot associate application logo to alert since it is not attached to a stage");
            }
        }
        return alert;
    }
}
