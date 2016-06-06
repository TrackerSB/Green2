package bayern.steinbrecher.gruen2.login.ssh;

import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.elements.CheckedPasswordField;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.login.LoginController;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

/**
 * The controller for SshLogin.fxml.
 *
 * @author Stefan Huber
 */
public class SshLoginController extends LoginController {

    @FXML
    private CheckedTextField sshUsernameField;
    @FXML
    private CheckedPasswordField sshPasswordField;
    @FXML
    private CheckedTextField databaseUsernameField;
    @FXML
    private CheckedPasswordField databasePasswordField;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initAllValidProperty(sshUsernameField, sshPasswordField,
                databaseUsernameField, databasePasswordField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        if (userConfirmed) {
            Map<LoginKey, String> loginInfo
                    = new HashMap<>(LoginKey.values().length);
            loginInfo.put(LoginKey.DATABASE_USERNAME,
                    databaseUsernameField.getText());
            loginInfo.put(LoginKey.DATABASE_PASSWORD,
                    databasePasswordField.getText());
            loginInfo.put(LoginKey.SSH_USERNAME, sshUsernameField.getText());
            loginInfo.put(LoginKey.SSH_PASSWORD, sshPasswordField.getText());
            return Optional.of(loginInfo);
        }
        return Optional.empty();
    }
}
