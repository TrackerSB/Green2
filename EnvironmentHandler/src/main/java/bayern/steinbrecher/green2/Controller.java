/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2;

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
    protected Stage stage = null;
    /**
     * Only {@code true} when the user explicitly aborted his input. (E.g. pressing the X of the window.)
     */
    private boolean userAborted = false;

    /**
     * Sets the stage the controller can refer to. (E.g. for closing the stage) NOTE: It overrides
     * {@link Stage#onCloseRequest}.
     *
     * @param stage The stage to refer to.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(evt -> userAborted = true);
    }

    /**
     * Throws a {@link IllegalStateException} only if stage is {@code null}.
     */
    protected void checkStage() {
        if (stage == null) {
            throw new IllegalStateException("You have to call setStage(...) first");
        }
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
