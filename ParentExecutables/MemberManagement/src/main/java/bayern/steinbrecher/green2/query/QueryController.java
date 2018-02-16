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
import bayern.steinbrecher.green2.elements.spinner.CheckedDoubleSpinner;
import bayern.steinbrecher.green2.elements.spinner.CheckedIntegerSpinner;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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

    private static <T> Optional<CheckedInputField<T, ?>> createInputField(Class<T> columnType) {
        CheckedInputField<T, ?> inputField;
        //FIXME Think about how to solve this in a different way.
        if (columnType.isAssignableFrom(Boolean.class)) {
            inputField = new CheckedInputField<T, CheckBox>() {
                @Override
                protected CheckBox getNodeImpl() {
                    return new CheckBox();
                }

                @Override
                protected Function<CheckBox, T> getNodeValueFunction() {
                    return cb -> (T) (Boolean) cb.isSelected();
                }
            };
        } else if (columnType.isAssignableFrom(String.class)) {
            inputField = new CheckedInputField<T, TextField>() {
                @Override
                protected TextField getNodeImpl() {
                    return new TextField();
                }

                @Override
                protected Function<TextField, T> getNodeValueFunction() {
                    return tf -> (T) tf.getText();
                }
            };
        } else if (columnType.isAssignableFrom(LocalDate.class)) {
            inputField = new CheckedInputField<T, DatePicker>() {
                @Override
                protected DatePicker getNodeImpl() {
                    return new DatePicker();
                }

                @Override
                protected Function<DatePicker, T> getNodeValueFunction() {
                    return dp -> (T) dp.getValue();
                }
            };
        } else if (columnType.isAssignableFrom(Integer.class)) {
            inputField = new CheckedInputField<T, CheckedIntegerSpinner>() {
                @Override
                protected CheckedIntegerSpinner getNodeImpl() {
                    //FIXME Clear text of spinner
                    CheckedIntegerSpinner cis = new CheckedIntegerSpinner(Integer.MIN_VALUE, 0, 1);
                    cis.setEditable(true);
                    cis.getEditor().setText("");
                    return cis;
                }

                @Override
                protected Function<CheckedIntegerSpinner, T> getNodeValueFunction() {
                    return cis -> (T) cis.getValue();
                }
            };
        } else if (columnType.isAssignableFrom(Double.class)) {
            inputField = new CheckedInputField<T, CheckedDoubleSpinner>() {
                @Override
                protected CheckedDoubleSpinner getNodeImpl() {
                    //FIXME Clear text of spinner
                    CheckedDoubleSpinner cds = new CheckedDoubleSpinner(Double.MIN_VALUE, 0, 1);
                    cds.setEditable(true);
                    cds.getEditor().setText("");
                    return cds;
                }

                @Override
                protected Function<CheckedDoubleSpinner, T> getNodeValueFunction() {
                    return cds -> (T) cds.getValue();
                }
            };
        } else {
            inputField = null;
        }
        return Optional.ofNullable(inputField);
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
                Optional<? extends CheckedInputField<?, ?>> inputField = createInputField(column.getValue());
                if (inputField.isPresent()) {
                    queryInput.addRow(rowCounter, columnLabel, inputField.get().getNode());
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
            //TODO Should lastQueryResult be cleared when changing the DBConnection?
        });
    }

    private void updateLastQueryResult() {
        //TODO Determine whether updating lastQueryResult is needed
        //throw new UnsupportedOperationException("Querying itself is not supported yet.");
        lastQueryResult.set(Optional.of(List.of(List.of("Spalte 1", "Spalte 2", "Spalte 3"),
                List.of("Eintrag 1", "Eintrag 2", "Eintrag 3"), List.of("Eintrag 4", "Eintrag 5", "Eintrag 6"))));
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

    //FIXME May force somehow "N extends CheckedControl"
    private static abstract class CheckedInputField<T, N extends Node> {

        private N node = null;

        protected abstract N getNodeImpl();

        public final N getNode() {
            if (node == null) {
                node = getNodeImpl();
            }
            return node;
        }

        protected abstract Function<N, T> getNodeValueFunction();

        public final T getNodeValue() {
            if (node == null) {
                throw new IllegalStateException("No node has been created yet.");
            } else {
                return getNodeValueFunction().apply(node);
            }
        }
    }
}
