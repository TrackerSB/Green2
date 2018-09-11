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

import com.google.errorprone.annotations.DoNotCall;
import java.util.Map;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.ObservableMap;
import javafx.util.Pair;

/**
 * Represents objects which can be added to a {@link ReportSummary} directly.
 *
 * @author Stefan Huber
 */
public interface Reportable {

    /**
     * Returns a {@link Map} from messages to their {@link ReportType} and to an expression which holds when to
     * show/hide the message. NOTE When implementing this method it has to be made sure that the
     * {@link BooleanExpression}s representing the validation are always the same when requested. Otherwise
     * {@link ReportSummary} may not be able to remove all validation associated with this {@link Reportable}.
     *
     * @return A {@link Map} from messages to their {@link ReportType} and to an expression which holds when to
     * show/hide the message.
     */
    ObservableMap<String, Pair<ReportType, BooleanExpression>> getReports();

    /**
     * Adds a further report to the list of reports. NOTE Use this method in subclasses of implementing classes only!
     * Since multiple inheritance is not possible in Java this class has to be an interface and interfaces are not
     * allowed to have protected members.
     *
     * @param message The message to display.
     * @param report The report containing its type and an expression evaluating whether to show the report.
     */
    //FIXME How to make it protected and final?
    @DoNotCall
    /* protected final */ void addReport(String message, Pair<ReportType, BooleanExpression> report);

    /**
     * Adds all reports of the given {@link Reportable} to this {@link Reportable}. NOTE Use this method in subclasses
     * of implementing classes only! Since multiple inheritance is not possible in Java this class has to be an
     * interface and interfaces are not allowed to have protected members.
     *
     * @param reportable The {@link Reportable} whose messages have to be added to this one.
     */
    //FIXME How to make it protected and final?
    @DoNotCall
    /* protected final */ default void addReports(Reportable reportable) {
        reportable.getReports()
                .entrySet()
                .stream()
                .forEach(report -> addReport(report.getKey(), report.getValue()));
    }
}
