/*
 * Copyright (C) 2018 Steinbrecher
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
package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.report.ReportSummary;
import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.spinner.CheckedSpinner;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import javafx.beans.binding.BooleanExpression;

/**
 * Contains on the one hand short hand methods for adding entries to {@link ReportSummary} and on the other a way for
 * chaining such addtions.
 *
 * @author Stefan Huber
 */
public final class ReportSummaryBuilder {

    private final ReportSummary reportSummary;

    /**
     * Creates a new {@link ReportSummaryBuilder} which operates on the given {@link ReportSummary}.
     *
     * @param reportSummary The {@link ReportSummary} to add any entry to.
     */
    public ReportSummaryBuilder(ReportSummary reportSummary) {
        this.reportSummary = reportSummary;
    }

    /**
     * Adds a report entry.
     *
     * @param message The message of the report to increase its counter to add.
     * @param type The type the new report has or the existing report has to be set to.
     * @param validation The initial expression checked whether the message occurrs.
     * @see ReportSummary#addReportEntry(java.lang.String, bayern.steinbrecher.green2.elements.report.ReportType,
     * javafx.beans.binding.BooleanExpression)
     * @return The biulder itself to allow chaining.
     */
    public ReportSummaryBuilder addReportEntry(String message, ReportType type, BooleanExpression validation) {
        reportSummary.addReportEntry(message, type, validation);
        return this;
    }

    /**
     * Adds a report entry showing a message stating missing input when {@code validation} returns {@code true}.
     *
     * @param validation The expression stating whether input is missing.
     * @return The biulder itself to allow chaining.
     */
    public ReportSummaryBuilder addInputMissingReportEntry(BooleanExpression validation) {
        reportSummary.addReportEntry(EnvironmentHandler.getResourceValue("inputMissing"), ReportType.ERROR, validation);
        return this;
    }

    /**
     * Adds a report entry showing a message stating invalid input when {@code validation} returns {@code true}.
     *
     * @param validation The expression stating whether input is invalid.
     * @return The biulder itself to allow chaining.
     */
    public ReportSummaryBuilder addInputInvalidReportEntry(BooleanExpression validation) {
        reportSummary.addReportEntry(EnvironmentHandler.getResourceValue("inputInvalid"), ReportType.ERROR, validation);
        return this;
    }

    /**
     * Adds a report entry showing a message stating to long input when {@code validation} returns {@code true}.
     *
     * @param validation The expression stating whether input is to long.
     * @return The biulder itself to allow chaining.
     */
    public ReportSummaryBuilder addInputToLongReportEntry(BooleanExpression validation) {
        reportSummary.addReportEntry(EnvironmentHandler.getResourceValue("inputToLong"), ReportType.ERROR, validation);
        return this;
    }

    /**
     * Adds multiple entries representing various properties of a {@link CheckedSpinner}.
     *
     * @param spinner The spinner to add entries for.
     * @return The biulder itself to allow chaining.
     */
    public ReportSummaryBuilder addEntries(CheckedSpinner<?> spinner) {
        addInputInvalidReportEntry(spinner.validProperty().not());
        addInputMissingReportEntry(spinner.valueProperty().isNull());
        return this;
    }

    /**
     * Adds multiple entries representing various properties of a {@link CheckedTextField}.
     *
     * @param textField The {@link CheckedTextField} to add entries for.
     * @return The biulder itself to allow chaining.
     */
    public ReportSummaryBuilder addEntries(CheckedTextField textField) {
        addInputMissingReportEntry(textField.emptyProperty().and(textField.checkedProperty()));
        addInputToLongReportEntry(textField.toLongProperty().and(textField.checkedProperty()));
        addInputInvalidReportEntry(textField.validProperty().not());
        return this;
    }
}
