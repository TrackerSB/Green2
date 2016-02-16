package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.data.LoginKey;
import java.util.Map;
import java.util.Optional;
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
    public abstract Optional<Map<LoginKey, String>> getLoginInformation();
}
