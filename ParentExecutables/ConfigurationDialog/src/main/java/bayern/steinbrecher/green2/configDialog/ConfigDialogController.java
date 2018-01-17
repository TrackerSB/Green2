/* 
 * Copyright (C) 2018 Stefan Huber
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
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.CheckedComboBox;
import bayern.steinbrecher.green2.elements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.green2.elements.textfields.CheckedRegexTextField;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import bayern.steinbrecher.green2.utility.BindingUtility;
import bayern.steinbrecher.green2.utility.Programs;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
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
    private CheckedIntegerSpinner sshPort;
    @FXML
    private CheckedTextField databaseHostTextField;
    @FXML
    private CheckedIntegerSpinner databasePort;
    @FXML
    private CheckedTextField databaseNameTextField;
    @FXML
    private CheckedTextField profileNameTextField;
    @FXML
    private CheckedComboBox<SupportedDatabases> dbmsComboBox;
    @FXML
    private CheckBox birthdayFeaturesCheckbox;
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

        birthdayExpressionTextField.setRegex(ProfileSettings.BIRTHDAY_FUNCTION_PATTERN.pattern());
        profileNameTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (stage != null) {
                stage.setTitle(EnvironmentHandler.getResourceValue("configureApplication") + ": " + newVal);
            }
            profileAlreadyExists.set(!profile.getProfileName().equals(newVal)
                    && Profile.getAvailableProfiles().contains(newVal));
        });

        anyInputMissing.bind(BindingUtility.reduceOr(checkedTextFields.stream().map(CheckedTextField::emptyProperty))
                .or(dbmsComboBox.nothingSelectedProperty())
                .or(sshPort.valueProperty().isNull())
                .or(databasePort.valueProperty().isNull()));
        anyInputToLong.bind(BindingUtility.reduceOr(checkedTextFields.stream().map(CheckedTextField::toLongProperty)));
        valid.bind(BindingUtility.reduceAnd(checkedTextFields.stream().map(CheckedTextField::validProperty))
                .and(profileAlreadyExists.not())
                .and(dbmsComboBox.nothingSelectedProperty().not())
                .and(sshPort.validProperty())
                .and(databasePort.validProperty()));

        //TODO Can loading/saving be abstracted?
        //Load settings
        profile = EnvironmentHandler.getProfile();
        useSSHCheckBox.setSelected(profile.getOrDefault(ProfileSettings.USE_SSH, true));
        sshHostTextField.setText(profile.getOrDefault(ProfileSettings.SSH_HOST, ""));
        sshPort.getValueFactory().setValue(profile.getOrDefault(ProfileSettings.SSH_PORT, 22));
        databaseHostTextField.setText(profile.getOrDefault(ProfileSettings.DATABASE_HOST, ""));
        databasePort.getValueFactory().setValue(profile.getOrDefault(ProfileSettings.DATABASE_PORT,
                profile.getOrDefault(ProfileSettings.DBMS, SupportedDatabases.MY_SQL).getDefaultPort()));
        databaseNameTextField.setText(profile.getOrDefault(ProfileSettings.DATABASE_NAME, ""));
        birthdayExpressionTextField.setText(profile.getOrDefault(ProfileSettings.BIRTHDAY_EXPRESSION, ""));
        profileNameTextField.setText(profile.getProfileName());
        sepaWithBomCheckBox.setSelected(profile.getOrDefault(ProfileSettings.SEPA_USE_BOM, true));
        sshCharsetTextField.setText(
                profile.getOrDefault(ProfileSettings.SSH_CHARSET, StandardCharsets.ISO_8859_1).name());
        dbmsComboBox.setItems(FXCollections.observableList(Arrays.asList(SupportedDatabases.values())));
        dbmsComboBox.getSelectionModel().select(profile.getOrDefault(ProfileSettings.DBMS, null));
        birthdayFeaturesCheckbox.setSelected(profile.getOrDefault(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES, true));
    }

    private boolean saveSettings() {
        checkStage();
        boolean isValid = isValid();
        if (isValid) {
            profile.set(ProfileSettings.USE_SSH, useSSHCheckBox.isSelected());
            profile.set(ProfileSettings.SSH_HOST, sshHostTextField.getText());
            profile.set(ProfileSettings.SSH_PORT, sshPort.getValue());
            profile.set(ProfileSettings.DATABASE_HOST, databaseHostTextField.getText());
            profile.set(ProfileSettings.DATABASE_PORT, databasePort.getValue());
            profile.set(ProfileSettings.DATABASE_NAME, databaseNameTextField.getText());
            profile.set(ProfileSettings.BIRTHDAY_EXPRESSION, birthdayExpressionTextField.getRegexValidText());
            profile.set(ProfileSettings.SEPA_USE_BOM, sepaWithBomCheckBox.isSelected());
            profile.set(ProfileSettings.SSH_CHARSET, Charset.forName(sshCharsetTextField.getText()));
            profile.set(ProfileSettings.DBMS, dbmsComboBox.getSelectionModel().getSelectedItem());
            profile.set(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES, birthdayFeaturesCheckbox.isSelected());
            profile.saveSettings();
            profile.renameProfile(profileNameTextField.getText());
            stage.close();
        }
        return isValid;
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void saveSettingsAndReturn() {
        if (saveSettings()) {
            Programs.CONFIGURATION_DIALOG.call();
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    private void saveSettingsAndContinue() {
        if (saveSettings()) {
            Programs.MEMBER_MANAGEMENT.call();
        }
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
