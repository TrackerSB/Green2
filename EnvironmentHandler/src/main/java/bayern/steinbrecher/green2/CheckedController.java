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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * This controller extends {@link WizardableController} with the properties {@code anyMissingInput} and
 * {@code anyInputToLong}.
 *
 * @author Stefan Huber
 */
public abstract class CheckedController extends WizardableController {

    /**
     * A property indicating whether any input handled by the controller is to long.
     */
    protected final BooleanProperty anyInputToLong = new SimpleBooleanProperty(this, "anyInputToLong");
    /**
     * A property indiciating whether any input handled by the controller is missing.
     */
    protected final BooleanProperty anyInputMissing = new SimpleBooleanProperty(this, "anyInputMissing");

    /**
     * Returns the property representing a boolean value indicating whether any input is to long.
     *
     * @return The property representing a boolean value indicating whether any input is to long.
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
     * Returns the property representing a boolean value indicating whether any input is missing.
     *
     * @return The property representing a boolean value indicating whether any input is missing.
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
}
