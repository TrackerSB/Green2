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

import bayern.steinbrecher.green2.ViewStartException;
import bayern.steinbrecher.green2.WizardableView;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A dialog for displaying tables of {@link String}s representing results of queries.
 *
 * @author Stefan Huber
 */
public class QueryResult extends WizardableView<Optional<Void>, QueryResultController> {

    private final List<List<String>> queryResult;

    /**
     * Creates a new {@link QueryResult} initially showing the content of {@code queryResult}. It is assumed that the
     * first line contains the headings for the columns.
     *
     * @param queryResult The content to show initially.
     */
    public QueryResult(List<List<String>> queryResult) {
        this.queryResult = queryResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void callWhenLoadFXML() {
        setQueryResult(queryResult);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startImpl(Stage stage) {
        Parent root;
        try {
            root = loadFXML("QueryResult.fxml");
        } catch (IOException ex) {
            throw new ViewStartException(ex);
        }

        stage.setScene(new Scene(root));
    }

    /**
     * Sets the query result to show.
     *
     * @param queryResult The query result to show.
     */
    public void setQueryResult(List<List<String>> queryResult) {
        getController().setQueryResult(queryResult);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWizardFxmlPath() {
        return "QueryResult.fxml";
    }
}
