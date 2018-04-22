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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

/**
 * Represents an entry in a {@link ReportSummary}.
 *
 * @author Stefan Huber
 * @since 2u14
 */
final class ReportEntry {

    private final StringProperty message = new SimpleStringProperty(this, "message");
    private final ObjectProperty<ReportType> reportType = new SimpleObjectProperty<>(this, "reportType");
    private final IntegerProperty occurrences = new SimpleIntegerProperty(this, "occurrences");
    private final ListProperty<BooleanExpression> reportValidations
            = new SimpleListProperty<>(this, "reportValidations", FXCollections.observableArrayList());

    public ReportEntry(String message, ReportType type) {
        setMessage(message);
        setReportType(type);
        reportValidations.addListener((ListChangeListener.Change<? extends BooleanExpression> change) -> {
            occurrences.bind(BindingUtility.reduceSum(
                    change.getList().stream()
                            .map(exp -> {
                                SimpleIntegerProperty mayOccur = new SimpleIntegerProperty();
                                exp.addListener((obs, oldVal, newVal) -> {
                                    mayOccur.set(newVal ? 1 : 0);
                                });
                                return mayOccur;
                            })));
        });
    }

    public StringProperty messageProperty() {
        return message;
    }

    public String getMessage() {
        return messageProperty().get();
    }

    public void setMessage(String message) {
        messageProperty().set(message);
    }

    public ObjectProperty<ReportType> reportTypeProperty() {
        return reportType;
    }

    public ReportType getReportType() {
        return reportTypeProperty().get();
    }

    public void setReportType(ReportType reportType) {
        reportTypeProperty().set(reportType);
    }

    public ReadOnlyIntegerProperty occurrencesProperty() {
        return occurrences;
    }

    public int getOccurrences() {
        return occurrencesProperty().get();
    }

    public ReadOnlyIntegerProperty reportValidationsSizeProperty() {
        return reportValidations.sizeProperty();
    }

    public boolean addReportValidation(BooleanExpression validation) {
        Objects.requireNonNull(validation, "A validation is not allowed to be null.");
        return reportValidations.add(validation);
    }

    public boolean removeReportValidation(BooleanExpression validation) {
        return reportValidations.remove(validation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReportEntry) {
            ReportEntry entry = (ReportEntry) obj;
            return getMessage().equalsIgnoreCase(entry.getMessage());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //NOTE This is the default implementation of NetBeans
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.message);
        hash = 97 * hash + Objects.hashCode(this.reportType);
        hash = 97 * hash + Objects.hashCode(this.occurrences);
        return hash;
    }
}
