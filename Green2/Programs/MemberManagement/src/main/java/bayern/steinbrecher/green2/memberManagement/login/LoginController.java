package bayern.steinbrecher.green2.memberManagement.login;

import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.credentials.DBCredentials;
import bayern.steinbrecher.javaUtility.BindingUtility;
import bayern.steinbrecher.wizard.WizardPageController;
import javafx.fxml.FXML;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents a controller for a login.
 *
 * @author Stefan Huber
 * @param <C> The type of the login credentials.
 */
public abstract class LoginController<C extends DBCredentials> extends WizardPageController<Optional<C>> {

    /**
     * Adds all given textfields into {@code textInputFields} and sets up the properties {@code anyInputMissing},
     * {@code anyInputToLong} and {@code valid}.
     *
     * @param fields The textfields to use for setup.
     */
    protected void initProperties(CheckedTextField... fields) {
        List<CheckedTextField> textInputFields = Arrays.asList(fields);

        bindValidProperty(BindingUtility.reduceAnd(textInputFields.stream().map(CheckedTextField::validProperty)));
    }
}
