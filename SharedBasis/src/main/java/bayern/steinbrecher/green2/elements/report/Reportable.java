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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableList;

/**
 * Represents objects which can be added to a {@link ReportBubble} directly.
 *
 * @author Stefan Huber
 */
public interface Reportable {

    /**
     * Returns the property holding whether the current is valid. It is valid if and and only if there are no reports
     * triggered which are classified as {@link ReportType#ERROR}.
     *
     * @return The property holding whether the current is valid.
     */
    ReadOnlyBooleanProperty validProperty();

    /**
     * Checks whether the current input is valid.
     *
     * @return {@code true} only if the current input is valid.
     */
    boolean isValid();

    /**
     * Returns all associated reports.
     *
     * @return All associated reports. The list is unmodifiable.
     */
    ObservableList<ReportEntry> getReports();

    /**
     * Adds a further report to the list of reports.NOTE Use this method in subclasses of implementing classes only!
     * Since multiple inheritance is not possible in Java this class has to be an interface and interfaces are not
     * allowed to have protected members.
     *
     * @param report The report to add.
     * @return {@code true} only if the {@link ReportEntry} was added.
     * @throws IllegalArgumentException Only if the given {@link ReportEntry} has a message which was already added.
     */
    //FIXME How to make it protected and final?
    @DoNotCall
    /* protected final */ boolean addReport(ReportEntry report);

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
                .stream()
                .forEach(report -> addReport(report));
    }
}
