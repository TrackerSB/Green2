/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.exception;

/**
 * Indicating that a try to authenticate somewhere failed.
 *
 * @author Stefan Huber
 */
public class AuthException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Erstellt eine {@code AuthException}, welche weder eine Detalinachricht
     * noch einen Grund für ihr Auftreten enthält.
     */
    public AuthException() {
        super();
    }

    /**
     * Erstellt eine {@code AuthException}, welche die Datailnachricht
     * {@code message} enthält, jedoch keinen Grund für ihr Auftreten.
     *
     * @param message Die Detailnachricht, die weitere Informationen zum Fehler
     *                geben kann.
     */
    public AuthException(String message) {
        super(message);
    }

    /**
     * Erstellt eine {@code AuthException} mit gegebener Detailnachricht
     * {@code message} und dem Grund {@code cause} für ihr Auftreten.
     *
     * @param message Die Detailnachricht, die weitere Informationen zum Fehler
     *                geben kann.
     * @param cause   Der Grund weswegen der Fehler ausgelöst wurde.
     */
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
