package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.Controller;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for Main.fxml.
 *
 * @author Stefan Huber
 */
public class MainController extends Controller {
    
    private Main caller;
    @FXML
    private Button generateContribution;
    @FXML
    private Button generateAllAddresses;
    @FXML
    private Button generateUniversalSepa;
    @FXML
    private Button generateAddressesBirthdayThisYear;
    @FXML
    private Button generateAddressesBirthdayNextYear;
    @FXML
    private Button generateBirthdayThisYearInfos;
    @FXML
    private Button generateBirthdayNextYearInfos;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int currentYear = LocalDate.now().getYear();
        generateAddressesBirthdayThisYear.setText(
                "Von Geburtstagsmitgliedern " + currentYear);
        generateAddressesBirthdayNextYear.setText(
                "Von Geburtstagsmitgliedern " + (currentYear + 1));
        generateBirthdayThisYearInfos.setText(
                "Geburtstagsmitglieder " + currentYear + " gruppiert");
        generateBirthdayNextYearInfos.setText(
                "Geburtstagsmitglieder " + (currentYear + 1) + " gruppiert");
    }
    
    public void setCaller(Main caller) {
        this.caller = caller;
    }
    
    @FXML
    private void generateContributionSepa() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateContribution.setDisable(true);
        caller.generateContributionSepa();
        generateContribution.setDisable(false);
    }
    
    @FXML
    private void generateUniversalSepa() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateUniversalSepa.setDisable(true);
        caller.generateUniversalSepa();
        generateUniversalSepa.setDisable(false);
    }
    
    @FXML
    private void generateAddressesAll() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateAllAddresses.setDisable(true);
        caller.generateAddressesAll();
        generateAllAddresses.setDisable(false);
    }
    
    @FXML
    private void generateAddressesBirthdayThisYear() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateAddressesBirthdayThisYear.setDisable(true);
        caller.generateAddressesBirthdayThisYear();
        generateAddressesBirthdayThisYear.setDisable(false);
    }
    
    @FXML
    private void generateAddressesBirthdayNextYear() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateAddressesBirthdayNextYear.setDisable(true);
        caller.generateAddressesBirthdayNextYear();
        generateAddressesBirthdayNextYear.setDisable(false);
    }
    
    @FXML
    private void generateBirthdayThisYearInfos() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateBirthdayThisYearInfos.setDisable(true);
        caller.generateBirthdayThisYearInfos();
        generateBirthdayThisYearInfos.setDisable(false);
    }
    
    @FXML
    private void generateBirthdayNextYearInfos() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateBirthdayNextYearInfos.setDisable(true);
        caller.generateBirthdayNextYearInfos();
        generateBirthdayNextYearInfos.setDisable(false);
    }
}
