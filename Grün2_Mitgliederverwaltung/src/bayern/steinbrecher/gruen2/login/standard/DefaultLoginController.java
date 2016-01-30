package bayern.steinbrecher.gruen2.login.standard;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.elements.CheckedPasswordField;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.login.LoginController;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    @FXML
    private Label nameOrPasswdWrong;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allowEmptyFieldsCheckbox.selectedProperty()
                .addListener((obs, oldVal, newVal) -> {
                    databasePasswordField.setChecked(!newVal);
                    databaseUsernameField.setChecked(!newVal);
                });
        ChangeListener<Boolean> cl = (obs, oldVal, newVal) -> {
            boolean isAllInputValid = databasePasswordField.isValid()
                    && databaseUsernameField.isValid();
            loginButton.setDisable(!isAllInputValid);
            invalidInput.setVisible(!isAllInputValid);
        };
        databasePasswordField.validProperty().addListener(cl);
        databaseUsernameField.validProperty().addListener(cl);
    }

    @FXML
    private void login() {
        if (stage == null) {
            throw new IllegalStateException(
                    "Stage is not set. Use setStage().");
        } else {
            stage.close();
        }
    }

    @Override
    public Map<String, String> getLoginInformation() {
        Map<String, String> loginInfo = new HashMap<>(2);
        loginInfo.put(DataProvider.DATABASE_USERNAME_KEY,
                databaseUsernameField.getText());
        loginInfo.put(DataProvider.DATABASE_PASSWORD_KEY,
                databasePasswordField.getText());
        return loginInfo;
    }
}
