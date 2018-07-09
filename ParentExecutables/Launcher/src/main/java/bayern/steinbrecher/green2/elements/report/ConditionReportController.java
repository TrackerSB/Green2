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
package bayern.steinbrecher.green2.elements.report;

import bayern.steinbrecher.green2.ResultController;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Pair;

/**
 * The controller of {@link ConditionReport}.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ConditionReportController extends ResultController<Optional<Boolean>> {

    private final ObservableValue<ObservableList<Condition>> conditions
            = new SimpleObjectProperty<>(this, "conditions",
                    FXCollections.observableArrayList(i -> new Observable[]{i.nameProperty(), i.valueProperty()}));
    @FXML
    private TableView<Condition> conditionsReport;
    @FXML
    private ReportSummary reportSummary;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conditionsReport.itemsProperty().bind(conditions);

        TableColumn<Condition, String> conditionNameColumn
                = new TableColumn<>(EnvironmentHandler.getResourceValue("condition"));
        conditionNameColumn.setCellValueFactory(item -> item.getValue().nameProperty());
        TableColumn<Condition, Optional<Boolean>> conditionValueColumn
                = new TableColumn<>(EnvironmentHandler.getResourceValue("result"));
        conditionValueColumn.setCellValueFactory(item -> item.getValue().valueProperty());
        conditionValueColumn.setCellFactory(column -> new TableCell<Condition, Optional<Boolean>>() {
            @Override
            protected void updateItem(Optional<Boolean> item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    String resultTextKey;
                    EnvironmentHandler.ImageSet graphic;
                    if (item.isPresent()) {
                        if (item.get()) {
                            resultTextKey = "successful";
                            graphic = EnvironmentHandler.ImageSet.SUCCESS;
                        } else {
                            resultTextKey = "failing";
                            graphic = EnvironmentHandler.ImageSet.ERROR_SMALL;
                        }
                    } else {
                        resultTextKey = "skipped";
                        graphic = EnvironmentHandler.ImageSet.NEXT;
                    }
                    setText(EnvironmentHandler.getResourceValue(resultTextKey));
                    setGraphic(graphic.getAsImageView());
                }
            }
        });
        conditionsReport.getColumns()
                .setAll(List.of(conditionNameColumn, conditionValueColumn));

        conditions.getValue().addListener((ListChangeListener.Change<? extends Condition> change) -> {
            while (change.next()) {
                change.getAddedSubList()
                        .stream()
                        .forEach(reportSummary::addReportEntry);
                change.getRemoved()
                        .stream()
                        .forEach(reportSummary::removeReportValidation);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Boolean> calculateResult() {
        boolean result = conditions.getValue()
                .stream()
                /*
                 * Treat skipped conditions as valid conditions since a warning is shown and the check itself may be
                 * erroreous.
                 */
                .allMatch(condition -> condition.getValue().orElse(true));
        return Optional.of(result);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    @FXML
    private void close() {
        getStage().close();
    }

    /**
     * Replaces the currently set conditions with the given ones.
     *
     * @param conditions The conditions to replace the current ones with. Values of type {@link Optional#empty()}
     * represent skipped conditions.
     */
    public void setConditions(Map<String, Optional<Callable<Boolean>>> conditions) {
        this.conditions.getValue().clear();
        conditions.entrySet()
                .stream()
                .forEach(entry -> this.conditions.getValue().add(new Condition(entry.getKey(), entry.getValue())));
    }

    private static class Condition implements Reportable {

        private final StringProperty name = new SimpleStringProperty(this, "name");
        /**
         * Currently the hold value is not updated automatically.
         */
        private final ObjectProperty<Optional<Boolean>> value = new SimpleObjectProperty<>(this, "value");

        Condition(String name, Optional<Callable<Boolean>> value) {
            this.name.set(name);
            this.value.set(value.map(callable -> {
                try {
                    return callable.call();
                } catch (Exception ex) {
                    Logger.getLogger(ConditionReportController.class.getName())
                            .log(Level.WARNING, "An evaluation of a condition failed. It is skipped.", ex);
                    return null;
                }
            }));
        }

        @Override
        public Map<String, Pair<ReportType, BooleanExpression>> getReports() {
            return Map.of(EnvironmentHandler.getResourceValue("skippedConditions"),
                    new Pair<>(ReportType.WARNING, value.isEqualTo(Optional.empty())));
        }

        public StringProperty nameProperty() {
            return name;
        }

        public String getName() {
            return nameProperty().get();
        }

        public void setName(String name) {
            nameProperty().set(name);
        }

        public ReadOnlyObjectProperty<Optional<Boolean>> valueProperty() {
            return value;
        }

        public Optional<Boolean> getValue() {
            return valueProperty().get();
        }
    }
}
