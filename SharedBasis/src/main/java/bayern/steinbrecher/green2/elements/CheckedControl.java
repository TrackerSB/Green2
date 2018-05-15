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
package bayern.steinbrecher.green2.elements;

import javafx.beans.property.BooleanProperty;

/**
 * Represents which represents controls like buttons, checkboxes, input fields, etc which have addional properties
 * describing whether the current input is valid or if it is checked whether it is valid. In contrast to
 * {@link ReadOnlyCheckedControl} the checked property can be changed.
 *
 * @see ReadOnlyCheckedControl
 *
 * @author Stefan Huber
 */
public interface CheckedControl extends ReadOnlyCheckedControl {

    /**
     * {@inheritDoc}
     */
    @Override
    BooleanProperty checkedProperty();

    /**
     * Sets whether to check the input or not.
     *
     * @param checked {@code true} only if the input has to be checked.
     */
    void setChecked(boolean checked);
}
