/*
 * Copyright (C) 2016 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.gruen2.configDialog;

import bayern.steinbrecher.gruen2.CheckedController;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.utility.IOStreamUtility;
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
public class ConfigDialogController extends CheckedController {

    @FXML
    private CheckBox useSSHCheckBox;
    @FXML
    private CheckBox sepaWithBomCheckBox;
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
        sepaWithBomCheckBox.setSelected(DataProvider.isSepaWithBom());

    }

    @FXML
    private void saveSettings() {
        checkStage();
        if (isValid()) {
            String out = ConfigKey.USE_SSH + ":"
                    + (useSSHCheckBox.isSelected() ? "true" : "false") + '\n'
                    + ConfigKey.SSH_HOST + ":" + (useSSHCheckBox.isSelected()
                            ? sshHostTextField.getText() : "noHost") + '\n'
                    + ConfigKey.DATABASE_HOST + ":"
                    + databaseHostTextField.getText() + '\n'
                    + ConfigKey.DATABASE_NAME + ":"
                    + databaseNameTextField.getText() + '\n'
                    + ConfigKey.BIRTHDAY_EXPRESSION + ":"
                    + birthdayExpressionTextField.getText() + '\n'
                    + ConfigKey.SEPA_USE_BOM + ":"
                    + (sepaWithBomCheckBox.isSelected() ? "true" : "false");
            IOStreamUtility.printContent(out, DataProvider.CONFIGFILE_PATH, false);
            stage.close();
        }
    }
}
