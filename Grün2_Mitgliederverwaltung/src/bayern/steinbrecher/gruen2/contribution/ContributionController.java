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
 * Represents a window for inserting a double value.
 *
 * @author Stefan Huber
 */
public class ContributionController extends Controller {

    @FXML
    private Button selectButton;
    @FXML
    private CheckedDoubleSpinner contributionSpinner;
    @FXML
    private Label missingInput;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contributionSpinner.getEditor().setOnAction(aevt -> select());
        contributionSpinner.validProperty().addListener(
                (obs, oldVal, newVal) -> {
            missingInput.setVisible(!newVal);
            selectButton.setDisable(!newVal);
        });
    }

    @FXML
    private void select() {
        if (!selectButton.isDisabled()) {
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
