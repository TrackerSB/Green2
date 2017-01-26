/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Represents a controller of a {@code WizardableView}.
 *
 * @author Stefan Huber
 */
public abstract class WizardableController extends Controller {

    protected BooleanProperty valid = new SimpleBooleanProperty(this, "valid");

    /**
     * Returns the property containing whether the current input of this
     * controller is valid.
     *
     * @return The property containing whether the current input of this
     * controller is valid.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether the current input is valid.
     *
     * @return {@code true} only if the current input is valid.
     */
    public boolean isValid() {
        return valid.get();
    }
}
