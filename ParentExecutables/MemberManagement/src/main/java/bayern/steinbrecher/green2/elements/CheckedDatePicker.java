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
package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.report.Reportable;
import bayern.steinbrecher.green2.utility.ElementsUtility;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.DatePicker;
import javafx.util.Pair;

/**
 * Represents a DatePicker which sets a css class attribute when it is empty or an invalid date is inserted.
 *
 * @author Stefan Huber
 */
public class CheckedDatePicker extends DatePicker implements CheckedControl, Reportable {

    /**
     * The CSS class associated with this class.
     */
    public static final String CSS_CLASS_CHECKED_DATE_PICKER = "checked-date-picker";
    private static final DateTimeFormatter DATE_TIME_FORMAT_SHORT
            = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    private static final DateTimeFormatter DATE_TIME_FORMAT_MEDIUM
            = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    /**
     * BooleanProperty indicating whether the currently inserted date is valid.
     */
    private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid");
    private final BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid");
    /**
     * Holds {@code true} only if the content has to be checked.
     */
    private final BooleanProperty checked = new SimpleBooleanProperty(this, "checked", true);
    private final BooleanProperty empty = new SimpleBooleanProperty(this, "empty");
    private final BooleanProperty forceFuture = new SimpleBooleanProperty(this, "forceFuture", false);
    private final BooleanProperty invalidPastDate = new SimpleBooleanProperty(this, "invalidPastDate");
    private final Map<String, Pair<ReportType, BooleanExpression>> reports
            = Map.of(EnvironmentHandler.getResourceValue("pastExecutionDate"),
                    new Pair<>(ReportType.ERROR, invalidPastDateProperty()),
                    EnvironmentHandler.getResourceValue("inputMissing"),
                    new Pair<>(ReportType.ERROR, checkedProperty().and(emptyProperty())),
                    EnvironmentHandler.getResourceValue("inputInvalid"), new Pair<>(ReportType.ERROR, invalid));

    /**
     * Constructs {@link CheckedDatePicker} without initial date and {@code forceFuture} set to {@code false}.
     */
    public CheckedDatePicker() {
        this(false);
    }

    /**
     * Constructs a {@link CheckedDatePicker} with no initial date inserted.
     *
     * @param forceFuture {@code true} indicates a date can only be valid if it is not in the past and not today.
     */
    public CheckedDatePicker(boolean forceFuture) {
        this(null, forceFuture);
    }

    /**
     * Constructs a {@link CheckedDatePicker} with {@code locale} as initial date.
     *
     * @param locale The initial date.
     * @param forceFuture {@code true} indicates a date can only be valid if it is not in the past and not today.
     */
    public CheckedDatePicker(LocalDate locale, boolean forceFuture) {
        super(locale);
        this.forceFuture.set(forceFuture);
        initProperties();

        getStyleClass().add(CSS_CLASS_CHECKED_DATE_PICKER);
        getStylesheets().add(CheckedDatePicker.class.getResource("checkedDatePicker.css").toExternalForm());
    }

    private void initProperties() {
        empty.bind(getEditor().textProperty().isEmpty());

        ObjectBinding<LocalDate> executionDateBinding = Bindings.createObjectBinding(() -> {
            String dateToParse = getEditor().textProperty().get();
            LocalDate newDate = null;

            String[] dateParts = dateToParse.split("\\.");
            //CHECKSTYLE.OFF: MagicNumber - Every date consists of 3 elements (year, month, day)
            if (dateParts.length == 3) {
                //CHECKSTYLE.ON: MagicNumber
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

        valid.bind((executionDateBinding.isNotNull().and(invalidPastDate.not()).and(empty.not())).or(checked.not()));
        invalid.bind(valid.not());
        invalidPastDate.bind(this.forceFuture.and(executionDateInFuture.not()));

        ElementsUtility.addCssClassIf(this, invalid, ElementsUtility.CSS_CLASS_INVALID_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Pair<ReportType, BooleanExpression>> getReports() {
        return reports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanProperty checkedProperty() {
        return checked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChecked() {
        return checked.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChecked(boolean checked) {
        this.checked.set(checked);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return valid.get();
    }

    /**
     * Represents a boolean value indicating whether the textfield is empty or not.
     *
     * @return The property represents a boolean value indicating whether the textfield is empty or not.
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
     * Returns the property indicating whether the date picker checks whether the inserted date si not in the past and
     * not today.
     *
     * @return The property indicating whether the date picker checks whether the inserted date si not in the past and
     * not today.
     */
    public BooleanProperty forceFutureProperty() {
        return forceFuture;
    }

    /**
     * Indicates whether the date picker checks whether the inserted date si not in the past and not today.
     *
     * @return {@code true} only if the inserted date has to be in the future to be valid.
     */
    public boolean isForceFuture() {
        return forceFuture.get();
    }

    /**
     * Sets {@code forceFuture} which indicates whether the inserted date has to be in the future to be valid.
     *
     * @param forceFuture {@code true} only if the inserted date has to be in the future to be valid.
     */
    public void setForceFuture(boolean forceFuture) {
        this.forceFuture.set(forceFuture);
    }

    /**
     * Returns the property which saves {@code false} if {@code forceFuture} saves {@code true} but the date is not in
     * the future. It saves {@code false} otherwise.
     *
     * @return The property which saves {@code false} if {@code forceFuture} saves {@code true} but the date is not in
     * the future. It saves {@code false} otherwise.
     */
    public ReadOnlyBooleanProperty invalidPastDateProperty() {
        return invalidPastDate;
    }

    /**
     * Returns {@code false} if {@code forceFuture} saves {@code true} but the date is not in the future. It returns
     * {@code false} otherwise.
     *
     * @return {@code false} only if {@code forceFuture} saves {@code true} but the date is not in the future.
     */
    public boolean isInvalidPastDate() {
        return invalidPastDate.get();
    }
}
