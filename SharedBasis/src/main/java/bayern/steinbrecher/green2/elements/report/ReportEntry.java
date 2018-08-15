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
package bayern.steinbrecher.green2.elements.report;

import bayern.steinbrecher.green2.utility.BindingUtility;
import java.util.Objects;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;

/**
 * Represents an entry in a {@link ReportSummary}. Every entry has a type and a message and is associated with one or
 * multiple validations. The number of occurrences describes the number of validations evaluating to {@code true}. When
 * the number of occurrences is greater 0 the message may be shown by a {@link ReportSummary}.
 *
 * @author Stefan Huber
 * @since 2u14
 */
final class ReportEntry {

    private final StringProperty message = new SimpleStringProperty(this, "message");
    private final ObjectProperty<ReportType> reportType = new SimpleObjectProperty<>(this, "reportType");
    private final ReadOnlyIntegerWrapper occurrences = new ReadOnlyIntegerWrapper(this, "occurrences");
    private final ListProperty<BooleanExpression> reportValidations
            = new SimpleListProperty<>(this, "reportValidations", FXCollections.observableArrayList());

    /**
     * Creates a {@link ReportEntry} of a certain type and showing a given message.
     *
     * @param message The message the entry has to show.
     * @param type The type of the message.
     */
    ReportEntry(String message, ReportType type) {
        setMessage(message);
        setReportType(type);
        reportValidations.addListener((obs, oldVal, newVal) -> {
            occurrences.bind(BindingUtility.reduceSum(
                    newVal.stream()
                            .map(exp -> {
                                SimpleIntegerProperty mayOccur = new SimpleIntegerProperty();
                                ChangeListener<? super Boolean> listener = (obsInner, oldValInner, newValInner) -> {
                                    mayOccur.set(newValInner ? 1 : 0);
                                };
                                exp.addListener(listener);
                                return mayOccur;
                            })));
        });
    }

    /**
     * Return the property holding the message the {@link ReportEntry} represents.
     *
     * @return The property holding the message the {@link ReportEntry} represents.
     */
    public StringProperty messageProperty() {
        return message;
    }

    /**
     * Returns the message describing the entry.
     *
     * @return The message describing the entry.
     */
    public String getMessage() {
        return messageProperty().get();
    }

    /**
     * Sets the message describing the entry.
     *
     * @param message The message describing the entry.
     */
    public void setMessage(String message) {
        messageProperty().set(message);
    }

    /**
     * Returns the property holding the type of the entry.
     *
     * @return The property holding the type of the entry.
     */
    public ObjectProperty<ReportType> reportTypeProperty() {
        return reportType;
    }

    /**
     * Returns the type of the report entry.
     *
     * @return The type of the report entry.
     */
    public ReportType getReportType() {
        return reportTypeProperty().get();
    }

    /**
     * Changes the type of the entry.
     *
     * @param reportType The new type of the entry.
     */
    public void setReportType(ReportType reportType) {
        reportTypeProperty().set(reportType);
    }

    /**
     * Returns the property holding the number of validations evaluating to {@code true}.
     *
     * @return The property holding the number of validations evaluating to {@code true}.
     */
    public ReadOnlyIntegerProperty occurrencesProperty() {
        return occurrences.getReadOnlyProperty();
    }

    /**
     * Returns the number of validations evaluating to {@code true}.
     *
     * @return The number of occurences validations evaluating to {@code true}.
     */
    public int getOccurrences() {
        return occurrencesProperty().get();
    }

    /**
     * Returns the property holding the number of associated validations.
     *
     * @return The property holding the number of associated validations.
     */
    public ReadOnlyIntegerProperty reportValidationsSizeProperty() {
        return reportValidations.sizeProperty();
    }

    /**
     * Associates another validation with this entry.
     *
     * @param validation The validation to associate this entry with.
     * @return {@code true} only if the validation was added.
     * @see ListProperty#add(java.lang.Object)
     */
    public boolean addReportValidation(BooleanExpression validation) {
        Objects.requireNonNull(validation, "A validation is not allowed to be null.");
        return reportValidations.add(validation);
    }

    /**
     * Removes the given validation from the associated validations.
     *
     * @param validation The validation to unassociate.
     * @return {@code true} only if the validation was removed.
     * @see ListProperty#remove(java.lang.Object)
     */
    public boolean removeReportValidation(BooleanExpression validation) {
        return reportValidations.remove(validation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        boolean areEqual;
        if (obj instanceof ReportEntry) {
            ReportEntry entry = (ReportEntry) obj;
            areEqual = getMessage().equalsIgnoreCase(entry.getMessage());
        } else {
            areEqual = false;
        }
        return areEqual;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        //NOTE This is the default implementation of NetBeans
        //CHECKSTYLE.OFF: MagicNumber - This is the default implementation of NetBeans
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.message);
        hash = 97 * hash + Objects.hashCode(this.reportType);
        hash = 97 * hash + Objects.hashCode(this.occurrences);
        //CHECKSTYLE.ON: MagicNumber
        return hash;
    }
}
