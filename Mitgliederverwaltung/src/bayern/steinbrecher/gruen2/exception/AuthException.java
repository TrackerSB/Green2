/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.exception;

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
     * geben kann.
     */
    public AuthException(String message) {
        super(message);
    }

    /**
     * Erstellt eine {@code AuthException} mit gegebener Detailnachricht
     * {@code message} und dem Grund {@code cause} für ihr Auftreten.
     *
     * @param message Die Detailnachricht, die weitere Informationen zum Fehler
     * geben kann.
     * @param cause Der Grund weswegen der Fehler ausgelöst wurde.
     */
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
