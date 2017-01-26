/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.exception;

/**
 * Indicating that a database scheme could not be created.
 *
 * @author Stefan Huber
 */
public class SchemeCreationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Erstellt eine {@code SchemeCreationException}, welche weder eine
     * Detalinachricht noch einen Grund für ihr Auftreten enthält.
     */
    public SchemeCreationException() {
        super();
    }

    /**
     * Erstellt eine {@code SchemeCreationException}, welche die Datailnachricht
     * {@code message} enthält, jedoch keinen Grund für ihr Auftreten.
     *
     * @param message Die Detailnachricht, die weitere Informationen zum Fehler
     *                geben kann.
     */
    public SchemeCreationException(String message) {
        super(message);
    }

    /**
     * Erstellt eine {@code SchemeCreationException} mit gegebener
     * Detailnachricht {@code message} und dem Grund {@code cause} für ihr
     * Auftreten.
     *
     * @param message Die Detailnachricht, die weitere Informationen zum Fehler
     *                geben kann.
     * @param cause   Der Grund weswegen der Fehler ausgelöst wurde.
     */
    public SchemeCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
