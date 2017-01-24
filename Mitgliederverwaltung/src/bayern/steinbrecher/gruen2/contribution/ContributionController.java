/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.contribution;

import bayern.steinbrecher.gruen2.WizardableController;
import bayern.steinbrecher.gruen2.elements.CheckedDoubleSpinner;
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
