/*
 * Copyright (c) 2017. Stefan Huber
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
package bayern.steinbrecher.wizard;

/**
 * Signals that a wizard page having a certain key could not be found.
 *
 * @author Stefan Huber
 */
public class IllegalPageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code IllegalPageException} without detail message and no
     * cause.
     */
    public IllegalPageException() {
        super();
    }

    /**
     * Constructs an {@code IllegalPageException} with detail message but no
     * cause.
     *
     * @param message The detail message to show.
     */
    public IllegalPageException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code IllegalPageException} with given detail message and
     * cause.
     *
     * @param message The detail message to show.
     * @param cause The cause of this exception.
     */
    public IllegalPageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code IllegalPageException} without detail message but
     * with given cause.
     *
     * @param cause The cause of this exception.
     */
    public IllegalPageException(Throwable cause) {
        super(cause);
    }
}
