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
package bayern.steinbrecher.green2.result;

import bayern.steinbrecher.green2.WizardableController;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

/**
 * The controller of {@link Result}.
 *
 * @author Stefan Huber
 */
public class ResultDialogController extends WizardableController<Optional<Void>> {

    @FXML
    private TableView<List<ReadOnlyStringProperty>> resultView;
    private final ObjectProperty<List<List<String>>> results = new SimpleObjectProperty<>();
    private final BooleanProperty empty = new SimpleBooleanProperty(this, "empty");

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        empty.bind(Bindings.createBooleanBinding(() -> {
            return results.get() == null || results.get().size() < 2
                    || results.get().stream().flatMap(List::stream).count() <= 0;
        }, results));
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
                    //TODO Is there a way to "collect" to an observable list?
                    ObservableList<List<ReadOnlyStringProperty>> items = FXCollections.observableArrayList();
                    newVal.subList(1, newVal.size()).stream()
                            .map(givenRow -> {
                                List<ReadOnlyStringProperty> itemsRow = new ArrayList<>(numColumns);
                                for (int i = 0; i < numColumns; i++) {
                                    String cellValue = i >= givenRow.size() ? "" : givenRow.get(i);
                                    //Each iteration defines an observable cell entry.
                                    itemsRow.add(new SimpleStringProperty(cellValue)); //NOPMD
                                }
                                return itemsRow;
                            })
                            .forEach(itemsRow -> items.add(itemsRow));
                    resultView.setItems(items);
                }
            }
        });
        HBox.setHgrow(resultView, Priority.ALWAYS);
        VBox.setVgrow(resultView, Priority.ALWAYS);
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void export() {
        if (!isEmpty()) {
            Optional<File> path
                    = EnvironmentHandler.askForSavePath(getStage(), "query", "csv");
            if (path.isPresent()) {
                String content = results.get().stream()
                        .map(row -> row.stream().collect(Collectors.joining(";")))
                        .collect(Collectors.joining("\n"));
                try {
                    IOStreamUtility.printContent(content, path.get(), true);
                } catch (IOException ex) {
                    Logger.getLogger(ResultDialogController.class.getName()).log(Level.SEVERE, null, ex);
                    DialogUtility.createStacktraceAlert(
                            getStage(), ex, EnvironmentHandler.getResourceValue("exportFailed"));
                }
            }
        }
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
        return empty;
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