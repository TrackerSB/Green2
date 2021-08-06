package bayern.steinbrecher.green2.memberManagement.elements;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.StandaloneWizardPage;

import java.util.Optional;

/**
 * @author Stefan Huber
 */
public class WaitScreen extends StandaloneWizardPage<Optional<Void>, WaitScreenController> {

    public WaitScreen() {
        super("WaitScreen.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
    }
}
