/* 
 * Copyright (C) 2017 Stefan Huber
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

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.ThreadUtility;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Represents the base implementation of all other windows.
 *
 * @param <T> The class used as controller for this view.
 * @author Stefan Huber
 */
public abstract class View<T extends Controller> extends Application {

    /**
     * The stage which has to be set in every start-Method of implementing classes.
     */
    protected Stage stage = null;
    private final BooleanProperty gotShownProperty = new SimpleBooleanProperty(this, "gotShown", false);
    private final BooleanProperty gotClosedProperty = new SimpleBooleanProperty(this, "gotClosed", false);
    private final BooleanBinding wouldShowProperty = gotShownProperty.not();
    protected T controller;

    /**
     * Throws a {@link IllegalStateException} only if stage is {@code null}.
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
     * Loads the given FMXL resource. This method may be overidden if some informations have to passed to the
     * controller. This should be done the following way:
     * {@code P root = super.loadFXML(resource);controller.setSomeInput(input);return root;}
     *
     * @param <P> The concrete type of the root element of the given resource.
     * @param resource The FXML resource.
     * @return The root of{@code resource}.
     * @throws IOException Thrown only if the given resource could not be loaded by {@link FXMLLoader#load()}.
     * @see Class#getResource(java.lang.String)
     */
    protected <P extends Parent> P loadFXML(String resource) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(resource));
        fxmlLoader.setResources(EnvironmentHandler.RESOURCE_BUNDLE);
        P root = fxmlLoader.load();
        root.getStylesheets().add(EnvironmentHandler.DEFAULT_STYLESHEET);
        controller = fxmlLoader.getController();
        return root;
    }

    /**
     * Makes sure the window is only shown once. When multiple threads are calling this method they will be set to
     * {@link Thread#wait()} apart from the first one. This one opens the stage set in {@link Application#start(Stage)},
     * blocks until the window is closed and then notifies all other threads. If the JavaFX Application Thread calls it,
     * it calls {@link View#showOnceAndWait()}.
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
     * Makes sure the window is only shown once. The first call opens the view and returns. Further calls have no effect
     * until {@link View#reset()} is called. This method does NOT block.
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
                    if (callback != null) {
                        callback.run();
                    }
                    setClosedAndNotify();
                }
            });
            Platform.runLater(() -> stage.show());
        }
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

    /**
     * Indicates whether the currently inserted data is aborted by the user.
     *
     * @return {@code true} only if the user aborted the currently inserted data.
     */
    public boolean userAborted() {
        return controller.userAbborted();
    }
}
