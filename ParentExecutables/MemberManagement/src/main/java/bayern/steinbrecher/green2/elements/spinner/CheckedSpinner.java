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
package bayern.steinbrecher.green2.elements.spinner;

import bayern.steinbrecher.green2.utility.ElementsUtility;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Extends the class {@link Spinner} with a valid property and sets {@link ElementsUtility#CSS_CLASS_INVALID_CONTENT} if
 * the content of the spinner is not valid.
 *
 * @author Stefan Huber
 * @param <T> The type of the values to spin.
 */
public class CheckedSpinner<T> extends Spinner<T> {

    /**
     * {@link BooleanProperty} indicating whether the current value is valid.
     */
    private BooleanProperty valid = new SimpleBooleanProperty(this, "valid", true);
    private BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid");

    /**
     * Constructs a new {@code CheckedSpinner}.
     *
     * @param factory The factory generating values.
     * @param parser The function to parse the content of the {@link Spinner}.
     */
    public CheckedSpinner(SpinnerValueFactory<T> factory, Function<String, Optional<T>> parser) {
        super(factory);

        valid.bind(Bindings.createBooleanBinding(() -> {
            Optional<T> parsed = parser.apply(getEditor().textProperty().get());
            parsed.ifPresent(p -> factory.setValue(p));
            return parsed.isPresent();
        }, getEditor().textProperty()));
        invalid.bind(valid.not());

        ElementsUtility.addCssClassIf(this, invalid, ElementsUtility.CSS_CLASS_INVALID_CONTENT);
    }

    /**
     * Returns the {@link BooleanProperty} representing whether the current value is valid or not.
     *
     * @return The {@link BooleanProperty} representing whether the current value is valid or not.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether the currently inserted value is valid.
     *
     * @return {@code true} only if the current value is valid.
     */
    public boolean isValid() {
        return valid.get();
    }
}
