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

import android.support.annotation.CallSuper;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 * Represents a controller all other controller have to extend.
 *
 * @author Stefan Huber
 */
public abstract class Controller implements Initializable {

    /**
     * The stage the controller has to interact with.
     */
    private ObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    /**
     * Only {@code true} when the user explicitly aborted his input. (E.g. pressing the X of the window.)
     */
    private boolean userAborted = false;

    @Override
    @CallSuper
    public void initialize(URL location, ResourceBundle resources) {
        //no-op.
        //TODO Force call of super.initialize(...)
    }

    /**
     * Returns the property holding the currently set {@link Stage}.
     *
     * @return The property holding the currently set {@link Stage}.
     */
    public ObjectProperty<Stage> stageProperty() {
        return stage;
    }

    /**
     * Sets the stage the controller can refer to. (E.g. for closing the stage) NOTE: It overrides
     * {@link Stage#onCloseRequest}.
     *
     * @param stage The stage to refer to.
     */
    public void setStage(Stage stage) {
        stageProperty().set(stage);
        stage.setOnCloseRequest(evt -> userAborted = true);
    }

    /**
     * Returns the currently set {@link Stage}.
     *
     * @return The currently set {@link Stgae}.
     */
    public Stage getStage() {
        return stageProperty().get();
    }

    /**
     * Checks whether the user aborted his input.
     *
     * @return {@code true} only if the user aborted his input explicitly.
     */
    public boolean userAbborted() {
        return userAborted;
    }
}
