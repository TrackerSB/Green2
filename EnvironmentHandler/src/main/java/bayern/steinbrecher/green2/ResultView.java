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

/**
 * Represents a {@link View} which represents a certain result.
 *
 * @author Stefan Huber
 * @param <T> The type of the result.
 * @param <C> The type of the controller associated with this view.
 */
public abstract class ResultView<T extends Optional<?>, C extends ResultController<T>> extends View<C> {

    /**
     * Returns the result currently represented by the associated {@link ResultController}.
     *
     * @return The result currently represented by the associated {@link ResultController}.
     * @see ResultController#getResult()
     */
    public T getResult() {
        return getController().getResult();
    }
}
