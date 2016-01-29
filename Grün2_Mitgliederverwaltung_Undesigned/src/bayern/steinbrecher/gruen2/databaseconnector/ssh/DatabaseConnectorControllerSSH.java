package bayern.steinbrecher.gruen2.databaseconnector.ssh;

import bayern.steinbrecher.gruen2.data.DataProvider;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Die Controller-Klasse im MVC-Pattern f&uuml;r DatabaseConnection.fxml
 *
 * @author Stefan Huber
 */
public class DatabaseConnectorControllerSSH implements Initializable {

    @FXML
    private Label nameOrPasswdWrong,
            missingInputLabel;
    @FXML
    private CheckBox allowEmptyFieldsCheckbox;
    @FXML
    private Button loginButton;
    @FXML
    private TextField sshNameField,
            databaseNameField;
    @FXML
    private PasswordField sshPasswdField,
            databasePasswdField;
    //Um einfach auf alle Text-/Passwortfelder referenzieren zu können
    private ArrayList<TextField> textFields = new ArrayList<>();
    private DatabaseConnectorSSH dbc;

    @FXML
    private void login() {
        checkInputOfTextFields();
        try {
            if (!missingInputLabel.isVisible()) {
                dbc.update(new DatabaseSshConnection(
                        DataProvider.getOrDefault("sshhost", "localhost"),
                        sshNameField.getText(), sshPasswdField.getText(),
                        DataProvider.getOrDefault("databasehost", "localhost"),
                        databaseNameField.getText(),
                        databasePasswdField.getText(),
                        DataProvider.getOrDefault("databasename",
                                "Mitglieder")));
                try {
                    disableControls(true);
                    dbc.callCallable().get();
                } catch (Exception ex) {
                    Logger.getLogger(
                            DatabaseConnectorControllerSSH.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                ((Stage) sshNameField.getScene().getWindow()).close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnectorControllerSSH.class.getName())
                    .log(Level.SEVERE, null, ex);
            nameOrPasswdWrong.setVisible(true);
        }
    }
    
    private void disableControls(boolean disable) {
        textFields.forEach(t -> t.setDisable(disable));
        loginButton.setDisable(disable);
        allowEmptyFieldsCheckbox.setDisable(disable);
    }

    @FXML
    private void checkInputOfTextFields() {
        boolean anyMissingInput = false;
        for (TextField t : textFields) {
            if (t.getText().isEmpty()
                    && !allowEmptyFieldsCheckbox.isSelected()) {
                anyMissingInput = true;
                DataProvider.changeMissingInputStyleClass(t, true);
            } else {
                DataProvider.changeMissingInputStyleClass(t, false);
            }
        }
        missingInputLabel.setVisible(anyMissingInput);
        loginButton.setDisable(anyMissingInput);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Collections.addAll(textFields, sshNameField, databaseNameField,
                sshPasswdField, databasePasswdField);
        textFields.forEach(tf -> {
            tf.textProperty().addListener(obs -> checkInputOfTextFields());
        });
        allowEmptyFieldsCheckbox.selectedProperty()
                .addListener(obs -> checkInputOfTextFields());
    }

    /**
     * Setzt das Model, das die Daten h&auml;lt, die dieser Controller aktuell
     * halten soll.
     *
     * @param dbc Das neue Model-Objekt
     */
    public void setModel(DatabaseConnectorSSH dbc) {
        this.dbc = dbc;
    }
}
