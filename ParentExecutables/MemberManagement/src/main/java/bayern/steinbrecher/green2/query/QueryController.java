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

import bayern.steinbrecher.green2.CheckedController;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.connection.scheme.Columns;
import bayern.steinbrecher.green2.connection.scheme.Tables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Represents the controller of the dialog for querying member.
 *
 * @author Stefan Huber
 */
public class QueryController extends CheckedController {

    @FXML
    private GridPane queryInput;
    private final ObjectProperty<DBConnection> dbConnection = new SimpleObjectProperty<>(this, "dbConnection");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int rowCounter = 0;
        for (Columns<?> column : Tables.MEMBER.getAllColumns()) {
            Label columnLabel = new Label(column.getRealColumnName());
            Node inputField = null;
            Class<?> columnType = column.getType();
            if (columnType.isAssignableFrom(Boolean.class)) {

            } else {
                Logger.getLogger(QueryController.class.getName())
                        .log(Level.WARNING, "The column {0} has type {1} which is not supported by the query dialog.",
                                new Object[]{column.getRealColumnName(), columnType.toString()});
            }
            if (inputField != null) {
                queryInput.addRow(rowCounter, columnLabel, inputField);
                rowCounter++;
            }
        }
        //TODO Check if row counter is zero.
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void query() {
        if (isValid()) {
            throw new UnsupportedOperationException("Querying itself is not supported yet.");
        }
    }

    public ObjectProperty<DBConnection> dbConnectionProperty() {
        return dbConnection;
    }

    public DBConnection getDbConnection() {
        return dbConnectionProperty().get();
    }

    public void setDbConnection(DBConnection dbConnection) {
        this.dbConnection.set(dbConnection);
    }
}
