package bayern.steinbrecher.green2.memberManagement.login.ssh;

import java.io.IOException;

import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.green2.memberManagement.login.Login;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
