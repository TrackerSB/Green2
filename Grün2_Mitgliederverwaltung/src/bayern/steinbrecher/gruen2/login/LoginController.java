package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.data.LoginKey;
import java.util.Map;

/**
 * Represents a controller for a login.
 *
 * @author Stefan Huber
 */
public abstract class LoginController extends Controller {

    /**
     * Returns the currently entered login information.
     *
     * @return The currently entered login information.
     */
    public abstract Map<LoginKey, String> getLoginInformation();
}
