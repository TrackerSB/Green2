/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.login.ssh;

import bayern.steinbrecher.gruen2.elements.CheckedPasswordField;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.login.LoginController;
import bayern.steinbrecher.gruen2.login.LoginKey;
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
