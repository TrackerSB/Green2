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
 *
 * @author Stefan Huber
 */
public class QueryResult extends WizardableView<Optional<Void>, QueryResultController> {

    private List<List<String>> queryResult;

    public QueryResult(List<List<String>> queryResult) {
        this.queryResult = queryResult;
    }

    @Override
    protected void callWhenLoadFXML() {
        setQueryResult(queryResult);
    }

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

    public void setQueryResult(List<List<String>> queryResult) {
        getController().setQueryResult(queryResult);
    }

    @Override
    public WizardPage<Optional<Void>> getWizardPage() {
        try {
            Pane root = loadFXML("QueryResult_Wizard.fxml");
            return new WizardPage<>(root, null, false, () -> Optional.empty());
        } catch (IOException ex) {
            Logger.getLogger(QueryResult.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
