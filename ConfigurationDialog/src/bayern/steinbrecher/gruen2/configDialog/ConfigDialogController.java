/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.configDialog;

import bayern.steinbrecher.gruen2.CheckedController;
import bayern.steinbrecher.gruen2.data.ConfigKey;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.data.Profile;
import bayern.steinbrecher.gruen2.elements.CheckedRegexTextField;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.utility.ProgramCaller;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

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
    private CheckedRegexTextField birthdayExpressionTextField;
    private List<CheckedTextField> checkedTextFields = new ArrayList<>();
    private Profile profile;
    private BooleanProperty profileAlreadyExists = new SimpleBooleanProperty(this, "profileAlreadyExists");

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
                stage.setTitle(DataProvider.getResourceValue("configureApplication") + ": " + newVal);
            }
            profileAlreadyExists.set(!profile.getProfileName().equals(newVal)
                    && Profile.getAvailableProfiles().contains(newVal));
        });

        anyInputMissing.bind(checkedTextFields.stream()
                .map(CheckedTextField::emptyProperty)
                .reduce(FALSE_BINDING, BooleanExpression::or, BooleanBinding::or));
        anyInputToLong.bind(checkedTextFields.stream()
                .map(CheckedTextField::toLongProperty)
                .reduce(FALSE_BINDING, BooleanExpression::or, BooleanBinding::or));
        valid.bind(checkedTextFields.stream()
                .map(CheckedTextField::validProperty)
                .reduce(TRUE_BINDING, BooleanExpression::and, BooleanBinding::and)
                .and(profileAlreadyExists.not()));

        //Load settings
        //FIXME Think about whether to "bind" values to ConfigKeys
        profile = DataProvider.getProfile();
        useSSHCheckBox.setSelected(profile.getOrDefault(ConfigKey.USE_SSH, true));
        sshHostTextField.setText(profile.getOrDefault(ConfigKey.SSH_HOST, ""));
        databaseHostTextField.setText(profile.getOrDefault(ConfigKey.DATABASE_HOST, ""));
        databaseNameTextField.setText(profile.getOrDefault(ConfigKey.DATABASE_NAME, ""));
        birthdayExpressionTextField.setText(profile.getOrDefault(ConfigKey.BIRTHDAY_EXPRESSION, ""));
        profileNameTextField.setText(profile.getProfileName());
        sepaWithBomCheckBox.setSelected(profile.getOrDefault(ConfigKey.SEPA_USE_BOM, true));
        sshCharsetTextField.setText(profile.getOrDefault(ConfigKey.SSH_CHARSET, StandardCharsets.ISO_8859_1).name());
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
            profile.saveSettings();
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
