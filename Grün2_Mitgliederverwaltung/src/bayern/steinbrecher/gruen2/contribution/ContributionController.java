package bayern.steinbrecher.gruen2.contribution;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.elements.CheckedDoubleSpinner;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Contains a window for inserting a double value representing a contribution.
 *
 * @author Stefan Huber
 */
public class ContributionController extends Controller {

    @FXML
    private CheckedDoubleSpinner contributionSpinner;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contributionSpinner.getEditor().setOnAction(aevt -> select());
    }

    @FXML
    private void select() {
        checkStage();
        if (contributionSpinner.isValid()) {
            userConfirmed = true;
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
        if (userConfirmed) {
            return Optional.of(contributionSpinner.getValue());
        }
        return Optional.empty();
    }
}
