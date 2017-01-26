/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
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
     * Only {@code true} when the user explicitly abborted his input. (E.g.
     * pressing the X of the window.)
     */
    private boolean userAbborted = false;

    /**
     * Sets the stage the conroller can refer to. (E.g. for closing the stage)
     * NOTE: It overrides {@code onCloseRequest}.
     *
     * @param stage The stage to refer to.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(evt -> userAbborted = true);
    }

    /**
     * Throws a {@code IllegalStateException} only if stage is {@code null}.
     */
    protected void checkStage() {
        if (stage == null) {
            throw new IllegalStateException(
                    "You have to call setStage(...) first");
        }
    }

    /**
     * Checks whether the user abborted his input.
     *
     * @return {@code true} only if the user abborted his input explicitly.
     */
    public boolean userAbborted() {
        return userAbborted;
    }
}
