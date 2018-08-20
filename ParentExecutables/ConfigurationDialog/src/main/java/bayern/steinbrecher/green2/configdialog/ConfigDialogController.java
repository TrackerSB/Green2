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
package bayern.steinbrecher.green2.configdialog;

import bayern.steinbrecher.green2.WizardableController;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.CheckedComboBox;
import bayern.steinbrecher.green2.elements.ProfileNameField;
import bayern.steinbrecher.green2.elements.report.ReportSummary;
import bayern.steinbrecher.green2.elements.report.ReportType;
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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

/**
 * Controller of the dialog for configuring Green2.
 *
 * @author Stefan Huber
 */
public class ConfigDialogController extends WizardableController<Optional<Void>> {

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
    private ProfileNameField profileNameTextField;
    @FXML
    private CheckedComboBox<SupportedDatabases> dbmsComboBox;
    @FXML
    private CheckBox birthdayFeaturesCheckbox;
    @FXML
    private ReportSummary reportSummary;
    @FXML
    private CheckedRegexTextField birthdayExpressionTextField;
    private final List<CheckedTextField> checkedTextFields = new ArrayList<>();
    private Profile profile;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkedTextFields.addAll(Arrays.asList(sshHostTextField, databaseHostTextField, databaseNameTextField,
                birthdayExpressionTextField, profileNameTextField));

        birthdayExpressionTextField.setRegex(ProfileSettings.BIRTHDAY_FUNCTION_PATTERN.pattern());
        stageProperty().addListener((obs, oldVal, newVal) -> {
            newVal.titleProperty().bind(
                    new SimpleStringProperty(EnvironmentHandler.getResourceValue("configureApplication"))
                            .concat(": ")
                            .concat(profileNameTextField.textProperty()));
        });

        bindValidProperty(BindingUtility.reduceAnd(checkedTextFields.stream().map(CheckedTextField::validProperty))
                .and(dbmsComboBox.validProperty())
                .and(sshPort.validProperty())
                .and(databasePort.validProperty()));

        reportSummary.addReportEntry(EnvironmentHandler.getResourceValue("invalidBirthdayExpression"), ReportType.ERROR,
                birthdayExpressionTextField.matchRegexProperty().not()
                        .and(birthdayFeaturesCheckbox.selectedProperty()))
                .addReportEntry(sshPort)
                .addReportEntry(databasePort);
        checkedTextFields.stream().forEach(reportSummary::addReportEntry);

        //TODO Can loading/saving be abstracted?
        //Load settings
        profile = EnvironmentHandler.getProfile();
        useSSHCheckBox.setSelected(profile.getOrDefault(ProfileSettings.USE_SSH, true));
        sshHostTextField.setText(profile.getOrDefault(ProfileSettings.SSH_HOST, ""));
        //CHECKSTYLE.OFF: MagicNumber - 22 is the default port of SSH
        sshPort.getValueFactory().setValue(profile.getOrDefault(ProfileSettings.SSH_PORT, 22));
        //CHECKSTYLE.ON: MagicNumber
        databaseHostTextField.setText(profile.getOrDefault(ProfileSettings.DATABASE_HOST, ""));
        databasePort.getValueFactory().setValue(profile.getOrDefault(ProfileSettings.DATABASE_PORT,
                profile.getOrDefault(ProfileSettings.DBMS, SupportedDatabases.MY_SQL).getDefaultPort()));
        databaseNameTextField.setText(profile.getOrDefault(ProfileSettings.DATABASE_NAME, ""));
        birthdayExpressionTextField.setText(profile.getOrDefault(ProfileSettings.BIRTHDAY_EXPRESSION, ""));
        profileNameTextField.setText(profile.getProfileName());
        sepaWithBomCheckBox.setSelected(profile.getOrDefault(ProfileSettings.SEPA_USE_BOM, true));
        sshCharsetTextField.setText(
                profile.getOrDefault(ProfileSettings.SSH_CHARSET, StandardCharsets.UTF_8).name());
        dbmsComboBox.setItems(FXCollections.observableList(Arrays.asList(SupportedDatabases.values())));
        dbmsComboBox.getSelectionModel().select(profile.getOrDefault(ProfileSettings.DBMS, null));
        birthdayFeaturesCheckbox.setSelected(profile.getOrDefault(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES, true));
    }

    private boolean saveSettings() {
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
            getStage().close();
        }
        return isValid;
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void saveSettingsAndReturn() {
        if (saveSettings()) {
            Programs.CONFIGURATION_DIALOG.call();
        }
    }

    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void saveSettingsAndContinue() {
        if (saveSettings()) {
            Programs.MEMBER_MANAGEMENT.call();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Void> calculateResult() {
        return Optional.empty();
    }
}
