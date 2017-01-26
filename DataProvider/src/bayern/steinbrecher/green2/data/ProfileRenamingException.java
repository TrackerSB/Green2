/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
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
     * @param cause   The cause of this exception
     */
    public ProfileRenamingException(String message, Throwable cause) {
        super(message, cause);
    }
}
