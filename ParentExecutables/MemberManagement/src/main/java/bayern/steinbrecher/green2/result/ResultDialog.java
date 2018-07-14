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
package bayern.steinbrecher.green2.result;

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
public class ResultDialog extends WizardableView<Optional<Void>, ResultDialogController> {

    private transient final List<List<String>> results;

    /**
     * Creates a new {@link ResultDialog} initially showing the content of {@code results}. It is assumed that the first
     * line contains the headings for the columns.
     *
     * @param results The content to show initially.
     */
    public ResultDialog(List<List<String>> results) {
        super();
        this.results = results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void callWhenLoadFXML() {
        setResults(results);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startImpl(Stage stage) {
        Parent root;
        try {
            root = loadFXML("ResultDialog.fxml");
        } catch (IOException ex) {
            throw new ViewStartException(ex);
        }

        stage.setScene(new Scene(root));
    }

    /**
     * Sets the results to show.
     *
     * @param results The results to show.
     */
    public void setResults(List<List<String>> results) {
        getController().setResults(results);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWizardFxmlPath() {
        return "Result.fxml";
    }
}
