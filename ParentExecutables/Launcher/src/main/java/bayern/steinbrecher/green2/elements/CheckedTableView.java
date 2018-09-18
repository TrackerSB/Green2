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

import bayern.steinbrecher.green2.elements.report.ReportEntry;
import bayern.steinbrecher.green2.elements.report.Reportable;
import bayern.steinbrecher.green2.elements.report.ReportableBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * Represents a {@link TableView} which is extended by the capability of reporting.
 *
 * @author Stefan Huber
 * @param <S> See {@link TableView}.
 * @since 2u14
 */
public class CheckedTableView<S> extends TableView<S> implements Reportable {

    private final ReportableBase<CheckedTableView<S>> reportableBase = new ReportableBase<>(this);

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return reportableBase.validProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return reportableBase.isValid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservableList<ReportEntry> getReports() {
        return reportableBase.getReports();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addReport(ReportEntry report) {
        return reportableBase.addReport(report);
    }
}
