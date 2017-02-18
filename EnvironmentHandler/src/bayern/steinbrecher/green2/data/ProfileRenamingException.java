/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.data;

/**
 * Signals exceptions when renaming a profile or files according to it like originator settings.
 *
 * @author Stefan Huber
 */
public class ProfileRenamingException extends RuntimeException {

    /**
     * Creates an exception without detail message and no cause.
     */
    public ProfileRenamingException() {
        super();
    }

    /**
     * Creates an exception with detail message but without cause.
     *
     * @param message The detail message.
     */
    public ProfileRenamingException(String message) {
        super();
    }

    /**
     * Creates an exception with detail message and cause.
     *
     * @param message The detail message.
     * @param cause The cause of this exception
     */
    public ProfileRenamingException(String message, Throwable cause) {
        super(message, cause);
    }
}
