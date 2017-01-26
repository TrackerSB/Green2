/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.elements;

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
public class CheckedIntegerSpinner extends Spinner<Integer> {

    /**
     * Css class attribute indicating that the current value of a
     * {@code CheckedIntegerSpinner} is not valid.
     */
    public static final String CSS_CLASS_INVALID = "invalidSpinnerValue";
    /**
     * {@code BooleanProperty} indicating whether the current value is valid.
     */
    private BooleanProperty validProperty
            = new SimpleBooleanProperty(this, "valid", true);

    /**
     * Constructes a new {@code CheckedIntegerSpinner}.
     *
     * @param min            The minimum allowed value.
     * @param max            The maximum allowed value.
     * @param initialValue   The value of the Spinner when first instantiated,
     *                       must be within the bounds of the min and max arguments, or else the min
     *                       value will be used.
     * @param amountToStepBy The amount to increment or decrement by, per step.
     */
    public CheckedIntegerSpinner(@NamedArg("min") int min,
                                 @NamedArg("max") int max,
                                 @NamedArg("initialValue") int initialValue,
                                 @NamedArg("amountToStepBy") int amountToStepBy) {
        super(min, max, initialValue, amountToStepBy);

        SpinnerValueFactory.IntegerSpinnerValueFactory factory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                min, max, initialValue, amountToStepBy);
        setValueFactory(factory);
        getEditor().textProperty()
                .addListener((obs, oldVal, newVal) -> {
                    try {
                        int parsed = Integer.parseInt(
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