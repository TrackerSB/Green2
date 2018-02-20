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

import bayern.steinbrecher.green2.ViewStartException;
import bayern.steinbrecher.green2.WizardableView;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Represents a form for querying member.
 *
 * @author Stefan Huber
 */
public class Query extends WizardableView<Optional<List<List<String>>>, QueryController> {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public WizardPage<Optional<List<List<String>>>> getWizardPage() {
        try {
            Pane root = loadFXML("Query_Wizard.fxml");
            return new WizardPage<>(
                    root, null, false, () -> getController().getQueryResult(), getController().validProperty());
        } catch (IOException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
