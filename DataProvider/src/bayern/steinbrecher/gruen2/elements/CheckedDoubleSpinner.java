/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.elements;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Represents a spinner for double values which sets a css attribute when the
 * inserted value is not valid.
 *
 * @author Stefan Huber
 */
public class CheckedDoubleSpinner extends Spinner<Double> {

    /**
     * Css class attribute indicating that the current value of a
     * {@code CheckedDoubleSpinner} is not valid.
     */
    public static final String CSS_CLASS_INVALID = "invalidSpinnerValue";
    /**
     * {@code BooleanProperty} indicating whether the current value is valid.
     */
    private BooleanProperty validProperty
            = new SimpleBooleanProperty(this, "valid", true);

    /**
     * Constructes a new {@code CheckedDoubleSpinner}.
     *
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @param initialValue The value of the Spinner when first instantiated,
     * must be within the bounds of the min and max arguments, or else the min
     * value will be used.
     * @param amountToStepBy The amount to increment or decrement by, per step.
     */
    public CheckedDoubleSpinner(@NamedArg("min") double min,
            @NamedArg("max") double max,
            @NamedArg("initialValue") double initialValue,
            @NamedArg("amountToStepBy") double amountToStepBy) {
        super(min, max, initialValue, amountToStepBy);

        SpinnerValueFactory.DoubleSpinnerValueFactory factory
                = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        min, max, initialValue, amountToStepBy);
        setValueFactory(factory);
        getEditor().textProperty()
                .addListener((obs, oldVal, newVal) -> {
                    try {
                        double parsed = Double.parseDouble(
                                newVal.replace(',', '.'));
                        factory.setValue(parsed);
                        validProperty.set(true);
                    } catch (NumberFormatException ex) {
                        validProperty.set(false);
                    }

                    if (!validProperty.get()) {
                        if (!getStyleClass().contains(CSS_CLASS_INVALID)) {
                            getStyleClass().add(CSS_CLASS_INVALID);
                        }
                    } else {
                        getStyleClass().remove(CSS_CLASS_INVALID);
                    }
                });
    }

    /**
     * Returns the {@code BooleanProperty} representing whether the current
     * value is valid or not.
     *
     * @return The {@code BooleanProperty} representing whether the current
     * value is valid or not.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return validProperty;
    }

    /**
     * Checks whether the currently inserted value is valid.
     *
     * @return {@code true} only if the current value is valid.
     */
    public boolean isValid() {
        return validProperty.get();
    }
}
