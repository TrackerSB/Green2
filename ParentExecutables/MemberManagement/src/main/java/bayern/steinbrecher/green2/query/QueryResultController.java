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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Stefan Huber
 */
public class QueryResultController extends WizardableController {

    @FXML
    private TableView<List<ReadOnlyStringProperty>> queryResultView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        HBox.setHgrow(queryResultView, Priority.ALWAYS);
    }

    public void setQueryResult(List<List<String>> queryResult) {
        queryResultView.getItems().clear();
        queryResultView.getColumns().clear();

        if (queryResult.size() > 0) {
            int numColumns = queryResult.stream()
                    .mapToInt(List::size)
                    .max()
                    .orElse(0);
            List<String> headings = queryResult.get(0);
            for (int i = 0; i < numColumns; i++) {
                String heading = i >= headings.size() ? "" : headings.get(i);
                TableColumn<List<ReadOnlyStringProperty>, String> column = new TableColumn<>(heading);
                final int fixedI = i;
                column.setCellValueFactory(param -> param.getValue().get(fixedI));
                queryResultView.getColumns().add(column);
            }

            if (queryResult.size() > 1) {
                //TODO Is there a way to "collect" to an observable list?
                ObservableList<List<ReadOnlyStringProperty>> items = FXCollections.observableArrayList();
                queryResult.subList(1, queryResult.size()).stream()
                        .map(givenRow -> {
                            List<ReadOnlyStringProperty> itemsRow = new ArrayList<>(numColumns);
                            for (int i = 0; i < numColumns; i++) {
                                String cellValue = i >= givenRow.size() ? "" : givenRow.get(i);
                                itemsRow.add(new SimpleStringProperty(cellValue));
                            }
                            return itemsRow;
                        })
                        .forEach(itemsRow -> items.add(itemsRow));
                queryResultView.setItems(items);
            }
        }
    }
}
