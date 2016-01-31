package bayern.steinbrecher.gruen2.login.ssh;

import bayern.steinbrecher.gruen2.elements.CheckedPasswordField;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.login.Login;
import bayern.steinbrecher.gruen2.login.LoginController;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

/**
 * The controller for SshLogin.fxml.
 *
 * @author Stefan Huber
 */
public class SshLoginController extends LoginController
        implements Initializable {

    @FXML
    private Button loginButton;
    @FXML
    private Label invalidInput;
    @FXML
    private CheckedTextField sshUsernameField;
    @FXML
    private CheckedPasswordField sshPasswordField;
    @FXML
    private CheckedTextField databaseUsernameField;
    @FXML
    private CheckedPasswordField databasePasswordField;
    @FXML
    private CheckBox allowEmptyFieldsCheckbox;
    private List<CheckedTextField> textInputFields;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textInputFields
            = Arrays.asList(sshUsernameField, sshPasswordField,
                    databaseUsernameField, databasePasswordField);
        
        allowEmptyFieldsCheckbox.selectedProperty()
                .addListener((obs, oldVal, newVal) -> {
                    textInputFields.forEach(f -> f.setChecked(!newVal));
                });
        ChangeListener<Boolean> cl = (obs, oldVal, newVal) -> {
            boolean isAllInputValid = textInputFields.stream()
                    .allMatch(CheckedTextField::isValid);
            loginButton.setDisable(!isAllInputValid);
            invalidInput.setVisible(!isAllInputValid);
        };
        textInputFields.forEach(f -> f.validProperty().addListener(cl));
    }

    @Override
    public Map<String, String> getLoginInformation() {
        Map<String, String> loginInfo = new HashMap<>(4);
        loginInfo.put(Login.DATABASE_USERNAME_KEY,
                databaseUsernameField.getText());
        loginInfo.put(Login.DATABASE_PASSWORD_KEY,
                databasePasswordField.getText());
        loginInfo.put(Login.SSH_USERNAME_KEY, sshUsernameField.getText());
        loginInfo.put(Login.SSH_PASSWORD_KEY, sshPasswordField.getText());
        return loginInfo;
    }

    @FXML
    private void login() {
        if (!loginButton.isDisabled()) { //TODO isDisable or isDisabled?
            if (stage == null) {
                throw new IllegalStateException(
                        "Stage is not set. Use setStage().");
            } else {
                stage.close();
            }
        }
    }
}
