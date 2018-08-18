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

import javafx.scene.Node;

/**
 * This class represents an implementation of {@link CheckedControl} which can be used for delegation.
 *
 * @author Stefan Huber
 * @since 2u14
 * @param <C> The type of the control delegating to this class.
 */
//TODO May restrict to Control instead of Node (but ContributionField).
public class CheckedControlBase<C extends Node> extends ReadOnlyCheckedControlBase<C> implements CheckedControl {

    public CheckedControlBase(C control) {
        super(control);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChecked(boolean checked) {
        checkedProperty().set(checked);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return validProperty().get();
    }
}
