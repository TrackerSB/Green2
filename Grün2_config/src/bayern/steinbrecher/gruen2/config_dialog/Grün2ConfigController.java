package bayern.steinbrecher.gruen2.config_dialog;

import bayern.steinbrecher.gruen2.CheckedController;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;

/**
 * @author Stefan Huber
 */
public class Grün2ConfigController extends CheckedController {

    @FXML
    private CheckedTextField sshHostTextField;
    @FXML
    private CheckedTextField databaseHostTextField;
    @FXML
    private CheckedTextField databaseNameTextField;
    @FXML
    private CheckedTextField birthdayExpressionTextField;
    private List<CheckedTextField> checkedTextFields = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        checkedTextFields.addAll(Arrays.asList(sshHostTextField,
                databaseHostTextField, databaseNameTextField,
                birthdayExpressionTextField));
        anyInputMissing.bind(checkedTextFields.stream()
                .map(ctf -> ctf.emptyProperty())
                .reduce(FALSE_BINDING, (bind, prop) -> bind.or(prop),
                        BooleanBinding::or));
        anyInputToLong.bind(checkedTextFields.stream()
                .map(ctf -> ctf.toLongProperty())
                .reduce(FALSE_BINDING, (bind, prop) -> bind.or(prop),
                        BooleanBinding::or));
        valid.bind(checkedTextFields.stream()
                .map(ctf -> ctf.validProperty())
                .reduce(TRUE_BINDING, (bind, prop) -> bind.and(prop),
                        BooleanBinding::and));
    }
}
