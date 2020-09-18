package bayern.steinbrecher.green2.memberManagement.login.simple;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import bayern.steinbrecher.checkedElements.textfields.CheckedPasswordField;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.credentials.SimpleCredentials;
import bayern.steinbrecher.green2.memberManagement.login.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXML;

/**
 * Controller of DefaultLogin.fxml.
 *
 * @author Stefan Huber
 */
public class SimpleLoginController extends LoginController<SimpleCredentials> {

    @FXML
    private CheckedTextField databaseUsernameField;
    @FXML
    private CheckedPasswordField databasePasswordField;

    @FXML
    public void initialize() {
        initProperties(databaseUsernameField, databasePasswordField);
        Platform.runLater(() -> databaseUsernameField.requestFocus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<SimpleCredentials> calculateResult() {
        return Optional.of(new SimpleCredentials(databaseUsernameField.getText(), databasePasswordField.getText()));
    }
}
