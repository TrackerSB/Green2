package bayern.steinbrecher.gruen2.login.standard;

import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.elements.CheckedPasswordField;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.login.LoginController;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

/**
 * Controller of DefaultLogin.fxml.
 *
 * @author Stefan Huber
 */
public class DefaultLoginController extends LoginController {

    @FXML
    private CheckedTextField databaseUsernameField;
    @FXML
    private CheckedPasswordField databasePasswordField;
    @FXML
    private Button loginButton;
    @FXML
    private CheckBox allowEmptyFieldsCheckbox;
    @FXML
    private Label invalidInput;
    private List<CheckedTextField> textInputFields;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textInputFields
                = Arrays.asList(databaseUsernameField, databasePasswordField);

        allowEmptyFieldsCheckbox.selectedProperty()
                .addListener((obs, oldVal, newVal) -> {
                    textInputFields.forEach(f -> f.setChecked(!newVal));
                });
        ChangeListener<Boolean> cl = (obs, oldVal, newVal) -> {
            boolean isAllInputValid = textInputFields.parallelStream()
                    .allMatch(CheckedTextField::isValid);
            loginButton.setDisable(!isAllInputValid);
            invalidInput.setVisible(!isAllInputValid);
        };
        textInputFields.forEach(f -> f.validProperty().addListener(cl));
    }

    /**
     * Closes the stage only if the inserted information is valid.
     */
    @FXML
    private void login() {
        checkStage();
        if (!loginButton.isDisabled()) {
            userConfirmed = true;
            stage.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        Map<LoginKey, String> loginInfo = new HashMap<>(2);
        loginInfo.put(LoginKey.DATABASE_USERNAME,
                databaseUsernameField.getText());
        loginInfo.put(LoginKey.DATABASE_PASSWORD,
                databasePasswordField.getText());
        if (userConfirmed) {
            return Optional.of(loginInfo);
        }
        return Optional.empty();
    }
}
