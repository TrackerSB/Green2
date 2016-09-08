package bayern.steinbrecher.gruen2.config_dialog;

import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * @author Stefan Huber
 */
public class Grün2ConfigController implements Initializable {

    private SimpleBooleanProperty anyInputMissing
            = new SimpleBooleanProperty(this, "anyInputMissing");
    private SimpleBooleanProperty valid
            = new SimpleBooleanProperty(this, "valid");
    @FXML
    private CheckedTextField useSSHTextField;
    @FXML
    private CheckedTextField sshHostTextField;
    @FXML
    private CheckedTextField databaseHostTextField;
    @FXML
    private CheckedTextField databaseNameTextField;
    @FXML
    private CheckedTextField birthdayExpressionTextField;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        anyInputMissing.bind(useSSHTextField.emptyProperty()
                .or(sshHostTextField.emptyProperty())
                .or(databaseHostTextField.emptyProperty())
                .or(databaseNameTextField.emptyProperty())
                .or(birthdayExpressionTextField.emptyProperty()));
        valid.bind(anyInputMissing.not());
    }

    /**
     * Returns the property reprsenting a boolean value indicating whether any
     * input is missing.
     *
     * @return The property reprsenting a boolean value indicating whether any
     * input is missing.
     */
    public ReadOnlyBooleanProperty anyInputMissingProperty() {
        return anyInputMissing;
    }

    /**
     * Checks whether any input is missing.
     *
     * @return {@code true} only if any input is missing.
     */
    public boolean isAnyInputMissing() {
        return anyInputMissing.get();
    }

    /**
     * Returns the property reprsenting a boolean value indicating whether all
     * input is valid.
     *
     * @return The property reprsenting a boolean value indicating whether all
     * input is valid.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether all inserted data is valid.
     *
     * @return {@code true} only if all inserted data is valid.
     */
    public boolean isValid() {
        return valid.get();
    }
}
