package bayern.steinbrecher.gruen2.config_dialog;

import bayern.steinbrecher.gruen2.CheckedController;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.data.Output;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

/**
 * Controller of the dialog for configuring Grün2.
 *
 * @author Stefan Huber
 */
public class Grün2ConfigController extends CheckedController {

    @FXML
    private CheckBox useSSHCheckBox;
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

        //Load settings
        useSSHCheckBox.setSelected(DataProvider.useSsh());
        sshHostTextField.setText(
                DataProvider.getOrDefault(ConfigKey.SSH_HOST, ""));
        databaseHostTextField.setText(
                DataProvider.getOrDefault(ConfigKey.DATABASE_HOST, ""));
        databaseNameTextField.setText(
                DataProvider.getOrDefault(ConfigKey.DATABASE_NAME, ""));
        birthdayExpressionTextField.setText(
                DataProvider.getOrDefault(ConfigKey.BIRTHDAY_EXPRESSION, ""));
    }

    @FXML
    private void saveSettings() {
        checkStage();
        if (isValid()) {
            String out = ConfigKey.USE_SSH + ":"
                    + (useSSHCheckBox.isSelected() ? "ja" : "nein") + '\n'
                    + ConfigKey.SSH_HOST + ":" + (useSSHCheckBox.isSelected()
                            ? sshHostTextField.getText() : "noHost") + '\n'
                    + ConfigKey.DATABASE_HOST + ":"
                    + databaseHostTextField.getText() + '\n'
                    + ConfigKey.DATABASE_NAME + ":"
                    + databaseNameTextField.getText() + '\n'
                    + ConfigKey.BIRTHDAY_EXPRESSION + ":"
                    + birthdayExpressionTextField.getText();
            Output.printContent(out, DataProvider.CONFIGFILE_PATH, false);
            stage.close();
        }
    }
}
