/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package bayern.steinbrecher.green2;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * This controller extends {@code Controller} with the properties
 * {@code anyMissingInput}, {@code anyInputToLong} and {@code valid}.
 *
 * @author Stefan Huber
 */
public abstract class CheckedController extends WizardableController {

    /**
     * Used as identity for sequence of or bindings connected with OR.
     */
    protected static final BooleanBinding FALSE_BINDING = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return false;
        }
    };
    /**
     * Used as identity for sequence of or bindings connected with AND.
     */
    protected static final BooleanBinding TRUE_BINDING = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return true;
        }
    };
    protected final BooleanProperty anyInputToLong
            = new SimpleBooleanProperty(this, "anyInputToLong");
    protected final BooleanProperty anyInputMissing
            = new SimpleBooleanProperty(this, "anyInputMissing");

    /**
     * Returns the property reprsenting a boolean value indicating whether any
     * input is to long.
     *
     * @return The property reprsenting a boolean value indicating whether any
     * input is to long.
     */
    public ReadOnlyBooleanProperty anyInputToLongProperty() {
        return anyInputToLong;
    }

    /**
     * Checks whether any input is to long.
     *
     * @return {@code true} only if any input is to long.
     */
    public boolean isAnyInputToLong() {
        return anyInputToLong.get();
    }

    /**
     * Returns the property reprsenting a boolean value indicating whether any
     * input is missing.
     *
     * @return The property reprsenting a boolean value indicating whether any
     * input is missing.
     */
    public ReadOnlyBooleanProperty anyInputMissingProperty() {
        return anyInputMissing;
    }

    /**
     * Checks whether any input is missing.
     *
     * @return {@code true} only if any input is missing.
     */
    public boolean isAnyInputMissing() {
        return anyInputMissing.get();
    }

    /**
     * Returns the property reprsenting a boolean value indicating whether all
     * input is valid.
     *
     * @return The property reprsenting a boolean value indicating whether all
     * input is valid.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether all inserted data is valid.
     *
     * @return {@code true} only if all inserted data is valid.
     */
    public boolean isValid() {
        return valid.get();
    }
}
