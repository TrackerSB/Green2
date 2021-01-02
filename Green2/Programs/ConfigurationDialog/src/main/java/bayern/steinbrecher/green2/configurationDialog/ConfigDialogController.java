package bayern.steinbrecher.green2.configurationDialog;

import bayern.steinbrecher.checkedElements.CheckedComboBox;
import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.checkedElements.textfields.CheckedRegexTextField;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.query.SupportedDBMS;
import bayern.steinbrecher.green2.configurationDialog.elements.ProfileNameField;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.Profile;
import bayern.steinbrecher.green2.sharedBasis.data.ProfileSettings;
import bayern.steinbrecher.green2.sharedBasis.utility.Programs;
import bayern.steinbrecher.javaUtility.BindingUtility;
import bayern.steinbrecher.wizard.WizardPageController;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller of the dialog for configuring Green2.
 *
 * @author Stefan Huber
 */
public class ConfigDialogController extends WizardPageController<Optional<Void>> {

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
    private CheckedComboBox<SupportedDBMS> dbmsComboBox;
    @FXML
    private CheckBox birthdayFeaturesCheckbox;
    @FXML
    private CheckedRegexTextField birthdayExpressionTextField;
    @FXML
    private ResourceBundle resources;
    private final ReadOnlyObjectWrapper<Stage> stage = new ReadOnlyObjectWrapper<>();
    private final List<CheckedTextField> checkedTextFields = new ArrayList<>();
    private Profile profile;

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initialize() {
        checkedTextFields.addAll(Arrays.asList(sshHostTextField, databaseHostTextField, databaseNameTextField,
                birthdayExpressionTextField, profileNameTextField));

        birthdayExpressionTextField.setRegex(ProfileSettings.BIRTHDAY_FUNCTION_PATTERN.pattern());
        stageProperty().addListener((obs, oldVal, newVal) -> {
            newVal.titleProperty().bind(
                    new SimpleStringProperty(resources.getString("configureApplication"))
                            .concat(": ")
                            .concat(profileNameTextField.textProperty()));
        });

        bindValidProperty(BindingUtility.reduceAnd(checkedTextFields.stream().map(CheckedTextField::validProperty))
                .and(dbmsComboBox.validProperty())
                .and(sshPort.validProperty())
                .and(databasePort.validProperty()));

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
                profile.getOrDefault(ProfileSettings.DBMS, SupportedDBMS.MY_SQL).getDefaultPort()));
        databaseNameTextField.setText(profile.getOrDefault(ProfileSettings.DATABASE_NAME, ""));
        birthdayExpressionTextField.setText(profile.getOrDefault(ProfileSettings.BIRTHDAY_EXPRESSION, ""));
        profileNameTextField.setText(profile.getProfileName());
        sepaWithBomCheckBox.setSelected(profile.getOrDefault(ProfileSettings.SEPA_USE_BOM, true));
        sshCharsetTextField.setText(
                profile.getOrDefault(ProfileSettings.SSH_CHARSET, StandardCharsets.UTF_8).name());
        // Copy DBMSs in order to avoid access to an immutable list
        dbmsComboBox.setItems(FXCollections.observableList(new ArrayList<>(SupportedDBMS.DBMSs)));
        dbmsComboBox.getSelectionModel().select(profile.getOrDefault(ProfileSettings.DBMS, null));
        birthdayFeaturesCheckbox.setSelected(profile.getOrDefault(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES, true));
    }

    public ReadOnlyObjectProperty<Stage> stageProperty() {
        return stage.getReadOnlyProperty();
    }

    public Stage getStage() {
        return stageProperty().get();
    }

    void setStage(Stage stage) {
        this.stage.set(stage);
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
    @SuppressWarnings("unused")
    private void saveSettingsAndReturn() {
        if (saveSettings()) {
            Programs.CONFIGURATION_DIALOG.call();
        }
    }

    @FXML
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
