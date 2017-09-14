/* 
 * Copyright (C) 2017 Stefan Huber
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

import bayern.steinbrecher.green2.utility.ElementsUtility;
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
public class CheckedComboBox<T> extends ComboBox<T> {

    private final BooleanProperty nothingSelected = new SimpleBooleanProperty(this, "nothingSelected");

    public CheckedComboBox() {
        this(FXCollections.observableArrayList());
    }

    public CheckedComboBox(ObservableList<T> items) {
        super(items);
        nothingSelected.bind(Bindings.createBooleanBinding(() -> getSelectionModel().isEmpty(),
                selectionModelProperty(), getSelectionModel().selectedItemProperty()));
        ElementsUtility.addCssClassIf(this, nothingSelected, ElementsUtility.CSS_CLASS_INVALID_CONTENT);
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
}
