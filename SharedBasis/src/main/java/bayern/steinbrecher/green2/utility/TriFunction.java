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
package bayern.steinbrecher.green2.utility;

/**
 * Represents a {@link java.util.function.BiFunction} but accepting three different input parameter.
 *
 * @author Stefan Huber
 * @param <T> The type of the first argument.
 * @param <U> The type of the second argument.
 * @param <V> The type of the third argument.
 * @param <R> The type of the output.
 * @since 2u14
 * @see java.util.function.BiFunction
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    /**
     * Performs the given operation on the passed arguments.
     *
     * @param t The first input argument.
     * @param u The second input argument.
     * @param v The third input argument.
     * @return The resulting object.
     */
    R accept(T t, U u, V v);
}
