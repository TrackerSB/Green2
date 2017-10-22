/* 
 * Copyright (C) 2017 Stefan Huber
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
package bayern.steinbrecher.green2.utility;

import java.util.stream.Stream;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableNumberValue;

/**
 * Contains methods for creating bindings.
 *
 * @author Stefan Huber
 */
public final class BindingUtility {

    private BindingUtility() {
        throw new UnsupportedOperationException("Construction of an objection not allowed.");
    }

    /**
     * Used as identity for sequence of sums.
     */
    public static final NumberBinding ZERO_BINDING = new IntegerBinding() {
        @Override
        protected int computeValue() {
            return 0;
        }
    };

    /**
     * Used as identity for sequence of or bindings connected with OR.
     */
    public static final BooleanBinding FALSE_BINDING = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return false;
        }
    };
    /**
     * Used as identity for sequence of or bindings connected with AND.
     */
    public static final BooleanBinding TRUE_BINDING = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return true;
        }
    };

    /**
     * Reduces the given stream summing up all its numerical values.
     *
     * @param observableValues The values to bind to a sum.
     * @return The binding representing the sum of all values within the stream.
     */
    public static NumberBinding reduceSum(Stream<? extends ObservableNumberValue> observableValues) {
        return observableValues.reduce(ZERO_BINDING, NumberBinding::add, NumberBinding::add);
    }

    /**
     * Reduces given stream concatenating the elements of the given stream using {@code or}.
     *
     * @param observableValues The stream which elements to concatenate.
     * @return The resulting {@code BooleanBinding}.
     */
    public static BooleanBinding reduceOr(Stream<? extends ObservableBooleanValue> observableValues) {
        return observableValues.reduce(FALSE_BINDING, BooleanBinding::or, BooleanBinding::or);
    }

    /**
     * Reduces given stream concatenating the elements of the given stream using {@code or}.
     *
     * @param observableValues The stream which elements to concatenate.
     * @return The resulting {@code BooleanBinding}.
     */
    public static BooleanBinding reduceAnd(Stream<? extends ObservableBooleanValue> observableValues) {
        return observableValues.reduce(TRUE_BINDING, BooleanBinding::and, BooleanBinding::and);
    }
}
