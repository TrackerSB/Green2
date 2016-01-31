package bayern.steinbrecher.gruen2.selection;

import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * Represents controller for Selection.fxml.
 *
 * @author Stefan Huber
 */
public class SelectionController<T> implements Initializable {

    private Stage stage;
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
    private CheckedTextField contributionTextField;
    private final ChangeListener<Boolean> selectionChange
            = (obs, oldVal, newVal) -> {
                ObservableList<CheckBox> items = optionsListView.getItems();
                boolean disableSelectAll = items.stream()
                .allMatch(CheckBox::isSelected);
                selectAllButton.setDisable(disableSelectAll);
                boolean disableSelectNone = items.stream()
                .allMatch(cb -> !cb.isSelected());
                selectNoneButton.setDisable(disableSelectNone);
            };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setStage(Stage stage) {
        this.stage = stage;
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
        stage.close();
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
}
