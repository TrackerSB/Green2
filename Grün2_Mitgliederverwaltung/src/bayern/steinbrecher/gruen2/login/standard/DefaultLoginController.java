package bayern.steinbrecher.gruen2.login.standard;

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
 * Controller of DefaultLogin.fxml.
 *
 * @author Stefan Huber
 */
public class DefaultLoginController extends LoginController {
//TODO don´t use focusTraversable

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
        Map<LoginKey, String> loginInfo = new HashMap<>(2);
        loginInfo.put(LoginKey.DATABASE_USERNAME,
                databaseUsernameField.getText());
        loginInfo.put(LoginKey.DATABASE_PASSWORD,
                databasePasswordField.getText());
        if (userConfirmed) {
            return Optional.of(loginInfo);
        }
        return Optional.empty();
    }
}
