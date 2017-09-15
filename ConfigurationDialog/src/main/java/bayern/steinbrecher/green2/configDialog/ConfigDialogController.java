/* 
 * Copyright (C) 2017 Stefan Huber
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
package bayern.steinbrecher.green2.configDialog;

import bayern.steinbrecher.green2.CheckedController;
import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.data.ConfigKey;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.CheckedComboBox;
import bayern.steinbrecher.green2.elements.textfields.CheckedRegexTextField;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import bayern.steinbrecher.green2.utility.BindingUtility;
import bayern.steinbrecher.green2.utility.ProgramCaller;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

/**
 * Controller of the dialog for configuring Green2.
 *
 * @author Stefan Huber
 */
public class ConfigDialogController extends CheckedController {

    @FXML
    private CheckBox useSSHCheckBox;
    @FXML
    private CheckBox sepaWithBomCheckBox;
    @FXML
    private CheckedTextField sshCharsetTextField;
    @FXML
    private CheckedTextField sshHostTextField;
    @FXML
    private CheckedTextField databaseHostTextField;
    @FXML
    private CheckedTextField databaseNameTextField;
    @FXML
    private CheckedTextField profileNameTextField;
    @FXML
    private CheckedComboBox<DBConnection.SupportedDatabase> dbmsComboBox;
    @FXML
    private CheckedRegexTextField birthdayExpressionTextField;
    private final List<CheckedTextField> checkedTextFields = new ArrayList<>();
    private Profile profile;
    private final BooleanProperty profileAlreadyExists = new SimpleBooleanProperty(this, "profileAlreadyExists");

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        checkedTextFields.addAll(Arrays.asList(sshHostTextField, databaseHostTextField, databaseNameTextField,
                birthdayExpressionTextField, profileNameTextField));

        birthdayExpressionTextField.setRegex(ConfigKey.BIRTHDAY_PATTERN.pattern());
        profileNameTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (stage != null) {
                stage.setTitle(EnvironmentHandler.getResourceValue("configureApplication") + ": " + newVal);
            }
            profileAlreadyExists.set(!profile.getProfileName().equals(newVal)
                    && Profile.getAvailableProfiles().contains(newVal));
        });

        anyInputMissing.bind(checkedTextFields.stream()
                .map(CheckedTextField::emptyProperty)
                .reduce(BindingUtility.FALSE_BINDING, BooleanExpression::or, BooleanBinding::or)
                .or(dbmsComboBox.nothingSelectedProperty()));
        anyInputToLong.bind(checkedTextFields.stream()
                .map(CheckedTextField::toLongProperty)
                .reduce(BindingUtility.FALSE_BINDING, BooleanExpression::or, BooleanBinding::or));
        valid.bind(checkedTextFields.stream()
                .map(CheckedTextField::validProperty)
                .reduce(BindingUtility.TRUE_BINDING, BooleanExpression::and, BooleanBinding::and)
                .and(profileAlreadyExists.not())
                .and(dbmsComboBox.nothingSelectedProperty().not()));

        //Load settings
        profile = EnvironmentHandler.getProfile();
        useSSHCheckBox.setSelected(profile.getOrDefault(ConfigKey.USE_SSH, true));
        sshHostTextField.setText(profile.getOrDefault(ConfigKey.SSH_HOST, ""));
        databaseHostTextField.setText(profile.getOrDefault(ConfigKey.DATABASE_HOST, ""));
        databaseNameTextField.setText(profile.getOrDefault(ConfigKey.DATABASE_NAME, ""));
        birthdayExpressionTextField.setText(profile.getOrDefault(ConfigKey.BIRTHDAY_EXPRESSION, ""));
        profileNameTextField.setText(profile.getProfileName());
        sepaWithBomCheckBox.setSelected(profile.getOrDefault(ConfigKey.SEPA_USE_BOM, true));
        sshCharsetTextField.setText(profile.getOrDefault(ConfigKey.SSH_CHARSET, StandardCharsets.ISO_8859_1).name());
        dbmsComboBox.setItems(FXCollections.observableList(Arrays.asList(DBConnection.SupportedDatabase.values())));
        dbmsComboBox.getSelectionModel().select(profile.getOrDefault(ConfigKey.DBMS, null));
    }

    @FXML
    private void saveSettings() {
        checkStage();
        if (isValid()) {
            profile.set(ConfigKey.USE_SSH, useSSHCheckBox.isSelected());
            profile.set(ConfigKey.SSH_HOST, sshHostTextField.getText());
            profile.set(ConfigKey.DATABASE_HOST, databaseHostTextField.getText());
            profile.set(ConfigKey.DATABASE_NAME, databaseNameTextField.getText());
            profile.set(ConfigKey.BIRTHDAY_EXPRESSION, birthdayExpressionTextField.getText());
            profile.renameProfile(profileNameTextField.getText());
            profile.set(ConfigKey.SEPA_USE_BOM, sepaWithBomCheckBox.isSelected());
            profile.set(ConfigKey.SSH_CHARSET, Charset.forName(sshCharsetTextField.getText()));
            profile.set(ConfigKey.DBMS, dbmsComboBox.getSelectionModel().getSelectedItem());
            profile.saveSettings();
            stage.close();
        }
    }

    @FXML
    private void saveSettingsAndContinue() {
        saveSettings();
        ProgramCaller.startGreen2();
    }

    /**
     * Returns the property containing a value indicating whether the current profile could be renamed to given profile.
     *
     * @return The property containing a value indicating whether the current profile could be renamed to given profile.
     */
    public ReadOnlyBooleanProperty profileAlreadyExistsProperty() {
        return profileAlreadyExists;
    }

    /**
     * Checks whether the current profile could be renamed to given profile.
     *
     * @return {@code true} only if the current profile could be renamed to given profile.
     */
    public boolean isProfileAlreadyExists() {
        return profileAlreadyExists.get();
    }
}
