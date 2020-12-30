package bayern.steinbrecher.green2.memberManagement.login.ssh;

import bayern.steinbrecher.checkedElements.textfields.CheckedPasswordField;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.green2.memberManagement.login.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.util.Optional;

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

    @FXML
    public void initialize() {
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
