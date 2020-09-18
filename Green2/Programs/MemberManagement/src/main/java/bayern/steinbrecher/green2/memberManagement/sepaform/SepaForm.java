package bayern.steinbrecher.green2.memberManagement.sepaform;

import bayern.steinbrecher.green2.memberManagement.people.Originator;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;

import java.util.Optional;

/**
 * Represents a form for input according to sepa informations.
 *
 * @author Stefan Huber
 */
public class SepaForm extends WizardPage<Optional<Originator>, SepaFormController> {

    public SepaForm(){
        super("SepaForm.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
    }
}
