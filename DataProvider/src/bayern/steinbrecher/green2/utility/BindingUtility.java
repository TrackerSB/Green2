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

package bayern.steinbrecher.green2.utility;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ObservableBooleanValue;

import java.util.stream.Stream;

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

    public static ObservableBooleanValue reduceOr(Stream<Boolean> observableValues){
        return observableValues.reduce(FALSE_BINDING, BooleanExpression::or);
    }
}
