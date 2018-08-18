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

import javafx.beans.binding.BooleanExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.util.Pair;

/**
 * Represents a base implementation of {@link Reportable} which may be used for delegation.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ReportableBase implements Reportable {

    private final ObservableMap<String, Pair<ReportType, BooleanExpression>> reports
            = FXCollections.observableHashMap();

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservableMap<String, Pair<ReportType, BooleanExpression>> getReports() {
        return reports;
    }

    /**
     * Adds a further report to the list of reports. NOTE Use this method in subclasses of implementing classes only!
     * Since multiple inheritance is not possible in Java this class has to be an interface and interfaces are not
     * allowed to have protected members.
     *
     * @param message The message to display.
     * @param report The report containing its type and an expression evaluating whether to show the report.
     */
    @Override
    public void addReport(String message, Pair<ReportType, BooleanExpression> report) {
        if (reports.containsKey(message)) {
            throw new IllegalArgumentException("A report for \"" + message + "\" is already registered.");
        } else {
            reports.put(message, report);
        }
    }
}
