package bayern.steinbrecher.gruen2;

import bayern.steinbrecher.gruen2.selection.Selection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Represents the base implementation of all other windows.
 *
 * @author Stefan Huber
 */
public abstract class Model extends Application {

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
    protected synchronized void onlyShowOnce() {
        if (!gotShown) {
            gotShown = true;
            stage.showAndWait();
            gotClosed = true;
            notifyAll();
        }
        while (!gotClosed) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Selection.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
}
