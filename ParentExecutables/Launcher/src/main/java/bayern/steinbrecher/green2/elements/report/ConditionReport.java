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
package bayern.steinbrecher.green2.elements.report;

import bayern.steinbrecher.green2.ResultView;
import bayern.steinbrecher.green2.ViewStartException;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a dialog showing a list of conditions and whether they are fullfilled.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ConditionReport extends ResultView<Optional<Boolean>, ConditionReportController> {

    private transient final Map<String, Callable<Boolean>> conditions;

    /**
     * Creates a new report showing and evaluating the given conditions.
     *
     * @param conditions The conditions to evaluate and show.
     */
    public ConditionReport(Map<String, Callable<Boolean>> conditions) {
        super();
        this.conditions = conditions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void callWhenLoadFXML() {
        getController().setConditions(conditions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startImpl(Stage stage) {
        Parent root;
        try {
            root = loadFXML("ConditionReport.fxml");
        } catch (IOException ex) {
            throw new ViewStartException(ex);
        }
        getController().setStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle(EnvironmentHandler.getResourceValue("conditionReportTitle"));
    }
}
