package bayern.steinbrecher.gruen2;

import bayern.steinbrecher.gruen2.utility.ThreadUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.stage.Stage;

/**
 * Represents the base implementation of all other windows.
 *
 * @author Stefan Huber
 */
public abstract class View extends Application {

    /**
     * The stage which has to be set in every start-Method of implementing
     * classes.
     */
    protected Stage stage = null;
    private BooleanProperty gotShownProperty = new BooleanPropertyBase(false) {
        @Override
        public Object getBean() {
            return View.this;
        }

        @Override
        public String getName() {
            return "gotShown";
        }
    };
    private BooleanProperty gotClosedProperty = new BooleanPropertyBase(false) {
        @Override
        public Object getBean() {
            return View.this;
        }

        @Override
        public String getName() {
            return "gotClosed";
        }
    };
    private BooleanBinding wouldShowProperty = gotShownProperty.not();

    /**
     * Throws a {@code IllegalStateException} only if stage is {@code null}.
     */
    public void checkStage() {
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
     */
    protected void onlyShowOnceAndWait() {
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
     * After calling this method the window can be opened once again. But
     * previously inserted data stays unchanged.
     */
    public synchronized void reset() {
        gotClosedProperty.set(false);
        gotShownProperty.set(false);
    }

    /**
     * Returns the property indicating whether the next call of
     * {@code onlyShowOnceAndWait()} would open the window.
     *
     * @return The property indicating whether the next call of
     * {@code onlyShowOnceAndWait()} would open the window.
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
        throw new UnsupportedOperationException(
                "Contains no data to be confirmed");
    }
}
