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
import bayern.steinbrecher.green2.elements.report.ReportType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * Extended version of {@link ComboBox}. Also contains {@link BooleanProperty} indicating whether nothing is selected.
 *
 * @author Stefan Huber
 * @param <T> The type of the element of the ComboBox.
 */
public class CheckedComboBox<T> extends ComboBox<T> implements CheckedControl {

    private final CheckableControlBase<CheckedComboBox<T>> ccBase = new CheckableControlBase<>(this);
    private final BooleanProperty nothingSelected = new SimpleBooleanProperty(this, "nothingSelected");

    /**
     * Creates a {@link CheckedComboBox} which contains no element.
     */
    public CheckedComboBox() {
        this(FXCollections.observableArrayList());
    }

    /**
     * Creates a {@link CheckedComboBox} which show the given elements.
     *
     * @param items The elements to show.
     */
    public CheckedComboBox(ObservableList<T> items) {
        super(items);
        initProperties();
    }

    private void initProperties() {
        nothingSelected.bind(Bindings.createBooleanBinding(() -> getSelectionModel().isEmpty(),
                selectionModelProperty(), getSelectionModel().selectedItemProperty()));
        addReport(new ReportEntry("nothingSelected", ReportType.ERROR, nothingSelected));
    }

    /**
     * Represents a boolean value indicating whether the textfield is empty or not.
     *
     * @return The property represents a boolean value indicating whether the textfield is empty or not.
     */
    public ReadOnlyBooleanProperty nothingSelectedProperty() {
        return nothingSelected;
    }

    /**
     * Checks whether the textfield is empty.
     *
     * @return {@code true} only if the textfield is empty.
     */
    public boolean isNothingSelected() {
        return nothingSelected.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlyBooleanProperty checkedProperty() {
        return ccBase.checkedProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChecked() {
        return ccBase.isChecked();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return ccBase.validProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return ccBase.isValid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservableList<ReportEntry> getReports() {
        return ccBase.getReports();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addReport(ReportEntry report) {
        return ccBase.addReport(report);
    }
}
