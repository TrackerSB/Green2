package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.elements.CheckedDoubleSpinner;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.value.ChangeListener;
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
    private List<T> options;
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
        @Override
        public Object getBean() {
            return SelectionController.this;
        }

        @Override
        public String getName() {
            return "currentSelectedCount";
        }
    };
    @FXML
    private Label itemCount;
    @FXML
    private CheckedDoubleSpinner contributionSpinner;
    private final ChangeListener<Boolean> selectionChange
            = (obs, oldVal, newVal) -> {
        ObservableList<CheckBox> items = optionsListView.getItems();
        boolean disableSelectAll = items.parallelStream()
                .allMatch(CheckBox::isSelected);
        selectAllButton.setDisable(disableSelectAll);
        boolean disableSelectNone = items.parallelStream()
                .allMatch(cb -> !cb.isSelected());
        selectNoneButton.setDisable(disableSelectNone);
        missingInput.setVisible(disableSelectNone);
        if (newVal) {
            currentSelectedCount.set(currentSelectedCount.get() + 1);
        } else {
            currentSelectedCount.set(currentSelectedCount.get() - 1);
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contributionSpinner.validProperty()
                .addListener((obs, oldVal, newVal) -> {
                    selectButton.setDisable(
                            selectNoneButton.isDisabled() || !newVal);
                });
        contributionSpinner.getEditor().setOnAction(aevt -> select());
        selectNoneButton.disabledProperty()
                .addListener((obs, oldValue, newValue) -> {
                    selectButton.setDisable(selectNoneButton.isDisabled()
                            || !contributionSpinner.validProperty().get());
                });
        currentSelectedCount.addListener((obs, oldVal, newVal) -> {
            selectedCount.setText(newVal.toString());
        });
    }

    public void setOptions(List<T> options) {
        optionsListView.getItems().clear();
        this.options = options;
        options.stream().sorted().forEach(op -> {
            CheckBox newItem = new CheckBox(op.toString());
            newItem.selectedProperty().addListener(selectionChange);
            optionsListView.getItems().add(newItem);
        });
        itemCount.setText(String.valueOf(options.size()));
    }

    @FXML
    private void select() {
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

    public List<T> getSelection() {
        List<T> selection = new ArrayList<>();
        ObservableList<CheckBox> items = optionsListView.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                selection.add(options.get(i));
            }
        }
        return selection;
    }

    public Double getContribution() {
        if (userConfirmed) {
            return contributionSpinner.getValue();
        }
        return null;
    }
}
