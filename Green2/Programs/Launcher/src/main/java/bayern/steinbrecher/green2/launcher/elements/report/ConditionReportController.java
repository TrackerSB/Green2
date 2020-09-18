package bayern.steinbrecher.green2.launcher.elements.report;

import bayern.steinbrecher.checkedElements.CheckedTableView;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.ReportType;
import bayern.steinbrecher.checkedElements.report.Reportable;
import bayern.steinbrecher.checkedElements.report.ReportableBase;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPageController;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The controller of {@link ConditionReport}.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ConditionReportController extends WizardPageController<Optional<Boolean>> {

    private static final Logger LOGGER = Logger.getLogger(ConditionReportController.class.getName());
    private final ObservableValue<ObservableList<Condition>> conditions
            = new SimpleObjectProperty<>(this, "conditions",
            FXCollections.observableArrayList(i -> new Observable[]{i.nameProperty(), i.valueProperty()}));
    @FXML
    private CheckedTableView<Condition> conditionsReport;
    @FXML
    private ResourceBundle resources;

    @FXML
    public void initialize() {
        conditionsReport.itemsProperty().bind(conditions);

        TableColumn<Condition, String> conditionNameColumn
                = new TableColumn<>(resources.getString("condition"));
        conditionNameColumn.setCellValueFactory(item -> item.getValue().nameProperty());
        TableColumn<Condition, Optional<Boolean>> conditionValueColumn
                = new TableColumn<>(resources.getString("result"));
        conditionValueColumn.setCellValueFactory(item -> item.getValue().valueProperty());
        conditionValueColumn.setCellFactory(column -> new TableCell<>() {
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
                    setText(resources.getString(resultTextKey));
                    setGraphic(graphic.getAsImageView());
                }
            }
        });
        conditionsReport.getColumns()
                .setAll(List.of(conditionNameColumn, conditionValueColumn));
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

    @SuppressWarnings("unused")
    @FXML
    private void close() {
        // getStage().close(); // FIXME No-op
    }

    /**
     * Replaces the currently set conditions with the given ones.
     *
     * @param conditions The conditions to replace the current ones with. Values of type {@link Optional#empty()}
     *                   represent skipped conditions.
     */
    public void setConditions(Map<String, Callable<Boolean>> conditions) {
        this.conditions.getValue().clear();
        conditions.entrySet()
                .stream()
                .forEach(entry -> this.conditions.getValue().add(new Condition(entry.getKey(), entry.getValue())));
    }

    /**
     * Represents a condition and whether it is currently fullfilled ({@code true}/{@code false}).
     */
    //FIXME How to make this class static again?
    private /*static*/ class Condition implements Reportable {

        private final ReportableBase<CheckedTableView<Condition>> reportableBase
                = new ReportableBase<>(conditionsReport);
        private final StringProperty name = new SimpleStringProperty(this, "name");
        /**
         * Currently the hold value is not updated automatically.
         */
        private final ObjectProperty<Optional<Boolean>> value = new SimpleObjectProperty<>(this, "value");

        Condition(String name, Callable<Boolean> value) {
            this.name.set(name);
            try {
                this.value.set(Optional.of(value.call()));
            } catch (Exception ex) { //NOPMD - Make sure (re-)evaluation of all conditions continues at any point.
                LOGGER.log(Level.WARNING, "An evaluation of a condition failed. It is skipped.", ex);
                this.value.set(Optional.empty());
            }
            initProperties();
        }

        private void initProperties() {
            addReport(new ReportEntry("skippedConditions", ReportType.WARNING, this.value.isEqualTo(Optional.empty())));
        }

        /**
         * Returns the property holding the name of this {@link Condition}. This property is used for displaying a
         * speaking description to the user.
         *
         * @return The property holding the name of this {@link Condition}.
         */
        public StringProperty nameProperty() {
            return name;
        }

        /**
         * Returns the name of this {@link Condition}. This name is used for displaying a speaking description to the
         * user.
         *
         * @return
         */
        public String getName() {
            return nameProperty().get();
        }

        /**
         * Changes the name of this {@link Condition}.
         *
         * @param name The new name of this {@link Condition}.
         */
        public void setName(String name) {
            nameProperty().set(name);
        }

        /**
         * @return
         */
        public ReadOnlyObjectProperty<Optional<Boolean>> valueProperty() {
            return value;
        }

        /**
         * Checks whether this {@link Condition} is currently fullfilled.
         *
         * @return Returns {@code true} or {@code false} describing whether this {@link Condition} is fullfilled.
         * Returns {@link Optional#empty()} only if it could not be determined.
         */
        public Optional<Boolean> getValue() {
            return valueProperty().get();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ObservableList<ReportEntry> getReports() {
            return reportableBase.getReports();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addReport(ReportEntry report) {
            return reportableBase.addReport(report);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return reportableBase.validProperty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid() {
            return reportableBase.isValid();
        }
    }
}
