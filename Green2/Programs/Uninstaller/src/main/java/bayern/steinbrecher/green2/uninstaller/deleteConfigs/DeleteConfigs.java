package bayern.steinbrecher.green2.uninstaller.deleteConfigs;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;

import java.util.Optional;

public class DeleteConfigs extends WizardPage<Optional<Boolean>, DeleteConfigsController> {
    public DeleteConfigs() {
        super("deleteConfigs.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
    }
}
