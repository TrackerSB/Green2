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

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

/**
 * Represents an immutable entry of a report which can be used with {@link ReportBubble}.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public final class ReportEntry {

    private final ReadOnlyStringWrapper messageKey = new ReadOnlyStringWrapper(this, "messageKey");
    private final ReadOnlyProperty<?>[] messageParams;
    private final ReadOnlyObjectWrapper<ReportType> type = new ReadOnlyObjectWrapper<>(this, "type");
    private final ReadOnlyObjectWrapper<BooleanExpression> reportTrigger
            = new ReadOnlyObjectWrapper<>(this, "reportTrigger");

    /**
     * Creates an immutable {@link ReportEntry}.
     *
     * @param messageKey The key used for retrieving the acutal message from the resource bundles.
     * @param type The type of the report.
     * @param reportTrigger The expression determining whether the report has to be shown.
     * @param messageParams The parameters passed to the resource bundle along the message key.
     */
    public ReportEntry(
            String messageKey, ReportType type, BooleanExpression reportTrigger, ReadOnlyProperty<?>... messageParams) {
        this.messageKey.set(messageKey);
        this.type.set(type);
        this.reportTrigger.set(reportTrigger);
        this.messageParams = messageParams;
    }

    /**
     * Returns the property holding the associated message key.
     *
     * @return The property holding the associated message key.
     */
    public ReadOnlyStringProperty messageKeyProperty() {
        return messageKey.getReadOnlyProperty();
    }

    /**
     * Returns the message key associated with this report.
     *
     * @return The message key associated with this report.
     * @see #getMessage()
     */
    public String getMessageKey() {
        return messageKeyProperty().get();
    }

    /**
     * Returns the retrieved message of the resource bundle after passing {@link #getMessageKey()} and the given params
     * to the resource bundle.
     *
     * @return The message of the resource bundle assocated with {@link #getMessageKey()}.
     * @see #messageKeyProperty()
     */
    public String getMessage() {
        Object[] params = Arrays.stream(messageParams)
                .map(ReadOnlyProperty::getValue)
                .collect(Collectors.toList())
                .toArray(new Object[messageParams.length]);
        return EnvironmentHandler.getResourceValue(getMessageKey(), params);
    }

    /**
     * Returns the property holding the type of this report.
     *
     * @return The property holding the type of this report.
     */
    public ReadOnlyObjectProperty<ReportType> typeProperty() {
        return type;
    }

    /**
     * Returns the type of this report.
     *
     * @return The type of this report.
     */
    public ReportType getType() {
        return typeProperty().get();
    }

    /**
     * Returns the property holding the expression determing whether this report has to be shown.
     *
     * @return The property holding the expression determing whether this report has to be shown.
     */
    public ReadOnlyObjectProperty<BooleanExpression> reportTriggerProperty() {
        return reportTrigger;
    }

    /**
     * Returns the expression determing whether this report has to be shown.
     *
     * @return The expression determing whether this report has to be shown.
     */
    public BooleanExpression getReportTrigger() {
        return reportTriggerProperty().get();
    }
}
