package bayern.steinbrecher.gruen2.login.standard;

import bayern.steinbrecher.gruen2.data.DataProvider;
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
 * Controller of DefaultLogin.fxml.
 *
 * @author Stefan Huber
 */
public class DefaultLoginController extends LoginController
        implements Initializable {

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
            boolean isAllInputValid = textInputFields.stream()
                    .allMatch(CheckedTextField::isValid);
            loginButton.setDisable(!isAllInputValid);
            invalidInput.setVisible(!isAllInputValid);
        };
        textInputFields.forEach(f -> f.validProperty().addListener(cl));
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

    @Override
    public Map<String, String> getLoginInformation() {
        Map<String, String> loginInfo = new HashMap<>(2);
        loginInfo.put(Login.DATABASE_USERNAME_KEY,
                databaseUsernameField.getText());
        loginInfo.put(Login.DATABASE_PASSWORD_KEY,
                databasePasswordField.getText());
        return loginInfo;
    }
}
