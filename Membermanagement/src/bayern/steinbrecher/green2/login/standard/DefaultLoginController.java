/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */

package bayern.steinbrecher.green2.login.standard;

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
 * Controller of DefaultLogin.fxml.
 *
 * @author Stefan Huber
 */
public class DefaultLoginController extends LoginController {

    @FXML
    private CheckedTextField databaseUsernameField;
    @FXML
    private CheckedPasswordField databasePasswordField;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initProperties(databaseUsernameField, databasePasswordField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        if (userAbborted()) {
            return Optional.empty();
        } else {
            Map<LoginKey, String> loginInfo = new HashMap<>(2);
            loginInfo.put(LoginKey.DATABASE_USERNAME, databaseUsernameField.getText());
            loginInfo.put(LoginKey.DATABASE_PASSWORD, databasePasswordField.getText());
            return Optional.of(loginInfo);
        }
    }
}
