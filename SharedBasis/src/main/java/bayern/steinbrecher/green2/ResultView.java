/*
 * Copyright (C) 2018 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Represents a {@link View} which represents a certain result.
 *
 * @author Stefan Huber
 * @param <T> The type of the result.
 * @param <C> The type of the controller associated with this view.
 */
public abstract class ResultView<T extends Optional<?>, C extends ResultController<T>> extends View<C> {

    private static final Logger LOGGER = Logger.getLogger(ResultView.class.getName());
    private final BooleanProperty gotShownProperty = new SimpleBooleanProperty(this, "gotShown", false);
    private final BooleanProperty gotClosedProperty = new SimpleBooleanProperty(this, "gotClosed", false);
    private final BooleanBinding wouldShowProperty = gotShownProperty.not();

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification = "The wait is called by other methods like "
            + "Stage#showAndWait() or ThreadUtility#waitWhile(...).")
    private void setClosedAndNotify() {
        gotClosedProperty.set(true);
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Shows the window if not shown yet, waits for it being closed and returns the result currently represented by the
     * associated {@link ResultController}. If multiple threads call this method only the first opens the window and
     * notifies the other threads when the window is getting closed.
     *
     * @return The result currently represented by the associated {@link ResultController}.
     * @see ResultController#getResult()
     */
    public T getResult() {
        if (!gotShownProperty.get()) {
            gotShownProperty.set(true);
            if (Platform.isFxApplicationThread()) {
                getStage().showAndWait();
                setClosedAndNotify();
            } else {
                getStage().showingProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        setClosedAndNotify();
                    }
                });
                Platform.runLater(() -> getStage().show());
            }
        }
        while (!gotClosedProperty.get()) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        return getController().getResult();
    }

    /**
     * After calling this method the window can be opened once again. But previously inserted data stays unchanged.
     */
    public synchronized void reset() {
        gotClosedProperty.set(false);
        gotShownProperty.set(false);
    }

    /**
     * Returns the property indicating whether the next call of {@link View#showOnceAndWait()} would open the window.
     *
     * @return The property indicating whether the next call of {@link View#showOnceAndWait()} would open the window.
     */
    public BooleanBinding wouldShowBinding() {
        return wouldShowProperty;
    }
}
