/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.exception;

/**
 * Indicating that a database scheme could not be created.
 *
 * @author Stefan Huber
 */
public class SchemeCreationException extends RuntimeException {

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
     * geben kann.
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
     * geben kann.
     * @param cause Der Grund weswegen der Fehler ausgelöst wurde.
     */
    public SchemeCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
