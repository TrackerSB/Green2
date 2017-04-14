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
package bayern.steinbrecher.green2.elements.spinner;

import javafx.beans.NamedArg;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Represents a spinner for double values which sets a css attribute when the inserted value is not valid.
 *
 * @author Stefan Huber
 */
public class CheckedIntegerSpinner extends CheckedSpinner<Integer> {

    private static final ParseFunction<Integer> parseFunction = value -> {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ParseException(ex);
        }
    };

    /**
     * Constructs a new {@link CheckedIntegerSpinner}.
     *
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @param initialValue The value of the Spinner when first instantiated, must be within the bounds of the min and
     * max arguments, or else the min value will be used.
     * @param amountToStepBy The amount to increment or decrement by, per step.
     */
    public CheckedIntegerSpinner(@NamedArg("min") int min,
            @NamedArg("max") int max,
            @NamedArg("initialValue") int initialValue,
            @NamedArg("amountToStepBy") int amountToStepBy) {
        super(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue, amountToStepBy),
                parseFunction);
    }
}