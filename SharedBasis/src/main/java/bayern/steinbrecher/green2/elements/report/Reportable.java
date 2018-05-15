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

import java.util.Map;
import javafx.beans.binding.BooleanExpression;
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
    Map<String, Pair<ReportType, BooleanExpression>> getReports();
}
