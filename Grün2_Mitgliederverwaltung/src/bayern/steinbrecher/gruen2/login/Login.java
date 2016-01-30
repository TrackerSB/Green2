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

    /**
     * Returns the Information that was entered in the login. This method blocks
     * until the frame is closed or hidden.
     *
     * @return The Information that was entered in the login.
     * @see Stage#showAndWait()
     */
    public abstract Map<String, String> getLoginInformation();
}
