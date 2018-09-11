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

import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.report.Reportable;
import com.google.errorprone.annotations.DoNotCall;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.util.Pair;

/**
 * Represents which represents controls like buttons, checkboxes, input fields, etc which have addional properties
 * describing whether the current input is valid or if it is checked whether it is valid.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public interface CheckedControl extends Reportable {

    /**
     * Returns the {@link javafx.beans.property.BooleanProperty} representing whether the current input is valid or not.
     * It is valid if it is checked and there are no reports of errors triggered.
     *
     * @return The {@link javafx.beans.property.BooleanProperty} representing whether the current input is valid or not.
     * @see #isValid()
     * @see #checkedProperty()
     * @see #addReport(java.lang.String, javafx.util.Pair)
     */
    ReadOnlyBooleanProperty validProperty();

    /**
     * Checks whether the currently input is valid or not checked.
     *
     * @return {@code true} only if the current input is valid or it is not checked.
     * @see #isChecked()
     */
    boolean isValid();

    /**
     * Adds a report to the set of reports. If the {@link ReportType} is {@link ReportType#ERROR} the negation of the
     * {@link BooleanExpression} is added to the set of conditions for validity of the input of this control. Reports
     * are only shown if this control {@link #isChecked()}. Be careful about cyclic dependencies between the
     * {@link BooleanExpression}s of the reports. NOTE Use this method in subclasses of implementing classes only! Since
     * multiple inheritance is not possible in Java this class has to be an interface and interfaces are not allowed to
     * have protected members.
     *
     * @param message The message to display.
     * @param report The report containing its type and an expression evaluating whether to show the report.
     */
    @DoNotCall
    @Override
    void addReport(String message, Pair<ReportType, BooleanExpression> report);

    /**
     * Represents whether the input is checked or not.
     *
     * @return The property representing whether the input is checked or not.
     */
    ReadOnlyBooleanProperty checkedProperty();

    /**
     * Checks whether the input is checked.
     *
     * @return {@code true} only if the input is checked.
     */
    boolean isChecked();
}
