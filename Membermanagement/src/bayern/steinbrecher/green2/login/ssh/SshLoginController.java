/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.login.ssh;

import bayern.steinbrecher.green2.elements.CheckedPasswordField;
import bayern.steinbrecher.green2.elements.CheckedTextField;
import bayern.steinbrecher.green2.login.LoginController;
import bayern.steinbrecher.green2.login.LoginKey;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

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
        initProperties(sshUsernameField, sshPasswordField, databaseUsernameField, databasePasswordField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        if (userAbborted()) {
            return Optional.empty();
        } else {
            Map<LoginKey, String> loginInfo = new HashMap<>(LoginKey.values().length);
            loginInfo.put(LoginKey.DATABASE_USERNAME, databaseUsernameField.getText());
            loginInfo.put(LoginKey.DATABASE_PASSWORD, databasePasswordField.getText());
            loginInfo.put(LoginKey.SSH_USERNAME, sshUsernameField.getText());
            loginInfo.put(LoginKey.SSH_PASSWORD, sshPasswordField.getText());
            return Optional.of(loginInfo);
        }
    }
}
