/*
 * Copyright (C) 2018 Steinbrecher
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
package bayern.steinbrecher.green2.query;

import bayern.steinbrecher.green2.WizardableController;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.connection.scheme.Tables;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.CheckedControl;
import bayern.steinbrecher.green2.elements.spinner.ContributionField;
import bayern.steinbrecher.green2.utility.BindingUtility;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

/**
 * Represents the controller of the dialog for querying member.
 *
 * @author Stefan Huber
 */
public class QueryController extends WizardableController {

    @FXML
    private GridPane queryInput;
    private final ObjectProperty<DBConnection> dbConnection = new SimpleObjectProperty<>(this, "dbConnection");
    private final ObjectProperty<Optional<List<List<String>>>> lastQueryResult
            = new SimpleObjectProperty<>(Optional.empty());
    private boolean isLastQueryUptodate = false;

    //TODO Is there any way to connect these two questionmarks?
    private Optional<CheckedConditionField<?>> createConditionField(Class<?> columnType) {
        CheckedConditionField<?> conditionField;
        //TODO How to avoid isAssignableFrom(...)?
        if (columnType.isAssignableFrom(Boolean.class)) {
            conditionField = new BooleanConditionField();
        } else if (columnType.isAssignableFrom(String.class)) {
            conditionField = new StringConditionField();
        } else if (columnType.isAssignableFrom(Integer.class)) {
            conditionField = new IntegerConditionField();
        } else if (columnType.isAssignableFrom(Double.class)) {
            conditionField = new DoubleConditionField();
        } else if (columnType.isAssignableFrom(LocalDate.class)) {
            conditionField = new LocalDateConditionField();
        } else {
            conditionField = null;
        }
        return Optional.ofNullable(conditionField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbConnection.addListener((obs, oldVal, newVal) -> {
            queryInput.getChildren().clear();
            int rowCounter = 0;
            List<Pair<String, Class<?>>> sortedColumns = newVal.getAllColumns(Tables.MEMBER).stream()
                    .sorted((c1, c2) -> c1.getKey().compareToIgnoreCase(c2.getKey()))
                    .collect(Collectors.toList());
            for (Pair<String, Class<?>> column : sortedColumns) {
                Label columnLabel = new Label(column.getKey());
                Optional<CheckedConditionField<?>> conditionField = createConditionField(column.getValue());
                if (conditionField.isPresent()) {
                    queryInput.addRow(rowCounter, columnLabel, conditionField.get());
                    rowCounter++;
                } else {
                    Logger.getLogger(QueryController.class.getName())
                            .log(Level.WARNING, "The type {0} of column {1} is not supported by the query dialog.",
                                    new Object[]{column.getValue(), column.getKey()});
                }
            }
            if (rowCounter <= 0) {
                throw new IllegalStateException(
                        "The query dialog can not be opened since it can not show any column to query.");
            }
            isLastQueryUptodate = false;
            //TODO Should lastQueryResult be cleared when changing the DBConnection?
        });
    }

    //TODO Should this method be synchronized?
    private void updateLastQueryResult() {
        if (!isLastQueryUptodate) {
            isLastQueryUptodate = true;
            throw new UnsupportedOperationException("Querying itself is not supported yet");
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void query() {
        if (isValid()) {
            updateLastQueryResult();
            stage.close();
        }
    }

    public Optional<List<List<String>>> getQueryResult() {
        if (userAbborted()) {
            return Optional.empty();
        } else {
            updateLastQueryResult();
            return lastQueryResult.get();
        }
    }

    public ObjectProperty<DBConnection> dbConnectionProperty() {
        return dbConnection;
    }

    public void setDbConnection(DBConnection dbConnection) {
        this.dbConnection.set(dbConnection);
    }

    public DBConnection getDbConnection() {
        return dbConnectionProperty().get();
    }

    private static abstract class CheckedConditionField<T> extends HBox implements CheckedControl, Initializable {

        private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid", false);
        private final ListProperty<ObservableBooleanValue> validConditions
                = new SimpleListProperty<>(FXCollections.observableArrayList());
        private final BooleanProperty validCondition = new SimpleBooleanProperty(true);
        private final BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid", true);
        private final BooleanProperty checked = new SimpleBooleanProperty(this, "checked", true);

        protected final void loadFXML(String fxmlFileName) {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    CheckedConditionField.class.getResource(fxmlFileName), EnvironmentHandler.RESOURCE_BUNDLE);
            try {
                fxmlLoader.setRoot(this);
                fxmlLoader.setController(this);
                fxmlLoader.load();
            } catch (IOException ex) {
                Logger.getLogger(ContributionField.class.getName())
                        .log(Level.SEVERE, "Could not load CheckedConditionField", ex);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void initialize(URL location, ResourceBundle resources) {
            validConditions.addListener(
                    (obs, oldVal, newVal) -> validCondition.bind(BindingUtility.reduceAnd(newVal.stream())));
            valid.bind(validCondition.or(checked.not()));
            invalid.bind(valid.not());
        }

        protected abstract Optional<String> getConditionImpl();

        public final Optional<String> getCondition() {
            Optional<String> condition;
            if (isValid()) {
                condition = getConditionImpl();
            } else {
                condition = Optional.empty();
            }
            return condition;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BooleanProperty checkedProperty() {
            return checked;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isChecked() {
            return checked.get();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setChecked(boolean checked) {
            this.checked.set(checked);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return valid;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid() {
            return valid.get();
        }

        protected void addValidCondition(ObservableBooleanValue condition) {
            validConditions.get().add(condition);
        }
    }

    private static class BooleanConditionField extends CheckedConditionField<Boolean> {

        public BooleanConditionField() {
            loadFXML("BooleanConditionField.fxml");
        }

        @Override
        protected Optional<String> getConditionImpl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class StringConditionField extends CheckedConditionField<String> {

        public StringConditionField() {
            loadFXML("StringConditionField.fxml");
        }

        @Override
        protected Optional<String> getConditionImpl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class IntegerConditionField extends CheckedConditionField<Integer> {

        public IntegerConditionField() {
            loadFXML("IntegerConditionField.fxml");
        }

        @Override
        protected Optional<String> getConditionImpl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class DoubleConditionField extends CheckedConditionField<Double> {

        public DoubleConditionField() {
            loadFXML("DoubleConditionField.fxml");
        }

        @Override
        protected Optional<String> getConditionImpl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class LocalDateConditionField extends CheckedConditionField<LocalDate> {

        public LocalDateConditionField() {
            loadFXML("LocalDateConditionField.fxml");
        }

        @Override
        protected Optional<String> getConditionImpl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
