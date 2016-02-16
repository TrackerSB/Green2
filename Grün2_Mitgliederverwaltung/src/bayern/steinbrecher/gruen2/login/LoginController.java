package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.data.LoginKey;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a controller for a login.
 *
 * @author Stefan Huber
 */
public abstract class LoginController extends Controller {

    /**
     * Returns the currently entered login information. It returns {@code null}
     * only if the window was closed without pressing a confirm button. That
     * means if {@code userConfirmed} is {@code false}.
     *
     * @return The currently entered login information.
     */
    public abstract Optional<Map<LoginKey, String>> getLoginInformation();
}
