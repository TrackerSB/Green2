/*
 * Copyright (C) 2018 Stefan Huber
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
package bayern.steinbrecher.green2;

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

/**
 * Represents a controller of a {@link WizardableView}.
 *
 * @author Stefan Huber
 * @param <T> The type of the result represented by this controller.
 */
public abstract class WizardableController<T extends Optional<?>> extends ResultController<T> {

    /**
     * A property indicating whether all input handled by this controller is valid.
     */
    private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid", true);

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getResult() {
        if (hasUserAbborted() || !isValid()) {
            return (T) Optional.empty();
        } else {
            return calculateResult();
        }
    }

    /**
     * Returns the property containing whether the current input of this controller is valid.
     *
     * @return The property containing whether the current input of this controller is valid.
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

    /**
     * Binds the {@link #validProperty()} to the given observable value.
     *
     * @param binding The value to bind {@link #validProperty()} to.
     */
    protected void bindValidProperty(ObservableValue<? extends Boolean> binding) {
        valid.bind(binding);
    }
}
