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
package bayern.steinbrecher.green2.elements.spinner;

import bayern.steinbrecher.green2.utility.ElementsUtility;
import java.security.PrivilegedActionException;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Extends the class {@link Spinner<T>} with a valid property and sets {@link ElementsUtility#CSS_CLASS_INVALID_CONTENT}
 * if the content of the spinner is not valid.
 *
 * @author Stefan Huber
 */
public class CheckedSpinner<T> extends Spinner<T> {

    /**
     * {@link BooleanProperty} indicating whether the current value is valid.
     */
    private BooleanProperty valid = new SimpleBooleanProperty(this, "valid", true);
    private BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid");

    /**
     * Constructs a new {@code CheckedSpinner}.
     *
     * @param factory The factory generating values.
     * @param parser The function to parse the content of the Spinner. (It throws a {@link ParseException} if the value
     * could not be parsed.)
     */
    public CheckedSpinner(SpinnerValueFactory<T> factory, ParseFunction<T> parser) {
        super(factory);

        valid.bind(Bindings.createBooleanBinding(() -> {
            try {
                T parsed = parser.apply(getEditor().textProperty().get());
                factory.setValue(parsed);
                return true;
            } catch (ParseException ex) {
                return false;
            }
        }, getEditor().textProperty()));
        invalid.bind(valid.not());

        ElementsUtility.addCssClassIf(this, invalid, ElementsUtility.CSS_CLASS_INVALID_CONTENT);
    }

    /**
     * Returns the {@link BooleanProperty} representing whether the current value is valid or not.
     *
     * @return The {@link BooleanProperty} representing whether the current value is valid or not.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether the currently inserted value is valid.
     *
     * @return {@code true} only if the current value is valid.
     */
    public boolean isValid() {
        return valid.get();
    }

    /**
     * This interface is very similar to {@link Function} but it contains a method throwing a {@link ParseException}.
     */
    @FunctionalInterface
    public interface ParseFunction<T> {

        /**
         * Parses the given String to T.
         *
         * @param value The String to parse.
         * @throws ParseException Thrown only if {@code value} could not be parsed to T.
         */
        T apply(String value) throws ParseException;
    }

    /**
     * Signals that the value of a Spinner&lt;T&gt; could not be parsed to T.
     */
    public static class ParseException extends Exception {

        /**
         * Constructs a new exception with {@code null} as its detail message and no cause.
         */
        public ParseException() {
            super();
        }

        /**
         * Constructs a new exception with the specified detail message and no cause.
         *
         * @param message The detail message.
         */
        public ParseException(String message) {
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
        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new exception with the specified cause and a detail message of <tt>(cause==null ? null :
         * cause.toString())</tt> (which typically contains the class and detail message of <tt>cause</tt>). This
         * constructor is useful for exceptions that are little more than wrappers for other throwables (for example,
         * {@link PrivilegedActionException}).
         *
         * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A
         * <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
         */
        public ParseException(Throwable cause) {
            super(cause);
        }
    }
}
