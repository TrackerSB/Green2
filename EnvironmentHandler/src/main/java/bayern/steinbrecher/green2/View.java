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

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.ThreadUtility;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
    private Stage stage;
    private final BooleanProperty gotShownProperty = new SimpleBooleanProperty(this, "gotShown", false);
    private final BooleanProperty gotClosedProperty = new SimpleBooleanProperty(this, "gotClosed", false);
    private final BooleanBinding wouldShowProperty = gotShownProperty.not();
    /**
     * The controller handling the actions of this view.
     */
    private T controller;

    /**
     * Contains the body usually inserted in {@link Application#start(javafx.stage.Stage)}. This method shall not call
     * {@link Stage#show()}.
     *
     * @param stage The {@link Stage} to be used by this application.
     * @throws ViewStartException Thrown if something goes wrong. Since it is not known which {@link Exception} (if any)
     * is thrown by an implementing class one could throw a plain {@link Exception}. But if this is the case any call to
     * this method (or to {@link #start(javafx.stage.Stage)}) has either to throw also {@link Exception} itself or catch
     * this unspecific {@link Exception}. Hence a wrapping exception is introduced.
     * @see ViewStartException
     */
    protected abstract void startImpl(Stage stage);

    /**
     * Sets the stage of this view, calls {@link #startImpl(javafx.stage.Stage)} where the actual content of
     * {@link Application#start(javafx.stage.Stage)} has to be placed and sets the application logo.
     *
     * @param stage The stage to be used by this view.
     * @see #startImpl(javafx.stage.Stage)
     */
    @Override
    public final void start(Stage stage) {
        this.stage = stage;
        startImpl(stage);
        stage.getIcons().add(EnvironmentHandler.LogoSet.LOGO.get());
    }

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification = "The wait is called by other methods like "
            + "Stage#showAndWait() or ThreadUtility#waitWhile(...).")
    private void setClosedAndNotify() {
        gotClosedProperty.set(true);
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * This method is called right before {@link #loadFXML(java.lang.String)} returns. It may be overriden to make sure
     * certain methods are called whenever {@link #loadFXML(java.lang.String)} is called.
     */
    protected void callWhenLoadFXML() {
        //no-op
    }

    /**
     * Loads the given FXML ressource. If it is needed to call certain methods each time this method is called, override
     * {@link #callWhenLoadFXML()}. E.g. set some options to the {@link Controller} or adding stylesheets and resource
     * bundles.
     *
     * @param <P> The concrete type of the root element of the given resource.
     * @param resource The FXML resource.
     * @return The root of{@code resource}.
     * @throws IOException Thrown only if the given resource could not be loaded by {@link FXMLLoader#load()}.
     * @see Class#getResource(java.lang.String)
     */
    @SuppressFBWarnings(value = "UI_INHERITANCE_UNSAFE_GETRESOURCE", justification = "Since the fxml files used by "
            + "the views are located in the same package relative paths have to be used.")
    protected final <P extends Parent> P loadFXML(String resource) throws IOException {
        //NOTE getClass() is needed since View.class may result in bad paths
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(resource));
        fxmlLoader.setResources(EnvironmentHandler.RESOURCE_BUNDLE);
        P root = fxmlLoader.load();
        //Make sure the default stylesheet does not override other stylesheets.
        root.getStylesheets().add(0, EnvironmentHandler.DEFAULT_STYLESHEET);
        controller = fxmlLoader.getController();
        callWhenLoadFXML();
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
        if (!gotShownProperty.get()) {
            gotShownProperty.set(true);
            getStage().showingProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    if (callback != null) {
                        callback.run();
                    }
                    setClosedAndNotify();
                }
            });
            Platform.runLater(() -> getStage().show());
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
        return getController().userAbborted();
    }

    /**
     * Returns the {@link Stage} of the application.
     *
     * @return The {@link Stage} to be used by the application.
     * @throws IllegalStateException Thrown only if the {@link Stage} is not set. This means
     * {@link #start(javafx.stage.Stage)} has to be called before.
     */
    protected Stage getStage() {
        if (stage == null) {
            throw new IllegalStateException("You have to call start(...) first");
        } else {
            return stage;
        }
    }

    /**
     * Returns the {@link Controller} used by the application.
     *
     * @return The {@link Controller} used by the application.
     * @throws IllegalStateException Thrown if the {@link Controller} is not set. This means
     * {@link #loadFXML(java.lang.String)} has to be called before.
     */
    protected T getController() {
        if (controller == null) {
            throw new IllegalStateException("You have to call loadFXML(..) first");
        } else {
            return controller;
        }
    }
}
