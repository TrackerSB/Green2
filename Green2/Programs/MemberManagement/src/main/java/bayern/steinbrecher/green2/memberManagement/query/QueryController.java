package bayern.steinbrecher.green2.memberManagement.query;

import bayern.steinbrecher.checkedElements.CheckableControlBase;
import bayern.steinbrecher.checkedElements.CheckedControl;
import bayern.steinbrecher.checkedElements.CheckedDatePicker;
import bayern.steinbrecher.checkedElements.buttons.HelpButton;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.spinner.CheckedDoubleSpinner;
import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.checkedElements.spinner.CheckedSpinner;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.DBConnection.Column;
import bayern.steinbrecher.dbConnector.query.GenerationFailedException;
import bayern.steinbrecher.dbConnector.query.QueryCondition;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.QueryGenerator;
import bayern.steinbrecher.dbConnector.query.QueryOperator;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.Tables;
import bayern.steinbrecher.javaUtility.BindingUtility;
import bayern.steinbrecher.wizard.WizardPageController;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents the controller of the dialog for querying member.
 *
 * @author Stefan Huber
 */
public class QueryController extends WizardPageController<Optional<List<List<String>>>> {

    private static final Logger LOGGER = Logger.getLogger(QueryController.class.getName());
    @FXML
    private GridPane queryInput;
    @FXML
    private ComboBox<TableScheme<?, ?>> tableSelection;
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
    private Optional<CheckedConditionField<?>> createConditionField(Column<?> column) {
        CheckedConditionField<?> conditionField;
        //TODO How to avoid isAssignableFrom(...)?
        //TODO How to avoid explicit cast?
        Class<?> columnType = column.getColumnType();
        QueryGenerator queryGenerator = getDbConnection()
                .getDbms()
                .getQueryGenerator();
        if (Boolean.class.isAssignableFrom(columnType)) {
            conditionField = new BooleanConditionField((Column<Boolean>) column, queryGenerator);
        } else if (String.class.isAssignableFrom(columnType)) {
            conditionField = new StringConditionField((Column<String>) column, queryGenerator);
        } else if (Integer.class.isAssignableFrom(columnType)) {
            conditionField = new IntegerConditionField((Column<Integer>) column, queryGenerator);
        } else if (Double.class.isAssignableFrom(columnType)) {
            conditionField = new DoubleConditionField((Column<Double>) column, queryGenerator);
        } else if (LocalDate.class.isAssignableFrom(columnType)) {
            conditionField = new LocalDateConditionField((Column<LocalDate>) column, queryGenerator);
        } else {
            conditionField = null;
        }
        return Optional.ofNullable(conditionField);
    }

    private void generateQueryInterface(DBConnection connection, TableScheme<?, ?> table) throws QueryFailedException {
        queryInput.getChildren().clear();
        conditionFields.clear();

        List<Column<?>> sortedColumns = connection.getAllColumns(table).stream()
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .collect(Collectors.toList());
        if (sortedColumns.isEmpty()) {
            throw new IllegalStateException(
                    "The query dialog can not be opened since it can not show any column to query.");
        } else {
            Set<Integer> conditionFieldLengths = new HashSet<>();
            for (int rowCounter = 0; rowCounter < sortedColumns.size(); rowCounter++) {
                Column<?> column = sortedColumns.get(rowCounter);
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
                            new Object[]{column.getColumnType(), column.getName()});
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
        calculateResult(); // Ensure that the cached query result is initialized
    }

    @FXML
    public void initialize() {
        dbConnection.addListener((obs, oldVal, newVal) -> {
            ObservableList<TableScheme<?, ?>> items = FXCollections.observableArrayList();
            items.addAll(Tables.SCHEMES);
            tableSelection.itemsProperty()
                    .setValue(items);
            tableSelection.getSelectionModel()
                    .select(Tables.MEMBER);
            try {
                generateQueryInterface(newVal, tableSelection.getSelectionModel().getSelectedItem());
            } catch (QueryFailedException ex) {
                LOGGER.log(Level.SEVERE, "Could not generate query interface", ex);
            }
        });
        tableSelection.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    try {
                        generateQueryInterface(getDbConnection(), newVal);
                    } catch (QueryFailedException ex) {
                        LOGGER.log(Level.SEVERE, "Could not generate query interface", ex);
                    }
                });
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

    private synchronized void updateLastQueryResult() throws GenerationFailedException, QueryFailedException {
        if (!isLastQueryUptodate) {
            List<QueryCondition<?>> conditions = conditionFields.stream()
                    .map(CheckedConditionField::getCondition)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            List<Column<?>> columns = conditionFields.stream()
                    .filter(CheckedConditionField::isSelected)
                    .map(CheckedConditionField::getColumn)
                    .collect(Collectors.toList());
            String searchQuery = getDbConnection()
                    .getDbms()
                    .getQueryGenerator()
                    .generateSearchQueryStatement(
                            getDbConnection().getDatabaseName(),
                            getDbConnection().getTable(Tables.MEMBER).orElseThrow(), columns, conditions);
            lastQueryResult.set(Optional.of(getDbConnection().execQuery(searchQuery)));
            isLastQueryUptodate = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<List<List<String>>> calculateResult() {
        try {
            updateLastQueryResult();
        } catch (GenerationFailedException | QueryFailedException ex) {
            LOGGER.log(Level.SEVERE, "Failed to update cached query result", ex);
        }
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
    private abstract static class CheckedConditionField<T> extends HBox implements CheckedControl, Observable {

        private final CheckableControlBase<CheckedConditionField<T>> ccBase = new CheckableControlBase<>(this);
        private final BooleanProperty empty = new SimpleBooleanProperty(this, "empty", false);
        private final Column<T> column;
        private final QueryGenerator queryGenerator;
        private final CheckBox selectColumn;
        private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper(this, "selected", true);

        /**
         * Creates a new {@link CheckedConditionField} for the given column and its appropriate type.
         *
         * @param column The column to create an input field for.
         */
        CheckedConditionField(Column<T> column, QueryGenerator queryGenerator) {
            super();
            initialize();
            this.column = column;
            this.queryGenerator = queryGenerator;
            selectColumn = new CheckBox();
            selectColumn.setSelected(true);
            selected.bind(selectColumn.selectedProperty());
            getChildren().add(selectColumn);
            getChildren().add(new Label(column.getName()));
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
         * This method is the first call within {@link #CheckedConditionField(Column, QueryGenerator)}.
         */
        public final void initialize() {
            ccBase.checkedProperty().bind(emptyProperty().not());
            initializeImpl();
        }

        /**
         * @since 2u14
         */
        protected abstract Optional<QueryCondition<T>> getConditionImpl();

        /**
         * @since 2u14
         */
        public final Optional<QueryCondition<T>> getCondition() {
            Optional<QueryCondition<T>> condition;
            if (isValid() && !isEmpty()) {
                condition = getConditionImpl();
            } else {
                condition = Optional.empty();
            }
            return condition;
        }

        /**
         * Creates a {@link StringConverter} mapping 1:1 a {@link String} displayed to the user and its associated
         * value.
         *
         * @param <T>             The type of the value to associate with {@link String}s.
         * @param valueDisplayMap The mapping between {@link String}s and their associated values.
         * @return The {@link StringConverter} representing the 1:1 mapping.
         */
        protected static <T> StringConverter<T> createStringConverter(BiMap<T, String> valueDisplayMap) {
            return new StringConverter<>() {
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
         * Returns the column this field represents.
         *
         * @return The column this field represents.
         */
        public Column<T> getColumn() {
            return column;
        }

        /**
         * @since 2u14
         */
        protected QueryGenerator getQueryGenerator() {
            return queryGenerator;
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
     * Represents a {@link CheckedConditionField} for querying columns of type {@link Boolean}.
     */
    private static class BooleanConditionField extends CheckedConditionField<Boolean> {

        private CheckBox checkbox;

        BooleanConditionField(Column<Boolean> column, QueryGenerator queryGenerator) {
            super(column, queryGenerator);
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
        protected Optional<QueryCondition<Boolean>> getConditionImpl() {
            QueryCondition<Boolean> condition;
            if (checkbox.isIndeterminate()) {
                condition = null;
            } else if (checkbox.isSelected()) {
                condition = QueryOperator.IS_TRUE.generateCondition(getQueryGenerator(), getColumn());
            } else {
                condition = QueryOperator.IS_FALSE.generateCondition(getQueryGenerator(), getColumn());
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

        private static final BiMap<QueryOperator<String>, String> valueDisplayMap = HashBiMap.create(Map.of(
                QueryOperator.LIKE, EnvironmentHandler.getResourceValue("exactlyMatches"),
                QueryOperator.CONTAINS, EnvironmentHandler.getResourceValue("contains")
        ));
        private static final StringConverter<QueryOperator<String>> compareModeConverter
                = createStringConverter(valueDisplayMap);
        private CheckedTextField inputField;
        private ComboBox<QueryOperator<String>> compareMode;

        StringConditionField(Column<String> column, QueryGenerator queryGenerator) {
            super(column, queryGenerator);
        }

        @Override
        protected void initializeImpl() {
            inputField = new CheckedTextField();
            inputField.checkedProperty().bind(checkedProperty());
            compareMode = new ComboBox<>(FXCollections.observableArrayList(valueDisplayMap.keySet()));
            compareMode.setConverter(compareModeConverter);
            compareMode.getSelectionModel()
                    .select(QueryOperator.CONTAINS);

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
        protected Optional<QueryCondition<String>> getConditionImpl() {
            return Optional.of(
                    compareMode.getValue()
                            .generateCondition(getQueryGenerator(), getColumn(), inputField.getText()));
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

        private final BiMap<QueryOperator<T>, String> valueDisplayMap;
        private final CheckedSpinner<T> spinner;
        private ComboBox<QueryOperator<T>> compareSymbol;

        SpinnerConditionField(Column<T> column, QueryGenerator queryGenerator,
                              BiMap<QueryOperator<T>, String> valueDisplayMap, CheckedSpinner<T> spinner) {
            super(column, queryGenerator);
            this.valueDisplayMap = valueDisplayMap;
            this.spinner = spinner;
        }

        @Override
        protected void initializeImpl() {
            spinner.checkedProperty()
                    .bind(checkedProperty());
            spinner.setEditable(true);
            spinner.getEditor()
                    .setText("");
            compareSymbol = new ComboBox<>(FXCollections.observableArrayList(valueDisplayMap.keySet()));
            compareSymbol.setConverter(createStringConverter(valueDisplayMap));
            compareSymbol.getSelectionModel()
                    .selectFirst();
            GridPane.setHgrow(spinner, Priority.ALWAYS);
            bindEmptyProperty(spinner.getEditor().textProperty().isEmpty());
            addReports(spinner);
        }

        @Override
        protected List<Node> generateChildren() {
            return List.of(new HBox(), compareSymbol, spinner);
        }

        @Override
        protected Optional<QueryCondition<T>> getConditionImpl() {
            return Optional.of(
                    compareSymbol.getValue()
                            .generateCondition(getQueryGenerator(), getColumn(), spinner.getValue())
            );
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

        IntegerConditionField(Column<Integer> column, QueryGenerator queryGenerator) {
            super(column, queryGenerator,
                    HashBiMap.create(Map.of(
                            QueryOperator.IS_EQUAL_I, "=",
                            QueryOperator.IS_SMALLER_I, "<",
                            QueryOperator.IS_SMALLER_EQUAL_I, "<=",
                            QueryOperator.IS_GREATER_EQUAL_I, ">=",
                            QueryOperator.IS_GREATER_I, ">"
                    )),
                    new CheckedIntegerSpinner(0, 1));
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code DOUBLE}.
     */
    private static class DoubleConditionField extends SpinnerConditionField<Double> {

        DoubleConditionField(Column<Double> column, QueryGenerator queryGenerator) {
            super(column, queryGenerator,
                    HashBiMap.create(Map.of(
                            QueryOperator.IS_EQUAL_D, "=",
                            QueryOperator.IS_SMALLER_D, "<",
                            QueryOperator.IS_SMALLER_EQUAL_D, "<=",
                            QueryOperator.IS_GREATER_EQUAL_D, ">=",
                            QueryOperator.IS_GREATER_D, ">"
                    )),
                    new CheckedDoubleSpinner(0, 1));
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@code DATE}.
     */
    private static class LocalDateConditionField extends CheckedConditionField<LocalDate> {

        private static final BiMap<QueryOperator<LocalDate>, String> valueDisplayMap = HashBiMap.create(Map.of(
                QueryOperator.IS_BEFORE_DATE, EnvironmentHandler.getResourceValue("beforeDate"),
                QueryOperator.IS_AT_DATE, EnvironmentHandler.getResourceValue("atDate"),
                QueryOperator.IS_AFTER_DATE, EnvironmentHandler.getResourceValue("afterDate")
        ));
        private static final StringConverter<QueryOperator<LocalDate>> compareModeConverter
                = createStringConverter(valueDisplayMap);
        private ComboBox<QueryOperator<LocalDate>> compareMode;
        private CheckedDatePicker datePicker;

        LocalDateConditionField(Column<LocalDate> column, QueryGenerator queryGenerator) {
            super(column, queryGenerator);
        }

        @Override
        protected void initializeImpl() {
            compareMode = new ComboBox<>(FXCollections.observableArrayList(valueDisplayMap.keySet()));
            compareMode.setConverter(compareModeConverter);
            compareMode.getSelectionModel()
                    .select(QueryOperator.IS_AT_DATE);
            datePicker = new CheckedDatePicker();
            datePicker.checkedProperty()
                    .bind(checkedProperty());

            bindEmptyProperty(datePicker.emptyProperty());
            addReports(datePicker);
        }

        @Override
        protected List<Node> generateChildren() {
            return List.of(new HBox(), compareMode, datePicker);
        }

        @Override
        protected Optional<QueryCondition<LocalDate>> getConditionImpl() {
            return Optional.of(
                    compareMode.getValue()
                            .generateCondition(getQueryGenerator(), getColumn(), datePicker.getValue())
            );
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
