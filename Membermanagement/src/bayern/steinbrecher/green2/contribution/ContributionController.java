/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bayern.steinbrecher.green2.contribution;

import bayern.steinbrecher.green2.WizardableController;
import bayern.steinbrecher.green2.elements.CheckedDoubleSpinner;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contains a window for inserting a double value representing a contribution.
 *
 * @author Stefan Huber
 */
public class ContributionController extends WizardableController {

    @FXML
    private CheckedDoubleSpinner contributionSpinner;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contributionSpinner.getEditor().setOnAction(aevt -> select());
        valid.bind(contributionSpinner.validProperty());
    }

    @FXML
    private void select() {
        checkStage();
        if (valid.get()) {
            stage.close();
        }
    }

    /**
     * Returns the currently inserted contribution. Returns
     * {@code Optional.empty} if the user didn´t confirm the contribution yet.
     *
     * @return The currently inserted contribution.
     */
    public Optional<Double> getContribution() {
        if (userAbborted()) {
            return Optional.empty();
        } else {
            return Optional.of(contributionSpinner.getValue());
        }
    }
}
