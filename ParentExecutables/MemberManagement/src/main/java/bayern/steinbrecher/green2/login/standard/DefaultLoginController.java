/* 
 * Copyright (C) 2018 Stefan Huber
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
package bayern.steinbrecher.green2.login.standard;

import bayern.steinbrecher.green2.elements.textfields.CheckedPasswordField;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import bayern.steinbrecher.green2.login.LoginController;
import bayern.steinbrecher.green2.login.LoginKey;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;

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
        Platform.runLater(() -> databaseUsernameField.requestFocus());
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
