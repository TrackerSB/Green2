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
 * Represents a {@link Controller} having a result like {@link ResultView}.
 *
 * @author Stefan Huber
 * @param <T> The type of the result.
 */
public abstract class ResultController<T extends Optional<?>> extends Controller {

    /**
     * Returns the result this controller currently represents. NOTE The calculation of the result itself has to be done
     * over {@link #calculateResult()}.
     *
     * @return The result this controller currently represents or {@link Optional#empty()} if the user abborted the
     * input, the input is invalid, etc.
     * @see #calculateResult()
     */
    public T getResult() {
        if (userAbborted()) {
            return (T) Optional.empty();
        } else {
            return calculateResult();
        }
    }

    /**
     * Calculates the actual result if any. It does not recognize any criteria about whether to return a result or not.
     * This is handled by {@link #getResult()}.
     *
     * @return The actual result if any.
     * @see #getResult()
     */
    protected abstract T calculateResult();
}
