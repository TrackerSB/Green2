/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
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
