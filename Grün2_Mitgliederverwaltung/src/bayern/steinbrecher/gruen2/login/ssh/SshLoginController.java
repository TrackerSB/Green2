/*
 * Copyright (C) 2016 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.gruen2.login.ssh;

import bayern.steinbrecher.gruen2.login.LoginKey;
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
//TODO don´t use focusTraversable

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
        initProperties(sshUsernameField, sshPasswordField,
                databaseUsernameField, databasePasswordField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        if (userAbborted()) {
            return Optional.empty();
        } else {
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
    }
}
