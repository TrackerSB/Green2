package bayern.steinbrecher.gruen2;

import bayern.steinbrecher.gruen2.selection.Selection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
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
    protected Stage stage;
    private boolean gotShown = false;
    private boolean gotClosed = false;

    /**
     * Makes sure the window is only shown once. When multiple threads are
     * calling this method they will be blocked apart from the first one. This
     * one opens the stage set in {@code start}, blocks until the window is
     * closed and then notifies all other threads.
     */
    protected void onlyShowOnce() {
        if (!gotShown) {
            gotShown = true;
            stage.showingProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    gotClosed = true;
                    synchronized (this) {
                        notifyAll();
                    }
                }
            });
            Platform.runLater(() -> stage.show());
        }
        while (!gotClosed) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Selection.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * After calling this method the window can be opened once again. But
     * previously inserted data stays unchanged.
     */
    public synchronized void reset() {
        gotClosed = false;
        gotShown = false;
    }

    /**
     * Checks whether the next call of {@code onlyShowOnce()} would open the
     * window.
     *
     * @return {@code true} only if the next call of {@code onlyShowOnce()}
     * would open the window.
     */
    public boolean wouldShow() {
        return !gotShown;
    }
}
