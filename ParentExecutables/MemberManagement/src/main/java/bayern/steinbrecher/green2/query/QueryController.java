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
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import java.sql.SQLException;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import bayern.steinbrecher.green2.elements.CheckedControl;
import bayern.steinbrecher.green2.elements.CheckableControlBase;
import bayern.steinbrecher.green2.elements.buttons.HelpButton;
import bayern.steinbrecher.green2.elements.report.ReportEntry;
import bayern.steinbrecher.green2.elements.spinner.CheckedDoubleSpinner;
import bayern.steinbrecher.green2.elements.spinner.CheckedIntegerSpinner;
import java.util.HashSet;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import java.util.Set;

/**
 * Represents the controller of the dialog for querying member.
 *
 * @author Stefan Huber
 */
public class QueryController extends WizardableController<Optional<List<List<String>>>> {

    private static final Logger LOGGER = Logger.getLogger(QueryController.class.getName());
    @FXML
    private GridPane queryInput;
    @FXML
    private ComboBox<Tables<?, ?>> tableSelection;
    private final ListProperty<CheckedConditionField<?>> conditionFields
            = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BooleanProperty allConditionFieldsValid = new SimpleBooleanProperty(this, "allConditionFieldsValid");
    private final BooleanProperty isNoConditionFieldEmpty = new SimpleBooleanProperty(this, "isNoConditionFieldEmpty");
    private final BooleanProperty isAnyTableSelected = new SimpleBooleanProperty(this, "isAnyTableSelected");
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

    private void generateQueryInterface(DBConnection connection, Tables<?, ?> table) {
        queryInput.getChildren().clear();
        conditionFields.clear();

        List<Pair<String, Class<?>>> sortedColumns = connection.getAllColumns(table).stream()
                .sorted((c1, c2) -> c1.getKey().compareToIgnoreCase(c2.getKey()))
                .collect(Collectors.toList());
        if (sortedColumns.isEmpty()) {
            throw new IllegalStateException(
                    "The query dialog can not be opened since it can not show any column to query.");
        } else {
            Set<Integer> conditionFieldLengths = new HashSet<>();
            for (int rowCounter = 0; rowCounter < sortedColumns.size(); rowCounter++) {
                Pair<String, Class<?>> column = sortedColumns.get(rowCounter);
                Optional<CheckedConditionField<?>> conditionField = createConditionField(column);
                if (conditionField.isPresent()) {
                    conditionField.get()
                            .addListener(invLis -> isLastQueryUptodate = false);
                    conditionFields.add(conditionField.get());
                    ObservableList<Node> children = conditionField.get()
                            .getChildren();
                    Node[] conditionFieldChildren = children.toArray(new Node[children.size()]); //NOPMD
                    conditionFieldLengths.add(conditionFieldChildren.length);
                    queryInput.addRow(rowCounter, conditionFieldChildren);
                } else {
                    LOGGER.log(Level.WARNING, "The type {0} of column {1} is not supported by the query dialog.",
                            new Object[]{column.getValue(), column.getKey()});
                }
            }
            if (conditionFieldLengths.size() > 1) {
                String lengths = conditionFieldLengths.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                LOGGER.log(Level.INFO, "The condition fields of the query dialog do not have the same length. "
                        + "The lengths include: {0}", lengths);
            }
        }
        isLastQueryUptodate = false;
        updateLastQueryResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbConnection.addListener((obs, oldVal, newVal) -> {
            ObservableList<Tables<?, ?>> items = FXCollections.observableArrayList();
            items.addAll(Tables.values());
            tableSelection.itemsProperty()
                    .setValue(items);
            tableSelection.getSelectionModel()
                    .select(Tables.MEMBER);
            generateQueryInterface(newVal, tableSelection.getSelectionModel().getSelectedItem());
        });
        tableSelection.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> generateQueryInterface(getDbConnection(), newVal));
        isAnyTableSelected.bind(
                tableSelection.getSelectionModel()
                        .selectedItemProperty()
                        .isNotNull()
        );
        conditionFields.addListener((obs, oldVal, newVal) -> {
            newVal.addListener((ListChangeListener.Change<? extends CheckedConditionField<?>> change) -> {
                while (change.next()) {
                    change.getAddedSubList()
                            .forEach(ccf -> ccf.addListener(invalidObs -> isLastQueryUptodate = false));
                }
            });
            allConditionFieldsValid.bind(
                    BindingUtility.reduceAnd(newVal.stream().map(CheckedConditionField::validProperty))
            );
            isNoConditionFieldEmpty.bind(
                    BindingUtility.reduceAnd(newVal.stream().map(CheckedConditionField::emptyProperty))
                            .not()
            );
            bindValidProperty(
                    allConditionFieldsValid
                            .and(isNoConditionFieldEmpty)
                            .and(isAnyTableSelected)
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
            List<String> columns = conditionFields.stream()
                    .filter(CheckedConditionField::isSelected)
                    .map(CheckedConditionField::getRealColumnName)
                    .collect(Collectors.toList());
            Optional<String> searchQuery = dbConnectionProperty().get()
                    .generateSearchQuery(Tables.MEMBER, columns, conditions);
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
            implements CheckedControl, Observable {

        private static final Logger LOGGER = Logger.getLogger(CheckedConditionField.class.getName());
        private final CheckableControlBase<CheckedConditionField<T>> ccBase = new CheckableControlBase<>(this);
        private final BooleanProperty empty = new SimpleBooleanProperty(this, "empty", false);
        private final String realColumnName;
        private final CheckBox selectColumn;
        private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper(this, "selected", true);

        /**
         * Creates a new {@link CheckedConditionField} for the given column and its appropriate type.
         *
         * @param column The column to create an input field for.
         */
        CheckedConditionField(Pair<String, Class<T>> column) {
            super();
            initialize();
            realColumnName = column.getKey();
            selectColumn = new CheckBox();
            selectColumn.setSelected(true);
            selected.bind(selectColumn.selectedProperty());
            getChildren().add(selectColumn);
            getChildren().add(new Label(realColumnName));
            getChildren().addAll(generateChildren());
        }

        /**
         * Returns the actual children representing the {@link CheckedConditionField}. NOTE: All nodes should to be
         * initialized within this method since it may be called before any class variables of the implementing subclass
         * is initialized.
         *
         * @return The actual children representing the {@link CheckedConditionField}.
         */
        protected abstract List<Node> generateChildren();

        /**
         * This method may be overridden in order to add further calls to {@link #initialize()}.
         */
        @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
        protected void initializeImpl() {
            //No-op
        }

        /**
         * Initializes properties, bindings and should be used for instanziating class variables representing nodes.
         * This method is the first call within {@link #CheckedConditionField(javafx.util.Pair)}.
         */
        public final void initialize() {
            ccBase.checkedProperty().bind(emptyProperty().not());
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
         * Adds listener additionally to {@link #addListener(javafx.beans.InvalidationListener)}.
         *
         * @param listener The listener to add.
         */
        protected abstract void addListenerImpl(InvalidationListener listener);

        /**
         * {@inheritDoc}
         */
        @Override
        public final void addListener(InvalidationListener listener) {
            selectColumn.selectedProperty()
                    .addListener(listener);
            addListenerImpl(listener);
        }

        /**
         * Returns the column name this {@link CheckedConditionField} represents.
         *
         * @return The column name this {@link CheckedConditionField} represents.
         */
        public String getRealColumnName() {
            return realColumnName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ObservableList<ReportEntry> getReports() {
            return ccBase.getReports();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addReport(ReportEntry report) {
            return ccBase.addReport(report);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BooleanProperty checkedProperty() {
            return ccBase.checkedProperty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isChecked() {
            return ccBase.isChecked();
        }

        /**
         * Returns the property holding whether this column has to be shown in the output result.
         *
         * @return The property holding whether this column has to be shown in the output result.
         */
        public ReadOnlyBooleanProperty selectedProperty() {
            return selected.getReadOnlyProperty();
        }

        /**
         * Checks whether this column has to be shown in the output result.
         *
         * @return {@code true} only if if this column has to be shown in the output result.
         */
        public boolean isSelected() {
            return selectedProperty().get();
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
            return ccBase.validProperty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid() {
            return validProperty().get();
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code BOOLEAN}.
     */
    private static class BooleanConditionField extends CheckedConditionField<Boolean> {

        private CheckBox checkbox;

        BooleanConditionField(Pair<String, Class<Boolean>> column) {
            super(column);
        }

        @Override
        protected void initializeImpl() {
            checkbox = new CheckBox();
            checkbox.setAllowIndeterminate(true);
            checkbox.setIndeterminate(true);
            bindEmptyProperty(checkbox.indeterminateProperty());
        }

        @Override
        protected List<Node> generateChildren() {
            return List.of(new HBox(), new HBox(), checkbox);
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
        public void addListenerImpl(InvalidationListener listener) {
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

        private CheckedTextField inputField;
        private BiMap<Pair<String, String>, String> valueDisplayMap;
        private ComboBox<Pair<String, String>> compareMode;

        StringConditionField(Pair<String, Class<String>> column) {
            super(column);
        }

        @Override
        protected void initializeImpl() {
            inputField = new CheckedTextField();
            inputField.checkedProperty().bind(checkedProperty());
            valueDisplayMap = HashBiMap.create(Map.of(
                    new Pair<>("", ""), EnvironmentHandler.getResourceValue("exactlyMatches"),
                    new Pair<>("%", "%"), EnvironmentHandler.getResourceValue("contains")
            ));
            compareMode = new ComboBox<>(FXCollections.observableArrayList(valueDisplayMap.keySet()));
            compareMode.setConverter(createStringConverter(valueDisplayMap));
            compareMode.getSelectionModel().select(new Pair<>("%", "%"));

            GridPane.setHgrow(inputField, Priority.ALWAYS);
            bindEmptyProperty(inputField.emptyProperty());
            addReports(inputField);
        }

        @Override
        protected List<Node> generateChildren() {
            return List.of(
                    new HelpButton(EnvironmentHandler.getResourceValue("sqlWildcardsHelp")), compareMode, inputField);
        }

        @Override
        protected Optional<String> getConditionImpl() {
            Pair<String, String> mode = compareMode.getValue();
            return Optional.of("LIKE " + ColumnParser.STRING_COLUMN_PARSER.toString(mode.getKey()
                    + inputField.getText() + mode.getValue()));
        }

        @Override
        public void addListenerImpl(InvalidationListener listener) {
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

        private CheckedSpinner<T> spinner;
        private ComboBox<String> compareSymbol;

        SpinnerConditionField(Pair<String, Class<T>> column) {
            super(column);
        }

        protected abstract CheckedSpinner<T> getSpinner();

        @Override
        protected void initializeImpl() {
            spinner = getSpinner();
            spinner.checkedProperty().bind(checkedProperty());
            spinner.setEditable(true);
            spinner.getEditor().setText("");
            compareSymbol = new ComboBox<>(FXCollections.observableArrayList("<", "<=", "=", ">=", ">"));
            compareSymbol.getSelectionModel().select("=");
            GridPane.setHgrow(spinner, Priority.ALWAYS);
            bindEmptyProperty(spinner.getEditor().textProperty().isEmpty());
            addReports(spinner);
        }

        /**
         * Converts a value to its {@link String} representation which can be used within the condition.
         *
         * @param value The value to get a representation usable within the condition.
         * @return The representation which can be used within the condition
         */
        protected abstract String convert(T value);

        @Override
        protected List<Node> generateChildren() {
            return List.of(new HBox(), compareSymbol, spinner);
        }

        @Override
        protected Optional<String> getConditionImpl() {
            return Optional.of(compareSymbol.getValue() + " " + convert(spinner.getValue()));
        }

        @Override
        public void addListenerImpl(InvalidationListener listener) {
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
        protected CheckedSpinner<Integer> getSpinner() {
            return new CheckedIntegerSpinner(0, 1);
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
        protected CheckedSpinner<Double> getSpinner() {
            return new CheckedDoubleSpinner(0, 1);
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code DATE}.
     */
    private static class LocalDateConditionField extends CheckedConditionField<LocalDate> {

        private BiMap<String, String> valueDisplayMap;
        private ComboBox<String> compareMode;
        private CheckedDatePicker datePicker;

        LocalDateConditionField(Pair<String, Class<LocalDate>> column) {
            super(column);
        }

        @Override
        protected void initializeImpl() {
            valueDisplayMap = HashBiMap.create(Map.of(
                    "<", EnvironmentHandler.getResourceValue("beforeDate"),
                    "=", EnvironmentHandler.getResourceValue("atDate"),
                    ">", EnvironmentHandler.getResourceValue("afterDate")
            ));
            compareMode = new ComboBox<>(FXCollections.observableArrayList(valueDisplayMap.keySet()));
            compareMode.setConverter(createStringConverter(valueDisplayMap));
            compareMode.getSelectionModel().select("=");
            datePicker = new CheckedDatePicker();
            datePicker.checkedProperty().bind(checkedProperty());

            bindEmptyProperty(datePicker.emptyProperty());
            addReports(datePicker);
        }

        @Override
        protected List<Node> generateChildren() {
            return List.of(new HBox(), compareMode, datePicker);
        }

        @Override
        protected Optional<String> getConditionImpl() {
            return Optional.of(compareMode.getValue() + " "
                    + ColumnParser.LOCALDATE_COLUMN_PARSER.toString(datePicker.getValue()));
        }

        @Override
        public void addListenerImpl(InvalidationListener listener) {
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
