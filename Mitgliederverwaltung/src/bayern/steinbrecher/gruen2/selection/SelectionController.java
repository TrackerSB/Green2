/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.WizardableController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Represents controller for Selection.fxml.
 *
 * @author Stefan Huber
 * @param <T> The type of the objects being able to select.
 */
public class SelectionController<T extends Comparable<T>>
        extends WizardableController {

    private final ListProperty<T> optionsProperty
            = new SimpleListProperty<>(FXCollections.observableArrayList());
    private IntegerProperty selectedCount
            = new SimpleIntegerProperty(this, "selectedCount");
    private final ReadOnlyIntegerProperty totalCount
            = optionsProperty.sizeProperty();
    private final BooleanProperty nothingSelected
            = new SimpleBooleanProperty(this, "nothingSelected");
    private final BooleanProperty allSelected
            = new SimpleBooleanProperty(this, "allSelected");
    @FXML
    private ListView<CheckBox> optionsListView;
    private final ChangeListener<Boolean> selectionChange
            = (obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedCount.set(selectedCount.get() + 1);
                } else {
                    selectedCount.set(selectedCount.get() - 1);
                }
            };

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nothingSelected.bind(selectedCount.lessThanOrEqualTo(0));
        allSelected.bind(selectedCount.greaterThanOrEqualTo(totalCount));
        valid.bind(nothingSelected.not());

        optionsProperty.addListener((obs, oldVal, newVal) -> {
            optionsListView.getItems().clear();
            newVal.stream().forEach(op -> {
                CheckBox newItem = new CheckBox(op.toString());
                newItem.selectedProperty().addListener(selectionChange);
                optionsListView.getItems().add(newItem);
            });
        });
    }

    /**
     * Removes all options and replaces them with the new list of options.
     *
     * @param options The list of new options.
     */
    public void setOptions(List<T> options) {
        this.optionsProperty.setAll(
                options.stream().sorted().collect(Collectors.toList()));
    }

    @FXML
    private void select() {
        checkStage();
        if (valid.get()) {
            stage.close();
        }
    }

    @FXML
    private void selectAllOptions() {
        optionsListView.getItems().stream().forEach(cb -> cb.setSelected(true));
    }

    @FXML
    private void selectNoOption() {
        optionsListView.getItems().stream()
                .forEach(cb -> cb.setSelected(false));
    }

    /**
     * Returns the list of currently selected items. Returns
     * {@code Optional.empty} if the user didn´t confirm the selection yet.
     *
     * @return An Optional containing the selection if any.
     */
    public Optional<List<T>> getSelection() {
        if (userAbborted()) {
            return Optional.empty();
        } else {
            List<T> selection = new ArrayList<>();
            ObservableList<CheckBox> items = optionsListView.getItems();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isSelected()) {
                    selection.add(optionsProperty.get(i));
                }
            }
            return Optional.of(selection);
        }
    }

    /**
     * Returns the property representing the number of currently selected
     * fields.
     *
     * @return The property representing the number of currently selected
     * fields.
     */
    public ReadOnlyIntegerProperty selectedCountProperty() {
        return selectedCount;
    }

    /**
     * Returns the number of currently selected fields.
     *
     * @return The number of currently selected fields.
     */
    public int getSelectedCount() {
        return selectedCount.get();
    }

    /**
     * Returns the property representing the total number of entries to select.
     *
     * @return The property representing the total number of entries to select.
     */
    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount;
    }

    /**
     * The total number of entries to select.
     *
     * @return The total number of entries to select.
     */
    public int getTotalCount() {
        return totalCount.get();
    }

    /**
     * Returns the property representing a boolean indicating whether no entry
     * is currently selected.
     *
     * @return The property representing a boolean indicating whether no entry
     * is currently selected.
     */
    public ReadOnlyBooleanProperty nothingSelectedProperty() {
        return nothingSelected;
    }

    /**
     * Checks whether currently no entry is selected.
     *
     * @return {@code true} only if no entry is selected.
     */
    public boolean isNothingSelected() {
        return nothingSelected.get();
    }

    /**
     * Returns the property representing a boolean indicating whether every
     * entry is currently selected.
     *
     * @return The property representing a boolean indicating whether every
     * entry is currently selected.
     */
    public ReadOnlyBooleanProperty allSelectedProperty() {
        return allSelected;
    }

    /**
     * Checks whether currently every entry is selected.
     *
     * @return {@code true} only if every entry is selected.
     */
    public boolean isAllSelected() {
        return allSelected.get();
    }
}
