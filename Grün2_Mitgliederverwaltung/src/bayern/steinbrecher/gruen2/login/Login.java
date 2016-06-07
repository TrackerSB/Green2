package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.View;
import bayern.steinbrecher.gruen2.data.LoginKey;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a login.
 *
 * @author Stefan Huber
 */
public abstract class Login extends View<LoginController> {

    /**
     * Returns the information that was entered in the login. This method blocks
     * until the frame is closed or hidden. It won't show more than once even if
     * multiple threads call it. They will be blocked and notified when the
     * login window closes.
     *
     * @return The Information that was entered in the login.
     */
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        showOnceAndWait();
        return controller.getLoginInformation();
    }
}
