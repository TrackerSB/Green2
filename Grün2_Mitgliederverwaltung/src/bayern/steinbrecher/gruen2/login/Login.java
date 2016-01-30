package bayern.steinbrecher.gruen2.login;

import java.util.Map;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Represents a login.
 *
 * @author Stefan Huber
 */
public abstract class Login extends Application {

    public static final String DATABASE_USERNAME_KEY = "databaseUsername";
    public static final String DATABASE_PASSWORD_KEY = "databasePassword";
    public static final String SSH_USERNAME_KEY = "sshUsername";
    public static final String SSH_PASSWORD_KEY = "sshPassword";

    /**
     * Returns the Information that was entered in the login. This method blocks
     * until the frame is closed or hidden.
     *
     * @return The Information that was entered in the login.
     * @see Stage#showAndWait()
     */
    public abstract Map<String, String> getLoginInformation();
}
