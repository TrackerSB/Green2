/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2;

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
     * Only {@code true} when the user explicitly confirmed his input. (E.g.
     * pressing "Login" or "Auswählen" confirms; closing the window with "X"
     * does not confirm.)
     */
    protected boolean userConfirmed = false;

    /**
     * Sets the stage the conroller can refer to. (E.g. for closing the stage)
     *
     * @param stage The stage to refer to.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                userConfirmed = false;
            }
        });
    }

    /**
     * Throws a {@code IllegalStateException} only if stage is {@code null}.
     */
    public void checkStage() {
        if (stage == null) {
            throw new IllegalStateException(
                    "You have to call setStage(...) first");
        }
    }

    /**
     * Checks whether the user confirmed his input.
     *
     * @return {@code true} only if the user confirmed his input explicitly.
     */
    public boolean userConfirmed() {
        return userConfirmed;
    }
}
