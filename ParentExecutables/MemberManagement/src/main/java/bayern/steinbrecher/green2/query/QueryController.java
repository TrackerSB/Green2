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
package bayern.steinbrecher.green2.query;

import bayern.steinbrecher.green2.WizardableController;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.connection.scheme.ColumnParser;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.connection.scheme.Tables;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.elements.CheckedDatePicker;
import bayern.steinbrecher.green2.elements.spinner.CheckedSpinner;
import bayern.steinbrecher.green2.utility.BindingUtility;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import bayern.steinbrecher.green2.elements.ReadOnlyCheckedControl;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Priority;

/**
 * Represents the controller of the dialog for querying member.
 *
 * @author Stefan Huber
 */
public class QueryController extends WizardableController<Optional<List<List<String>>>> {

    private static final Logger LOGGER = Logger.getLogger(QueryController.class.getName());
    @FXML
    private GridPane queryInput;
    private final ListProperty<CheckedConditionField<?>> conditionFields
            = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<DBConnection> dbConnection = new SimpleObjectProperty<>(this, "dbConnection");
    private final ObjectProperty<Optional<List<List<String>>>> lastQueryResult
            = new SimpleObjectProperty<>(Optional.empty());
    private boolean isLastQueryUptodate;

    //TODO Is there any way to connect these questionmarks?
    @SuppressWarnings("unchecked")
    private Optional<CheckedConditionField<?>> createConditionField(Pair<String, Class<?>> column) {
        CheckedConditionField<?> conditionField;
        //TODO How to avoid isAssignableFrom(...)?
        //TODO How to avoid explicit cast?
        Class<?> columnType = column.getValue();
        if (columnType.isAssignableFrom(Boolean.class)) {
            conditionField = new BooleanConditionField(new Pair<>(column.getKey(), (Class<Boolean>) columnType));
        } else if (columnType.isAssignableFrom(String.class)) {
            conditionField = new StringConditionField(new Pair<>(column.getKey(), (Class<String>) columnType));
        } else if (columnType.isAssignableFrom(Integer.class)) {
            conditionField = new IntegerConditionField(new Pair<>(column.getKey(), (Class<Integer>) columnType));
        } else if (columnType.isAssignableFrom(Double.class)) {
            conditionField = new DoubleConditionField(new Pair<>(column.getKey(), (Class<Double>) columnType));
        } else if (columnType.isAssignableFrom(LocalDate.class)) {
            conditionField = new LocalDateConditionField(new Pair<>(column.getKey(), (Class<LocalDate>) columnType));
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
                Optional<CheckedConditionField<?>> conditionField = createConditionField(column);
                if (conditionField.isPresent()) {
                    conditionFields.add(conditionField.get());
                    Label columnLabel = new Label(column.getKey()); //NOPMD - Each iteration defines a unique label.
                    queryInput.addRow(rowCounter, columnLabel);
                    ObservableList<Node> children = conditionField.get().getChildren();
                    Node[] conditionFieldChildren = children.toArray(new Node[children.size()]); //NOPMD
                    //CHECKSTYLE.OFF: MagicNumber - Having exactly 3 elements is only important for the visual layout.
                    if (conditionFieldChildren.length != 3) { //NOPMD - Only triples are currently layouted nicely.
                        //CHECKSTYLE.ON: MagicNumber
                        LOGGER.log(Level.WARNING, "An input field of the query dialog has not exactly 3 children. "
                                + "It may cause displacement of elements.");
                    }
                    queryInput.addRow(rowCounter, conditionFieldChildren);
                    rowCounter++;
                } else {
                    LOGGER.log(Level.WARNING, "The type {0} of column {1} is not supported by the query dialog.",
                            new Object[]{column.getValue(), column.getKey()});
                }
            }
            if (rowCounter <= 0) {
                throw new IllegalStateException(
                        "The query dialog can not be opened since it can not show any column to query.");
            }
            isLastQueryUptodate = false;
            updateLastQueryResult();
        });
        conditionFields.addListener((obs, oldVal, newVal) -> {
            newVal.addListener((ListChangeListener.Change<? extends CheckedConditionField<?>> change) -> {
                while (change.next()) {
                    change.getAddedSubList()
                            .forEach(ccf -> ccf.addListener(invalidObs -> isLastQueryUptodate = false));
                }
            });
            bindValidProperty(
                    BindingUtility.reduceAnd(conditionFields.stream().map(CheckedConditionField::validProperty))
                            .and(BindingUtility.reduceAnd(
                                    conditionFields.stream().map(CheckedConditionField::emptyProperty)).not()
                            )
            );
        });
    }

    private synchronized void updateLastQueryResult() {
        if (!isLastQueryUptodate) {
            List<String> conditions = conditionFields.stream()
                    .map(CheckedConditionField::getCondition)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            Optional<String> searchQuery = dbConnectionProperty().get()
                    .generateSearchQuery(Tables.MEMBER, new ArrayList<>(), conditions);
            searchQuery.ifPresent(query -> {
                try {
                    lastQueryResult.set(Optional.of(dbConnectionProperty().get().execQuery(query)));
                    isLastQueryUptodate = true;
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "The query \"" + query + "\" failed.", ex);
                }
            });
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void query() {
        if (isValid()) {
            updateLastQueryResult();
            getStage().close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<List<List<String>>> calculateResult() {
        updateLastQueryResult();
        return lastQueryResult.get();
    }

    /**
     * Returns the property holding the currently used {@link DBConnection}.
     *
     * @return The property holding the currently used {@link DBConnection}.
     */
    public ObjectProperty<DBConnection> dbConnectionProperty() {
        return dbConnection;
    }

    /**
     * Sets a new {@link DBConnection}.
     *
     * @param dbConnection The {@link DBConnection} to use from now on.
     */
    public void setDbConnection(DBConnection dbConnection) {
        this.dbConnection.set(dbConnection);
    }

    /**
     * Returns the currently used {@link DBConnection}.
     *
     * @return The currently used {@link DBConnection}.
     */
    public DBConnection getDbConnection() {
        return dbConnectionProperty().get();
    }

    /**
     * Represents specialized fields for querying certain types of columns of a database table.
     *
     * @param <T> The type of the column to query.
     */
    private abstract static class CheckedConditionField<T> extends HBox
            implements ReadOnlyCheckedControl, Initializable, Observable {

        private static final Logger LOGGER = Logger.getLogger(CheckedConditionField.class.getName());
        private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid", true);
        private final ListProperty<ObservableBooleanValue> validConditions
                = new SimpleListProperty<>(FXCollections.observableArrayList());
        private final BooleanProperty validCondition = new SimpleBooleanProperty(true);
        private final BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid", false);
        private final BooleanProperty checked = new SimpleBooleanProperty(this, "checked", false);
        private final BooleanProperty empty = new SimpleBooleanProperty(this, "empty", false);
        private final String realColumnName;

        /**
         * Creates a new {@link CheckedConditionField} for the given column and its appropriate type.
         *
         * @param column The column to create an input field for.
         */
        CheckedConditionField(Pair<String, Class<T>> column) {
            super();
            realColumnName = column.getKey();
            loadFXML();
        }

        /**
         * Returns the name of the fxml file to load, which represents this input field.
         *
         * @return The name of the fxml file to load, which represents this input field.
         */
        protected abstract String getFxmlFileName();

        private void loadFXML() {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    CheckedConditionField.class.getResource(getFxmlFileName()), EnvironmentHandler.RESOURCE_BUNDLE);
            try {
                fxmlLoader.setRoot(this);
                fxmlLoader.setController(this);
                fxmlLoader.load();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not load CheckedConditionField", ex);
            }
        }

        /**
         * This method may be overridden in order to add further calls to
         * {@link #initialize(java.net.URL, java.util.ResourceBundle)}.
         */
        @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
        protected void initializeImpl() {
            //No-op
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
            checked.bind(emptyProperty().not());
            initializeImpl();
        }

        /**
         * Returns the condition part itself. E.g. "LIKE 'some text'" or "&lt;= 0". The column name is added by
         * {@link #getCondition()} as well as a check for validity of the input.
         *
         * @return The condition part like "LIKE 'some text'" or "&lt;= 0". Returns {@link Optional#empty()} only if the
         * condition could not be constructed.
         */
        protected abstract Optional<String> getConditionImpl();

        /**
         * Returns the condition represented by this input field. E.g. "someColumn LIKE 'some text'" or "someOtherColumn
         * &lt;= 0"
         *
         * @return A representation of a condition suitable for the database of this profile. Returns
         * {@link Optional#empty()} only if the input is not valid or the condition could not be constructed.
         * @see ProfileSettings#DBMS
         * @see SupportedDatabases
         */
        public final Optional<String> getCondition() {
            Optional<String> condition;
            if (isValid() && !isEmpty()) {
                SupportedDatabases dbms = EnvironmentHandler.getProfile().get(ProfileSettings.DBMS);
                condition = getConditionImpl();
                if (condition.isPresent()) {
                    condition = Optional.of(dbms.quoteIdentifier(realColumnName) + " " + condition.get());
                }
            } else {
                condition = Optional.empty();
            }
            return condition;
        }

        /**
         * Creates a {@link StringConverter} mapping 1:1 a {@link String} displayed to the user and its associated
         * value.
         *
         * @param <T> The type of the value to associate with {@link String}s.
         * @param valueDisplayMap The mapping between {@link String}s and their associated values.
         * @return The {@link StringConverter} representing the 1:1 mapping.
         */
        protected static <T> StringConverter<T> createStringConverter(BiMap<T, String> valueDisplayMap) {
            return new StringConverter<T>() {
                @Override
                public String toString(T value) {
                    if (valueDisplayMap.containsKey(value)) {
                        return valueDisplayMap.get(value);
                    } else {
                        throw new NoSuchElementException("There is not display name for " + value);
                    }
                }

                @Override
                public T fromString(String displayName) {
                    if (valueDisplayMap.containsValue(displayName)) {
                        return valueDisplayMap.inverse().get(displayName);
                    } else {
                        throw new NoSuchElementException("There is no value for display name " + displayName);
                    }
                }
            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ReadOnlyBooleanProperty checkedProperty() {
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
         * Returns the property holding whether the input field is empty.
         *
         * @return Holds {@code true} only if the input field is empty.
         */
        public ReadOnlyBooleanProperty emptyProperty() {
            return empty;
        }

        /**
         * Checks whether the input field is empty.
         *
         * @return {@code true} only if the input field is empty.
         */
        public boolean isEmpty() {
            return emptyProperty().get();
        }

        /**
         * Binds the {@code emptyProperty} to another value. Only if {@code obs} holds {@code true} the input is empty.
         *
         * @param obs The value to bind the {@code emptyProperty} to.
         * @see #emptyProperty()
         */
        protected void bindEmptyProperty(ObservableValue<? extends Boolean> obs) {
            empty.bind(obs);
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

        /**
         * Adds a valid condition. Only if the already added conditions and this condition hold {@code true} the input
         * is valid.
         *
         * @param condition The addition to add for validation.
         */
        protected void addValidCondition(ObservableBooleanValue condition) {
            validConditions.get().add(condition);
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code BOOLEAN}.
     */
    private static class BooleanConditionField extends CheckedConditionField<Boolean> {

        @FXML
        private CheckBox checkbox;

        BooleanConditionField(Pair<String, Class<Boolean>> column) {
            super(column);
        }

        @Override
        protected void initializeImpl() {
            bindEmptyProperty(checkbox.indeterminateProperty());
        }

        @Override
        protected String getFxmlFileName() {
            return "BooleanConditionField.fxml";
        }

        @Override
        protected Optional<String> getConditionImpl() {
            String condition;
            if (checkbox.isIndeterminate()) {
                condition = null;
            } else {
                condition = " = " + ColumnParser.BOOLEAN_COLUMN_PARSER.toString(checkbox.isSelected());
            }
            return Optional.ofNullable(condition);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            checkbox.selectedProperty().addListener(listener);
            checkbox.indeterminateProperty().addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            checkbox.selectedProperty().removeListener(listener);
            checkbox.indeterminateProperty().removeListener(listener);
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code VARCHAR}.
     */
    private static class StringConditionField extends CheckedConditionField<String> {

        @FXML
        private CheckedTextField inputField;
        @FXML
        private ComboBox<Pair<String, String>> compareMode;

        StringConditionField(Pair<String, Class<String>> column) {
            super(column);
        }

        @Override
        protected void initializeImpl() {
            GridPane.setHgrow(inputField, Priority.ALWAYS);
            bindEmptyProperty(inputField.emptyProperty());
            addValidCondition(inputField.validProperty());
            inputField.checkedProperty().bind(checkedProperty());
            final BiMap<Pair<String, String>, String> valueDisplayMap = HashBiMap.create(Map.of(
                    new Pair<>("", ""), EnvironmentHandler.getResourceValue("exactlyMatches"),
                    new Pair<>("%", "%"), EnvironmentHandler.getResourceValue("contains")
            ));
            compareMode.setConverter(createStringConverter(valueDisplayMap));
            compareMode.setItems(FXCollections.observableArrayList(valueDisplayMap.keySet()));
            compareMode.getSelectionModel().select(new Pair<>("%", "%"));
        }

        @Override
        protected String getFxmlFileName() {
            return "StringConditionField.fxml";
        }

        @Override
        protected Optional<String> getConditionImpl() {
            Pair<String, String> mode = compareMode.getValue();
            return Optional.of("LIKE " + ColumnParser.STRING_COLUMN_PARSER.toString(mode.getKey()
                    + inputField.getText() + mode.getValue()));
        }

        @Override
        public void addListener(InvalidationListener listener) {
            compareMode.valueProperty().addListener(listener);
            inputField.textProperty().addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            compareMode.valueProperty().removeListener(listener);
            inputField.textProperty().removeListener(listener);
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns which have types that may be controlled by a
     * spinner.
     */
    private abstract static class SpinnerConditionField<T extends Number> extends CheckedConditionField<T> {

        @FXML
        private CheckedSpinner<T> spinner;
        @FXML
        private ComboBox<String> compareSymbol;

        SpinnerConditionField(Pair<String, Class<T>> column) {
            super(column);
        }

        @Override
        protected void initializeImpl() {
            GridPane.setHgrow(spinner, Priority.ALWAYS);
            bindEmptyProperty(spinner.getEditor().textProperty().isEmpty());
            addValidCondition(spinner.validProperty());
            spinner.checkedProperty().bind(checkedProperty());
            spinner.setEditable(true);
            spinner.getEditor().setText("");
            compareSymbol.setItems(FXCollections.observableArrayList("<", "<=", "=", ">=", ">"));
            compareSymbol.getSelectionModel().select("=");
        }

        /**
         * Converts a value to its {@link String} representation which can be used within the condition.
         *
         * @param value The value to get a representation usable within the condition.
         * @return The representation which can be used within the condition
         */
        protected abstract String convert(T value);

        @Override
        protected Optional<String> getConditionImpl() {
            return Optional.of(compareSymbol.getValue() + " " + convert(spinner.getValue()));
        }

        @Override
        public void addListener(InvalidationListener listener) {
            spinner.valueProperty().addListener(listener);
            compareSymbol.valueProperty().addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            spinner.valueProperty().removeListener(listener);
            compareSymbol.valueProperty().removeListener(listener);
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code INTEGER}.
     */
    private static class IntegerConditionField extends SpinnerConditionField<Integer> {

        IntegerConditionField(Pair<String, Class<Integer>> column) {
            super(column);
        }

        @Override
        protected String convert(Integer value) {
            return ColumnParser.INTEGER_COLUMN_PARSER.toString(value);
        }

        @Override
        protected String getFxmlFileName() {
            return "IntegerConditionField.fxml";
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code DOUBLE}.
     */
    private static class DoubleConditionField extends SpinnerConditionField<Double> {

        DoubleConditionField(Pair<String, Class<Double>> column) {
            super(column);
        }

        @Override
        protected String convert(Double value) {
            return ColumnParser.DOUBLE_COLUMN_PARSER.toString(value);
        }

        @Override
        protected String getFxmlFileName() {
            return "DoubleConditionField.fxml";
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code DATE}.
     */
    private static class LocalDateConditionField extends CheckedConditionField<LocalDate> {

        @FXML
        private ComboBox<String> compareMode;
        @FXML
        private CheckedDatePicker datePicker;

        LocalDateConditionField(Pair<String, Class<LocalDate>> column) {
            super(column);
        }

        @Override
        protected void initializeImpl() {
            bindEmptyProperty(datePicker.emptyProperty());
            addValidCondition(datePicker.validProperty());
            datePicker.checkedProperty().bind(checkedProperty());
            final BiMap<String, String> valueDisplayMap = HashBiMap.create(Map.of(
                    "<", EnvironmentHandler.getResourceValue("beforeDate"),
                    "=", EnvironmentHandler.getResourceValue("atDate"),
                    ">", EnvironmentHandler.getResourceValue("afterDate")
            ));
            compareMode.setConverter(createStringConverter(valueDisplayMap));
            compareMode.setItems(FXCollections.observableArrayList(valueDisplayMap.keySet()));
            compareMode.getSelectionModel().select("=");
        }

        @Override
        protected String getFxmlFileName() {
            return "LocalDateConditionField.fxml";
        }

        @Override
        protected Optional<String> getConditionImpl() {
            return Optional.of(compareMode.getValue() + " "
                    + ColumnParser.LOCALDATE_COLUMN_PARSER.toString(datePicker.getValue()));
        }

        @Override
        public void addListener(InvalidationListener listener) {
            compareMode.valueProperty().addListener(listener);
            datePicker.valueProperty().addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            compareMode.valueProperty().removeListener(listener);
            datePicker.valueProperty().removeListener(listener);
        }
    }
}
