package bayern.steinbrecher.green2.uninstaller.confirmUninstall;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;

import java.util.Optional;

public class ConfirmUninstall extends WizardPage<Optional<Void>, ConfirmUninstallController> {
    public ConfirmUninstall() {
        super("confirmUninstall.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
    }
}
