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

package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.utility.ElementsUtility;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a DatePicker which sets a css class attribute when it is empty or
 * an invalid date is inserted.
 *
 * @author Stefan Huber
 */
public class CheckedDatePicker extends DatePicker {

    private static final DateTimeFormatter DATE_TIME_FORMAT_SHORT
            = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    private static final DateTimeFormatter DATE_TIME_FORMAT_MEDIUM
            = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    /**
     * BooleanProperty indicating whether the currently inserted date is valid.
     */
    private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid");
    private final BooleanProperty empty = new SimpleBooleanProperty(this, "empty");
    private final BooleanProperty forceFuture = new SimpleBooleanProperty(this, "forceFuture", false);
    private final BooleanProperty invalidPastDate = new SimpleBooleanProperty(this, "invalidPastDate");

    /**
     * Constructs {@code CheckedDatePicker} without initial date and
     * {@code forceFuture} set to {@code false}.
     */
    public CheckedDatePicker() {
        this(false);
    }

    /**
     * Constructs a {@code CheckedDatePicker} with no initial date inserted.
     *
     * @param forceFuture {@code true} indicates a date can only be valid if it
     *                    is not in the past and not today.
     */
    public CheckedDatePicker(boolean forceFuture) {
        this(null, forceFuture);
    }

    /**
     * Constructs a {@code CheckedDatePicker} with {@code locale} as initial
     * date.
     *
     * @param locale      The initial date.
     * @param forceFuture {@code true} indicates a date can only be valid if it
     *                    is not in the past and not today.
     */
    public CheckedDatePicker(LocalDate locale, boolean forceFuture) {
        super(locale);
        this.forceFuture.set(forceFuture);

        empty.bind(getEditor().textProperty().isEmpty());

        ObjectBinding<LocalDate> executionDateBinding = Bindings.createObjectBinding(() -> {
            String dateToParse = getEditor().textProperty().get();
            LocalDate newDate = null;

            String[] dateParts = dateToParse.split("\\.");
            if (dateParts.length == 3) {
                dateParts[0] = (dateParts[0].length() < 2 ? "0" : "") + dateParts[0];
                dateParts[1] = (dateParts[1].length() < 2 ? "0" : "") + dateParts[1];
                dateToParse = Arrays.stream(dateParts).collect(Collectors.joining("."));
            }
            try {
                newDate = LocalDate.parse(dateToParse, DATE_TIME_FORMAT_SHORT);
            } catch (DateTimeParseException ex) {
                //FIXME Try not to use DateTimeParseException for control flow
                try {
                    newDate = LocalDate.parse(dateToParse, DATE_TIME_FORMAT_MEDIUM);
                } catch (DateTimeParseException ignored) {
                }
            }
            return newDate;
        }, getEditor().textProperty());

        BooleanBinding executionDateInFuture = Bindings.createBooleanBinding(() -> {
            LocalDate executionDate = executionDateBinding.get();
            return executionDate != null && executionDate.isAfter(LocalDate.now());
        }, executionDateBinding);

        valid.bind(executionDateBinding.isNotNull().and(invalidPastDate.not()).and(empty.not()));
        invalidPastDate.bind(this.forceFuture.and(executionDateInFuture.not()));

        ElementsUtility.addCssClassIf(this, valid.not(), ElementsUtility.CSS_CLASS_INVALID_CONTENT);
    }

    /**
     * Returns the {@code BooleanProperty} representing whether the current
     * value is valid or not.
     *
     * @return The {@code BooleanProperty} representing whether the current
     * value is valid or not.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether the current value is a valid date.
     *
     * @return {@code true} only if the current value is a valid date.
     */
    public boolean isValid() {
        return valid.get();
    }

    /**
     * Represents a boolean value indicating whether the textfield is empty or
     * not.
     *
     * @return The property represents a boolean value indicating whether the
     * textfield is empty or not.
     */
    public ReadOnlyBooleanProperty emptyProperty() {
        return empty;
    }

    /**
     * Checks whether the textfield is empty.
     *
     * @return {@code true} only if the textfield is empty.
     */
    public boolean isEmpty() {
        return empty.get();
    }

    /**
     * Returns the property indicating whether the date picker checks whether
     * the inserted date si not in the past and not today.
     *
     * @return The property indicating whether the date picker checks whether
     * the inserted date si not in the past and not today.
     */
    public BooleanProperty forceFutureProperty() {
        return forceFuture;
    }

    /**
     * Indicates whether the date picker checks whether the inserted date si not
     * in the past and not today.
     *
     * @return {@code true} only if the inserted date has to be in the future to
     * be valid.
     */
    public boolean isForceFuture() {
        return forceFuture.get();
    }

    /**
     * Sets {@code forceFuture} which indicates whether the inserted date has to
     * be in the future to be valid.
     *
     * @param forceFuture {@code true} only if the inserted date has to be in
     *                    the future to be valid.
     */
    public void setForceFuture(boolean forceFuture) {
        this.forceFuture.set(forceFuture);
    }

    /**
     * Returns the property which saves {@code false} if {@code forceFuture}
     * saves {@code true} but the date is not in the future. It saves
     * {@code false} otherwise.
     *
     * @return The property which saves {@code false} if {@code forceFuture}
     * saves {@code true} but the date is not in the future. It saves
     * {@code false} otherwise.
     */
    public ReadOnlyBooleanProperty invalidPastDateProperty() {
        return invalidPastDate;
    }

    /**
     * Returns {@code false} if {@code forceFuture} saves {@code true} but the
     * date is not in the future. It returns {@code false} otherwise.
     *
     * @return {@code false} only if {@code forceFuture} saves {@code true} but
     * the date is not in the future.
     */
    public boolean isInvalidPastDate() {
        return invalidPastDate.get();
    }
}
