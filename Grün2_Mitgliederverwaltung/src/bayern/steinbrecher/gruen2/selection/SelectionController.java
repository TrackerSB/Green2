package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.Controller;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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

/**
 * Represents controller for Selection.fxml.
 *
 * @author Stefan Huber
 * @param <T> The type of the objects being able to select.
 */
public class SelectionController<T extends Comparable> extends Controller {

    private ListProperty<T> optionsProperty
            = new SimpleListProperty<>(FXCollections.observableArrayList());
    private IntegerProperty selectedCount
            = new SimpleIntegerProperty(this, "selectedCount");
    private ReadOnlyIntegerProperty totalCount = optionsProperty.sizeProperty();
    private BooleanProperty nothingSelected = new SimpleBooleanProperty();
    private BooleanProperty allSelected = new SimpleBooleanProperty();
    private BooleanProperty valid = new SimpleBooleanProperty();
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
        if (isValid()) {
            userConfirmed = true;
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
        if (userConfirmed) {
            List<T> selection = new ArrayList<>();
            ObservableList<CheckBox> items = optionsListView.getItems();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isSelected()) {
                    selection.add(optionsProperty.get(i));
                }
            }
            return Optional.of(selection);
        }
        return Optional.empty();
    }

    public ReadOnlyIntegerProperty selectedCountProperty() {
        return selectedCount;
    }

    public int getSelectedCount() {
        return selectedCount.get();
    }

    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount;
    }

    public int getTotalCount() {
        return totalCount.get();
    }

    public ReadOnlyBooleanProperty nothingSelectedProperty() {
        return nothingSelected;
    }

    public boolean isNothingSelected() {
        return nothingSelected.get();
    }

    public ReadOnlyBooleanProperty allSelectedProperty() {
        return allSelected;
    }

    public boolean isAllSelected() {
        return allSelected.get();
    }

    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    public boolean isValid() {
        return valid.get();
    }
}
