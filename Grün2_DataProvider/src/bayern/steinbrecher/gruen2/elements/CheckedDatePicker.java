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
package bayern.steinbrecher.gruen2.elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.DatePicker;

/**
 * Represents a DatePicker which sets a css class attribute when it is empty or
 * an invalid date is inserted.
 *
 * @author Stefan Huber
 */
public class CheckedDatePicker extends DatePicker {

    /**
     * The css class added when the inserted date is no valid date.
     */
    public static final String CSS_CLASS_INVALID_DATE = "invalidDate";
    private static final DateTimeFormatter DATE_TIME_FORMAT
            = DateTimeFormatter.ofPattern("d.M.yyyy", Locale.GERMANY);
    /**
     * BooleanProperty indicating whether the currently inserted date is valid.
     */
    private final BooleanProperty valid
            = new SimpleBooleanProperty(this, "valid");
    private final BooleanProperty empty
            = new SimpleBooleanProperty(this, "empty");
    private final BooleanProperty forceFuture
            = new SimpleBooleanProperty(this, "forceFuture", false);
    private final BooleanProperty invalidPastDate
            = new SimpleBooleanProperty(this, "invalidPastDate");

    /**
     * Constructes {@code CheckedDatePicker} without initial date and
     * {@code forceFuture} set to {@code false}.
     */
    public CheckedDatePicker() {
        this(false);
    }

    /**
     * Constructes a {@code CheckedDatePicker} with no initial date inserted.
     *
     * @param forceFuture {@code true} indicates a date can only be valid if it
     * is not in the past and not today.
     */
    public CheckedDatePicker(boolean forceFuture) {
        this(null, forceFuture);
    }

    /**
     * Constructes a {@code CheckedDatePicker} with {@code locale} as initial
     * date.
     *
     * @param locale The initial date.
     * @param forceFuture {@code true} indicates a date can only be valid if it
     * is not in the past and not today.
     */
    public CheckedDatePicker(LocalDate locale, boolean forceFuture) {
        super(locale);
        this.forceFuture.set(forceFuture);

        empty.bind(getEditor().textProperty().isEmpty());

        valid.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                getStyleClass().remove(CSS_CLASS_INVALID_DATE);
            } else if (!getStyleClass().contains(CSS_CLASS_INVALID_DATE)) {
                getStyleClass().add(CSS_CLASS_INVALID_DATE);
            }
        });

        //Initiate ChangeListener
        valid.set(false); //FIXME Find a workaround
        valid.set(true);

        getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                LocalDate newDate = LocalDate.parse(newVal, DATE_TIME_FORMAT);
                valid.set(!this.forceFuture.get() || (this.forceFuture.get()
                        && newDate.isAfter(LocalDate.now())));
            } catch (DateTimeParseException ex) {
                valid.set(false);
            }
        });

        invalidPastDate.bind(this.forceFuture.not().or(valid));

        //Initiate ChangeListener
        String oldText = getEditor().getText();
        getEditor().setText(oldText + " extended"); //FIXME Find a workaround
        getEditor().setText(oldText);
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
     * the future to be valid.
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
