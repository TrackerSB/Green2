package bayern.steinbrecher.green2.memberManagement.result;

import bayern.steinbrecher.wizard.WizardPageController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The controller of {@link ResultDialog}.
 *
 * @author Stefan Huber
 */
public class ResultDialogController extends WizardPageController<Optional<Void>> {

    @FXML
    private TableView<List<ReadOnlyStringProperty>> resultView;
    private final ObjectProperty<List<List<String>>> results = new SimpleObjectProperty<>();
    private final ReadOnlyBooleanWrapper empty = new ReadOnlyBooleanWrapper(this, "empty");

    @FXML
    public void initialize() {
        empty.bind(Bindings.createBooleanBinding(
                () -> results.get() == null
                        || results.get().size() < 2
                        || results.get().stream().mapToLong(List::size).sum() <= 0,
                results));
        results.addListener((obs, oldVal, newVal) -> {
            resultView.getItems().clear();
            resultView.getColumns().clear();

            if (newVal.size() > 0) {
                int numColumns = newVal.stream()
                        .mapToInt(List::size)
                        .max()
                        .orElse(0);
                List<String> headings = newVal.get(0);
                for (int i = 0; i < numColumns; i++) {
                    String heading = i >= headings.size() ? "" : headings.get(i);
                    TableColumn<List<ReadOnlyStringProperty>, String> column
                            = new TableColumn<>(heading); //NOPMD - Each iteration defines an unique column.
                    final int fixedI = i;
                    column.setCellValueFactory(param -> param.getValue().get(fixedI));
                    resultView.getColumns().add(column);
                }

                if (newVal.size() > 1) { //NOPMD - Check whether there are row entries besides the headings.
                    ObservableList<List<ReadOnlyStringProperty>> items = newVal.subList(1, newVal.size()).stream()
                            .map(givenRow -> {
                                List<ReadOnlyStringProperty> itemsRow = new ArrayList<>(numColumns);
                                for (int i = 0; i < numColumns; i++) {
                                    String cellValue = i >= givenRow.size() ? "" : givenRow.get(i);
                                    //Each iteration defines an observable cell entry.
                                    itemsRow.add(new SimpleStringProperty(cellValue)); //NOPMD
                                }
                                return itemsRow;
                            }).collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);
                    resultView.setItems(items);
                }
            }
        });
        HBox.setHgrow(resultView, Priority.ALWAYS);
        VBox.setVgrow(resultView, Priority.ALWAYS);
    }

    @FXML
    @SuppressWarnings("unused")
    private void export() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Void> calculateResult() {
        return Optional.empty();
    }

    /**
     * Returns the property holding the currently shown results.
     *
     * @return The property holding the currently shown results.
     */
    public ObjectProperty<List<List<String>>> resultsProperty() {
        return results;
    }

    /**
     * Returns the currently shown results.
     *
     * @return The currently shown results.
     */
    public List<List<String>> getResults() {
        return resultsProperty().get();
    }

    /**
     * Replaces the currently shown results with the given one.
     *
     * @param results The results to replace the currently shown once with.
     */
    public void setResults(List<List<String>> results) {
        resultsProperty().set(results);
    }

    /**
     * Returns the property holding whether the shown result is empty.
     *
     * @return The property holding whether the shown result is empty. It holds {@code true} if and only if the current
     * result contains no columns, no rows or only one row representing the headings of the columns.
     */
    public ReadOnlyBooleanProperty emptyProperty() {
        return empty.getReadOnlyProperty();
    }

    /**
     * Checks whether the shown result is empty.
     *
     * @return {@code true} if and only if the current result contains no columns, no rows or only one row representing
     * the headings of the columns.
     */
    public boolean isEmpty() {
        return emptyProperty().get();
    }
}
