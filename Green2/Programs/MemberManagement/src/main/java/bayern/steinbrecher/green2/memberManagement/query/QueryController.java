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
import bayern.steinbrecher.dbConnector.DBConnection.Table;
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
import javafx.beans.binding.BooleanBinding;
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

import java.time.LocalDate;
import java.util.Comparator;
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
    private final ObjectProperty<DBConnection> dbConnection = new SimpleObjectProperty<>(this, "dbConnection");
    private final ObjectProperty<Optional<List<List<String>>>> lastQueryResult
            = new SimpleObjectProperty<>(Optional.empty());
    private boolean isLastQueryUpToDate;

    //TODO Is there any way to connect these question marks?
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
                .sorted(Comparator.comparingInt(Column::getIndex))
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
                            .addListener(invLis -> isLastQueryUpToDate = false);
                    conditionFields.add(conditionField.get());
                    ObservableList<Node> children = conditionField.get()
                            .getChildren();
                    Node[] conditionFieldChildren = children.toArray(new Node[0]); //NOPMD
                    conditionFieldLengths.add(conditionFieldChildren.length);
                    queryInput.addRow(rowCounter, conditionFieldChildren);
                } else {
                    LOGGER.log(Level.WARNING, "The type {0} of column {1} is not supported by the query dialog.",
                            new Object[]{column.getColumnType(), column.getName()});
                }
            }
            if (conditionFieldLengths.size() > 1) { // NOPMD - Containing more than a single element indicates a problem
                String lengths = conditionFieldLengths.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                LOGGER.log(Level.INFO, "The condition fields of the query dialog do not have the same length. "
                        + "The lengths include: {0}", lengths);
            }
        }
        isLastQueryUpToDate = false;
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
        });
        tableSelection.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (oldVal != newVal) {
                        try {
                            generateQueryInterface(getDbConnection(), newVal);
                        } catch (QueryFailedException ex) {
                            LOGGER.log(Level.SEVERE, "Could not generate query interface", ex);
                        }
                    }
                });
        conditionFields.addListener((obs, oldVal, newVal) -> {
            newVal.addListener((ListChangeListener.Change<? extends CheckedConditionField<?>> change) -> {
                while (change.next()) {
                    change.getAddedSubList()
                            .forEach(ccf -> ccf.addListener(invalidObs -> isLastQueryUpToDate = false));
                }
            });
            BooleanBinding allConditionFieldsValid
                    = BindingUtility.reduceAnd(newVal.stream().map(CheckedConditionField::validProperty));
            BooleanBinding isAnyTableSelected = tableSelection.getSelectionModel()
                    .selectedItemProperty()
                    .isNotNull();
            bindValidProperty(
                    allConditionFieldsValid
                            .and(isAnyTableSelected)
            );
            // FIXME 2021-02-20: validProperty() listener is called very often (for every element separately)
        });
    }

    private synchronized void updateLastQueryResult() throws GenerationFailedException, QueryFailedException {
        if (!isLastQueryUpToDate) {
            List<QueryCondition<?>> conditions = conditionFields.stream()
                    .map(CheckedConditionField::generateCondition)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            Table<?, ?> memberTable = getDbConnection().getTable(Tables.MEMBER)
                    .orElseThrow();
            String searchQuery = getDbConnection()
                    .getDbms()
                    .getQueryGenerator()
                    .generateSearchQueryStatement(
                            getDbConnection().getDatabaseName(), memberTable, memberTable.getColumns(), conditions);
            lastQueryResult.set(Optional.of(getDbConnection().execQuery(searchQuery)));
            isLastQueryUpToDate = true;
        }
    }

    @Override
    protected Optional<List<List<String>>> calculateResult() {
        try {
            updateLastQueryResult();
        } catch (GenerationFailedException | QueryFailedException ex) {
            LOGGER.log(Level.SEVERE, "Failed to update cached query result", ex);
        }
        return lastQueryResult.get();
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

    /**
     * Represents specialized fields for querying certain types of columns of a database table.
     *
     * @param <T> The type of the column to query.
     */
    private abstract static class CheckedConditionField<T> extends HBox implements CheckedControl, Observable {

        private final CheckableControlBase<CheckedConditionField<T>> ccBase = new CheckableControlBase<>(this);
        private final BooleanProperty empty = new SimpleBooleanProperty(true);
        private final Column<T> column;
        private final QueryGenerator queryGenerator;

        /**
         * Creates a new {@link CheckedConditionField} for the given column and its appropriate type.
         *
         * @param column The column to create an input field for.
         */
        CheckedConditionField(Column<T> column, QueryGenerator queryGenerator) {
            super();
            this.column = column;
            this.queryGenerator = queryGenerator;

            ccBase.checkedProperty().bind(emptyProperty().not());
            getChildren().add(new Label(column.getName()));
        }

        /**
         * @since 2u14
         */
        protected abstract Optional<QueryCondition<T>> generateConditionImpl();

        /**
         * @since 2u14
         */
        public final Optional<QueryCondition<T>> generateCondition() {
            Optional<QueryCondition<T>> condition;
            if (isValid() && !isEmpty()) {
                condition = generateConditionImpl();
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

        public Column<T> getColumn() {
            return column;
        }

        /**
         * @since 2u14
         */
        protected QueryGenerator getQueryGenerator() {
            return queryGenerator;
        }

        @Override
        public ObservableList<ReportEntry> getReports() {
            return ccBase.getReports();
        }

        @Override
        public boolean addReport(ReportEntry report) {
            return ccBase.addReport(report);
        }

        @Override
        public ReadOnlyBooleanProperty checkedProperty() {
            return ccBase.checkedProperty();
        }

        @Override
        public boolean isChecked() {
            return ccBase.isChecked();
        }

        public ReadOnlyBooleanProperty emptyProperty() {
            return empty;
        }

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

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return ccBase.validProperty();
        }

        @Override
        public boolean isValid() {
            return validProperty().get();
        }

        @Override
        public boolean addValidityConstraint(ObservableBooleanValue constraint) {
            return ccBase.addValidityConstraint(constraint);
        }
    }

    /**
     * Represents a {@link CheckedConditionField} for querying columns of type {@link Boolean}.
     */
    private static class BooleanConditionField extends CheckedConditionField<Boolean> {

        private final CheckBox checkbox = new CheckBox() {{
            setAllowIndeterminate(true);
            setIndeterminate(true);
        }};

        BooleanConditionField(Column<Boolean> column, QueryGenerator queryGenerator) {
            super(column, queryGenerator);

            bindEmptyProperty(checkbox.indeterminateProperty());
            addValidityConstraint(checkbox.indeterminateProperty().not());

            getChildren().add(new HBox());
            getChildren().add(new HBox());
            getChildren().add(checkbox);
        }

        @Override
        protected Optional<QueryCondition<Boolean>> generateConditionImpl() {
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

        private static final BiMap<QueryOperator<String>, String> valueDisplayMap = HashBiMap.create(Map.of(
                QueryOperator.LIKE, EnvironmentHandler.getResourceValue("exactlyMatches"),
                QueryOperator.CONTAINS, EnvironmentHandler.getResourceValue("contains")
        ));
        private static final StringConverter<QueryOperator<String>> compareModeConverter
                = createStringConverter(valueDisplayMap);
        private final CheckedTextField inputField = new CheckedTextField();
        private final ComboBox<QueryOperator<String>> compareMode
                = new ComboBox<>(FXCollections.observableArrayList(valueDisplayMap.keySet()));

        StringConditionField(Column<String> column, QueryGenerator queryGenerator) {
            super(column, queryGenerator);

            compareMode.setConverter(compareModeConverter);
            compareMode.getSelectionModel()
                    .select(QueryOperator.CONTAINS);
            GridPane.setHgrow(inputField, Priority.ALWAYS);

            bindEmptyProperty(inputField.emptyProperty());
            inputField.checkedProperty()
                    .bind(checkedProperty());
            addValidityConstraint(inputField.validProperty());

            getChildren().add(new HelpButton(EnvironmentHandler.getResourceValue("sqlWildcardsHelp")));
            getChildren().add(compareMode);
            getChildren().add(inputField);
        }

        @Override
        protected Optional<QueryCondition<String>> generateConditionImpl() {
            return Optional.of(
                    compareMode.getValue()
                            .generateCondition(getQueryGenerator(), getColumn(), inputField.getText()));
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

        private final CheckedSpinner<T> spinner;
        private final ComboBox<QueryOperator<T>> compareSymbol;

        SpinnerConditionField(Column<T> column, QueryGenerator queryGenerator,
                              BiMap<QueryOperator<T>, String> valueDisplayMap, CheckedSpinner<T> spinner) {
            super(column, queryGenerator);
            this.spinner = spinner;
            compareSymbol = new ComboBox<>(FXCollections.observableArrayList(valueDisplayMap.keySet()));

            spinner.setEditable(true);
            spinner.getEditor()
                    .setText("");
            compareSymbol.setConverter(createStringConverter(valueDisplayMap));
            compareSymbol.getSelectionModel()
                    .selectFirst();
            GridPane.setHgrow(spinner, Priority.ALWAYS);

            bindEmptyProperty(spinner.getEditor().textProperty().isEmpty());
            spinner.checkedProperty()
                    .bind(checkedProperty());
            addValidityConstraint(spinner.validProperty());

            getChildren().add(new HBox());
            getChildren().add(compareSymbol);
            getChildren().add(spinner);
        }

        @Override
        protected Optional<QueryCondition<T>> generateConditionImpl() {
            return Optional.of(
                    compareSymbol.getValue()
                            .generateCondition(getQueryGenerator(), getColumn(), spinner.getValue())
            );
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
        private final ComboBox<QueryOperator<LocalDate>> compareMode
                = new ComboBox<>(FXCollections.observableArrayList(valueDisplayMap.keySet()));
        private final CheckedDatePicker datePicker = new CheckedDatePicker();

        LocalDateConditionField(Column<LocalDate> column, QueryGenerator queryGenerator) {
            super(column, queryGenerator);

            compareMode.setConverter(compareModeConverter);
            compareMode.getSelectionModel()
                    .select(QueryOperator.IS_AT_DATE);

            bindEmptyProperty(datePicker.emptyProperty());
            datePicker.checkedProperty()
                    .bind(checkedProperty());
            addValidityConstraint(datePicker.validProperty());

            getChildren().add(new HBox());
            getChildren().add(compareMode);
            getChildren().add(datePicker);
        }

        @Override
        protected Optional<QueryCondition<LocalDate>> generateConditionImpl() {
            return Optional.of(
                    compareMode.getValue()
                            .generateCondition(getQueryGenerator(), getColumn(), datePicker.getValue())
            );
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
