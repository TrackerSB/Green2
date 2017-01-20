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
import bayern.steinbrecher.gruen2.data.Profile;
import bayern.steinbrecher.gruen2.utility.IOStreamUtility;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.utility.ProgramCaller;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private CheckedTextField birthdayExpressionTextField;
    private List<CheckedTextField> checkedTextFields = new ArrayList<>();
    private Profile profile;
    private BooleanProperty profileAlreadyExists
            = new SimpleBooleanProperty(this, "profileAlreadyExists");

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        checkedTextFields.addAll(Arrays.asList(sshHostTextField,
                databaseHostTextField, databaseNameTextField,
                birthdayExpressionTextField, profileNameTextField));

        profileNameTextField.textProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (stage != null) {
                        stage.setTitle(DataProvider
                                .getResourceValue("configureApplication")
                                + ": " + newVal);
                    }
                    profileAlreadyExists.set(
                            !profile.getProfileName().equals(newVal)
                            && Profile.getAvailableProfiles()
                                    .contains(newVal));
                });

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
                        BooleanBinding::and)
                .and(profileAlreadyExists.not()));

        //Load settings
        profile = DataProvider.getProfile();
        useSSHCheckBox.setSelected(
                profile.getOrDefault(ConfigKey.USE_SSH, true));
        sshHostTextField.setText(profile.getOrDefault(ConfigKey.SSH_HOST, ""));
        databaseHostTextField.setText(
                profile.getOrDefault(ConfigKey.DATABASE_HOST, ""));
        databaseNameTextField.setText(
                profile.getOrDefault(ConfigKey.DATABASE_NAME, ""));
        birthdayExpressionTextField.setText(
                profile.getOrDefault(ConfigKey.BIRTHDAY_EXPRESSION, ""));
        profileNameTextField.setText(profile.getProfileName());
        sepaWithBomCheckBox.setSelected(
                profile.getOrDefault(ConfigKey.SEPA_USE_BOM, true));
        sshCharsetTextField.setText(profile.getOrDefault(
                ConfigKey.SSH_CHARSET, StandardCharsets.ISO_8859_1.name()));
    }

    @FXML
    private void saveSettings() {
        checkStage();
        if (isValid()) {
            profile.saveSettings();
            profile.renameProfile(profileNameTextField.getText());
            stage.close();
        }
    }

    @FXML
    private void saveSettingsAndContinue() {
        saveSettings();
        ProgramCaller.startGrün2();
    }

    /**
     * Returns the property containing a value indicating whether the current
     * profile could be renamed to given profile.
     *
     * @return The property containing a value indicating whether the current
     * profile could be renamed to given profile.
     */
    public ReadOnlyBooleanProperty profileAlreadyExistsProperty() {
        return profileAlreadyExists;
    }

    /**
     * Checks whether the current profile could be renamed to given profile.
     *
     * @return {@code true} only if the current profile could be renamed to
     * given profile.
     */
    public boolean isProfileAlreadyExists() {
        return profileAlreadyExists.get();
    }
}
