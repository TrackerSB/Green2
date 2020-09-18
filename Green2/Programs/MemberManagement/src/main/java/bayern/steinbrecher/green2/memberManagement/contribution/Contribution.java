package bayern.steinbrecher.green2.memberManagement.contribution;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;
import com.google.common.collect.BiMap;
import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * Represents the contribution inserted.
 *
 * @author Stefan Huber
 */
public class Contribution extends WizardPage<Optional<BiMap<Double, Color>>, ContributionController> {

    public Contribution() {
        super("Contribution.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
    }
}
