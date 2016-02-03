package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;

/**
 * Represents controller for Selection.fxml.
 *
 * @author Stefan Huber
 */
public class SelectionController<T> extends Controller {

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
    private Spinner<Double> contributionSpinner;
    private final ChangeListener<Boolean> selectionChange
            = (obs, oldVal, newVal) -> {
                ObservableList<CheckBox> items = optionsListView.getItems();
                boolean disableSelectAll = items.stream()
                .allMatch(CheckBox::isSelected);
                selectAllButton.setDisable(disableSelectAll);
                boolean disableSelectNone = items.stream()
                .allMatch(cb -> !cb.isSelected());
                selectNoneButton.setDisable(disableSelectNone);
                missingInput.setVisible(disableSelectNone);
            };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DoubleSpinnerValueFactory factory
                = new DoubleSpinnerValueFactory(0, 200, 10, 0.01);
        contributionSpinner.setValueFactory(factory);
        contributionSpinner.getEditor().textProperty()
                .addListener((obs, oldValue, newValue) -> {
                    try {
                        double parsed = Double.parseDouble(newValue);
                        factory.setValue(parsed);
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(SelectionController.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                    selectButton.setDisable(selectNoneButton.isDisabled()
                            || contributionSpinner.getValue() == null);
                });

        selectNoneButton.disabledProperty()
                .addListener((obs, oldValue, newValue) -> {
                    selectButton.setDisable(selectNoneButton.isDisabled()
                            || contributionSpinner.getValue() == null);
                });
    }

    public void setOptions(List<T> options) {
        optionsListView.getItems().clear();
        this.options = options;
        options.stream().forEach(op -> {
            CheckBox newItem = new CheckBox(op.toString());
            newItem.selectedProperty().addListener(selectionChange);
            optionsListView.getItems().add(newItem);
        });
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
        List<T> selection = new LinkedList<>();
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
