package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.Controller;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ListPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Represents controller for Selection.fxml.
 *
 * @author Stefan Huber
 * @param <T> The type of the objects being able to select.
 */
public class SelectionController<T extends Comparable> extends Controller {

    @FXML
    private ListView<CheckBox> optionsListView;
    private ListProperty<T> optionsProperty
            = new ListPropertyBase<T>(FXCollections.observableArrayList()) {
        @Override
        public SelectionController getBean() {
            return SelectionController.this;
        }

        @Override
        public String getName() {
            return "options";
        }
    };
    @FXML
    private Button selectAllButton;
    @FXML
    private Button selectNoneButton;
    @FXML
    private Button selectButton;
    @FXML
    private Label missingInput;
    @FXML
    private Label selectedCount;
    private IntegerProperty currentSelectedCount = new IntegerPropertyBase() {
        /**
         * {@inheritDoc}
         */
        @Override
        public Object getBean() {
            return SelectionController.this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return "currentSelectedCount";
        }
    };
    @FXML
    private Label itemCount;
    private final ChangeListener<Boolean> selectionChange
            = (obs, oldVal, newVal) -> {
        if (newVal) {
            currentSelectedCount.set(currentSelectedCount.get() + 1);
        } else {
            currentSelectedCount.set(currentSelectedCount.get() - 1);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentSelectedCount.lessThanOrEqualTo(0)
                .addListener((obs, oldVal, newVal) -> {
                    missingInput.setVisible(newVal);
                    selectButton.setDisable(newVal);
                    selectNoneButton.setDisable(newVal);
                });
        currentSelectedCount.addListener((obs, oldVal, newVal) -> {
            selectedCount.setText(newVal.toString());
        });
        optionsProperty.addListener((obs, oldVal, newVal) -> {
            optionsListView.getItems().clear();
            newVal.stream().forEach(op -> {
                CheckBox newItem = new CheckBox(op.toString());
                newItem.selectedProperty().addListener(selectionChange);
                optionsListView.getItems().add(newItem);
            });
            itemCount.setText(String.valueOf(newVal.size()));
            currentSelectedCount.greaterThanOrEqualTo(newVal.size())
                    .addListener((obss, oldVall, newVall) -> {
                        selectAllButton.setDisable(newVall);
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
        if (!selectButton.isDisabled()) {
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
}
