package bayern.steinbrecher.green2.memberManagement.login.ssh;

import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.green2.memberManagement.login.Login;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;

/**
 * Represents a login with SSH.
 *
 * @author Stefan Huber
 */
public class SshLogin extends Login<SshCredentials> {

    public SshLogin(){
        super("SshLogin.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
    }
}
