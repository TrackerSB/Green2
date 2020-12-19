package bayern.steinbrecher.green2.memberManagement.login;

import bayern.steinbrecher.dbConnector.credentials.DBCredentials;
import bayern.steinbrecher.green2.sharedBasis.utility.StagePreparer;
import bayern.steinbrecher.wizard.StandaloneWizardPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Represents a login.
 *
 * @param <C> The type of the login credentials.
 * @author Stefan Huber
 */
public abstract class Login<C extends DBCredentials> extends StandaloneWizardPage<Optional<C>, LoginController<C>>
        implements StagePreparer {
    public Login(@NotNull String fxmlPath, @Nullable ResourceBundle bundle) {
        super(fxmlPath, bundle);
    }
}
