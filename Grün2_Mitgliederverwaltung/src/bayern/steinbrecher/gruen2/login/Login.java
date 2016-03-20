package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.Model;
import bayern.steinbrecher.gruen2.data.LoginKey;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a login.
 *
 * @author Stefan Huber
 */
public abstract class Login extends Model {

    protected LoginController loginContoller;

    /**
     * Returns the Information that was entered in the login. This method blocks
     * until the frame is closed or hidden. It won´t show more than once even if
     * multiple threads call it. They will be blocked and notified when the
     * login window closes.
     *
     * @return The Information that was entered in the login.
     */
    public Optional<Map<LoginKey, String>> getLoginInformation() {
        if (stage == null) {
            throw new IllegalStateException(
                    "start(...) has to be called first");
        }
        onlyShowOnce();
        return loginContoller.getLoginInformation();
    }
}
