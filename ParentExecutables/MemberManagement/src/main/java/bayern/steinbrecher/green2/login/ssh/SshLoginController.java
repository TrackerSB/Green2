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
package bayern.steinbrecher.green2.login.ssh;

import bayern.steinbrecher.green2.connection.credentials.SshCredentials;
import bayern.steinbrecher.green2.elements.textfields.CheckedPasswordField;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import bayern.steinbrecher.green2.login.LoginController;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;

/**
 * The controller for SshLogin.fxml.
 *
 * @author Stefan Huber
 */
public class SshLoginController extends LoginController<SshCredentials> {

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
    public void initialize(URL location, ResourceBundle resources) {
        initProperties(sshUsernameField, sshPasswordField, databaseUsernameField, databasePasswordField);
        Platform.runLater(() -> sshUsernameField.requestFocus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SshCredentials> calculateResult() {
        return Optional.of(new SshCredentials(databaseUsernameField.getText(), databasePasswordField.getText(),
                sshUsernameField.getText(), sshPasswordField.getText()));
    }
}
