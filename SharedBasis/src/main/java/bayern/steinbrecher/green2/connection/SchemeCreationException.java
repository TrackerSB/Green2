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
package bayern.steinbrecher.green2.connection;

/**
 * Indicating that a database scheme could not be created.
 *
 * @author Stefan Huber
 */
public class SchemeCreationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with {@code null} as its detail message and no cause.
     */
    public SchemeCreationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message and no cause.
     *
     * @param message The detail message.
     */
    public SchemeCreationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message.
     * @param cause The cause. (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or
     * unknown.)
     */
    public SchemeCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
