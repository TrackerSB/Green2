/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.selection;

import bayern.steinbrecher.green2.WizardableController;
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
 * @param <T> The type of the objects being able to select.
 * @author Stefan Huber
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
            newVal.forEach(op -> {
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
        optionsListView.getItems().forEach(cb -> cb.setSelected(true));
    }

    @FXML
    private void selectNoOption() {
        optionsListView.getItems().forEach(cb -> cb.setSelected(false));
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
