/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2;

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
