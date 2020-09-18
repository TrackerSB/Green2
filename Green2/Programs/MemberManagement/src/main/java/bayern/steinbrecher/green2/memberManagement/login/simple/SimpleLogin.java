package bayern.steinbrecher.green2.memberManagement.login.simple;

import bayern.steinbrecher.dbConnector.credentials.SimpleCredentials;
import bayern.steinbrecher.green2.memberManagement.login.Login;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;

/**
 * Represents a login without SSH.
 *
 * @author Stefan Huber
 */
public class SimpleLogin extends Login<SimpleCredentials> {

    public SimpleLogin() {
        super("SimpleLogin.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
    }
}
