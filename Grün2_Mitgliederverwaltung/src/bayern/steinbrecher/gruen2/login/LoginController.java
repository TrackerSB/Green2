package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.data.LoginKey;
import java.util.Map;
import javafx.stage.Stage;

/**
 * Represents a controller for a login.
 *
 * @author Stefan Huber
 */
public abstract class LoginController {

    protected Stage stage = null;

    /**
     * Returns the currently entered login information.
     *
     * @return The currently entered login information.
     */
    public abstract Map<LoginKey, String> getLoginInformation();

    /**
     * Sets the stage the conroller can refer to. (E.g. for closing the stage)
     *
     * @param stage The stage to refer to.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
