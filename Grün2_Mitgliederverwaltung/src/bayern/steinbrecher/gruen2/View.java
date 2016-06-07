package bayern.steinbrecher.gruen2;

import bayern.steinbrecher.gruen2.utility.ThreadUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

/**
 * Represents the base implementation of all other windows.
 *
 * @author Stefan Huber
 * @param <T> The class used as controller for this view.
 */
public abstract class View<T extends Controller> extends Application {

    /**
     * The stage which has to be set in every start-Method of implementing
     * classes.
     */
    protected Stage stage = null;
    private BooleanProperty gotShownProperty
            = new SimpleBooleanProperty(this, "gotShown", false);
    private BooleanProperty gotClosedProperty
            = new SimpleBooleanProperty(this, "gotClosed", false);
    private BooleanBinding wouldShowProperty = gotShownProperty.not();
    protected T controller;

    /**
     * Throws a {@code IllegalStateException} only if stage is {@code null}.
     */
    protected void checkStage() {
        if (stage == null) {
            throw new IllegalStateException("You have to call start(...)first");
        }
    }

    private void setClosedAndNotify() {
        gotClosedProperty.set(true);
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Makes sure the window is only shown once. When multiple threads are
     * calling this method they will be set to {@code wait} apart from the first
     * one. This one opens the stage set in {@code start}, blocks until the
     * window is closed and then notifies all other threads. If the JavaFX
     * Application Thread calls it, it calls {@code showAndWait}.
     *
     * @see Stage#showAndWait()
     * @see View#showOnce(java.lang.Runnable)
     */
    public void showOnceAndWait() {
        checkStage();
        if (!gotShownProperty.get()) {
            gotShownProperty.set(true);
            if (Platform.isFxApplicationThread()) {
                stage.showAndWait();
                setClosedAndNotify();
            } else {
                stage.showingProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        setClosedAndNotify();
                    }
                });
                Platform.runLater(() -> stage.show());
            }
        }
        ThreadUtility.waitWhile(this, gotClosedProperty.not());
    }

    /**
     * Makes sure the window is only shown once. The first call opens the view
     * and returns. Further calls have no effect until {@code reset()} is
     * called. This method does NOT block.
     *
     * @param callback The runnable to call when the view gets closed.
     * @see View#showOnceAndWait()
     */
    public void showOnce(Runnable callback) {
        checkStage();
        if (!gotShownProperty.get()) {
            gotShownProperty.set(true);
            stage.showingProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    callback.run();
                    setClosedAndNotify();
                }
            });
            Platform.runLater(() -> stage.show());
        }
    }

    /**
     * After calling this method the window can be opened once again. But
     * previously inserted data stays unchanged.
     */
    public synchronized void reset() {
        gotClosedProperty.set(false);
        gotShownProperty.set(false);
    }

    /**
     * Returns the property indicating whether the next call of
     * {@code showOnceAndWait()} would open the window.
     *
     * @return The property indicating whether the next call of
     * {@code showOnceAndWait()} would open the window.
     */
    public BooleanBinding wouldShowBinding() {
        return wouldShowProperty;
    }

    /**
     * Indicates whether the currently inserted data is confirmed by the user.
     *
     * @return {@code true} only if the user confirmed the currently inserted
     * data.
     */
    public boolean userConfirmed() {
        return controller.userConfirmed();
    }
}
