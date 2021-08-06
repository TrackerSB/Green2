package bayern.steinbrecher.green2.memberManagement.elements;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.StandaloneWizardPage;

import java.util.Optional;

/**
 * Represents a splashscreen.
 *
 * @author Stefan Huber
 */
public class SplashScreen extends StandaloneWizardPage<Optional<Void>, SplashScreenController> {

    public SplashScreen() {
        super("SplashScreen.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
    }
}
