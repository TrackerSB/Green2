package bayern.steinbrecher.green2.memberManagement.login;

import bayern.steinbrecher.dbConnector.credentials.DBCredentials;
import bayern.steinbrecher.wizard.StandaloneWizardPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Represents a login.
 *
 * @author Stefan Huber
 * @param <C> The type of the login credentials.
 */
public abstract class Login<C extends DBCredentials> extends StandaloneWizardPage<Optional<C>, LoginController<C>> {
    public Login(@NotNull String fxmlPath, @Nullable ResourceBundle bundle) {
        super(fxmlPath, bundle);
    }
}
