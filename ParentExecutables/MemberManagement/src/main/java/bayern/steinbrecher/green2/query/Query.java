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

import bayern.steinbrecher.green2.View;
import bayern.steinbrecher.green2.ViewStartException;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a form for querying member.
 *
 * @author Stefan Huber
 */
public class Query extends View<QueryController> {

    private final DBConnection dbConnection;

    /**
     * Creates a new query dialog which uses the given {@link DBConnection}.
     *
     * @param dbConnection The connection to use for queries.
     */
    public Query(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void callWhenLoadFXML() {
        getController().setDbConnection(dbConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startImpl(Stage stage) {
        Parent root;
        try {
            root = loadFXML("Query.fxml");
        } catch (IOException ex) {
            throw new ViewStartException(ex);
        }
        getController().setStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle(EnvironmentHandler.getResourceValue("queryMemberTitle"));
        stage.setResizable(false);
    }
}
