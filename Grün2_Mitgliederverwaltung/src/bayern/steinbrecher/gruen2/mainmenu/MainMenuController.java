package bayern.steinbrecher.gruen2.mainmenu;

import bayern.steinbrecher.gruen2.Controller;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for MainMenu.fxml.
 *
 * @author Stefan Huber
 */
public class MainMenuController extends Controller {

    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private MainMenu caller;
    @FXML
    private Button generateContribution;
    @FXML
    private Button generateAllAddresses;
    @FXML
    private Button generateUniversalSepa;
    @FXML
    private Button checkIban;
    @FXML
    private Button generateAddressesBirthdayLastYear;
    @FXML
    private Button generateAddressesBirthdayThisYear;
    @FXML
    private Button generateAddressesBirthdayNextYear;
    @FXML
    private Button generateBirthdayLastYearInfos;
    @FXML
    private Button generateBirthdayThisYearInfos;
    @FXML
    private Button generateBirthdayNextYearInfos;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generateAddressesBirthdayLastYear.setText(
                "Von Geburtstagsmitgliedern " + (CURRENT_YEAR - 1));
        generateAddressesBirthdayThisYear.setText(
                "Von Geburtstagsmitgliedern " + CURRENT_YEAR);
        generateAddressesBirthdayNextYear.setText(
                "Von Geburtstagsmitgliedern " + (CURRENT_YEAR + 1));
        generateBirthdayLastYearInfos.setText(
                "Geburtstagsmitglieder " + (CURRENT_YEAR - 1) + " gruppiert");
        generateBirthdayThisYearInfos.setText(
                "Geburtstagsmitglieder " + CURRENT_YEAR + " gruppiert");
        generateBirthdayNextYearInfos.setText(
                "Geburtstagsmitglieder " + (CURRENT_YEAR + 1) + " gruppiert");
    }

    /**
     * Sets the caller which provides the funcionality this controller has to
     * use.
     *
     * @param caller The provider of the functionality.
     */
    public void setCaller(MainMenu caller) {
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
    private void checkIban() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        checkIban.setDisable(true);
        caller.checkIban();
        checkIban.setDisable(false);
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
    private void generateAddressesBirthdayLastYear() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateAddressesBirthdayLastYear.setDisable(true);
        caller.generateAddressesBirthday(CURRENT_YEAR - 1);
        generateAddressesBirthdayLastYear.setDisable(false);
    }

    @FXML
    private void generateAddressesBirthdayThisYear() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateAddressesBirthdayThisYear.setDisable(true);
        caller.generateAddressesBirthday(CURRENT_YEAR);
        generateAddressesBirthdayThisYear.setDisable(false);
    }

    @FXML
    private void generateAddressesBirthdayNextYear() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateAddressesBirthdayNextYear.setDisable(true);
        caller.generateAddressesBirthday(CURRENT_YEAR + 1);
        generateAddressesBirthdayNextYear.setDisable(false);
    }

    @FXML
    private void generateBirthdayLastYearInfos() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateBirthdayLastYearInfos.setDisable(true);
        caller.generateBirthdayInfos(CURRENT_YEAR - 1);
        generateBirthdayLastYearInfos.setDisable(false);
    }

    @FXML
    private void generateBirthdayThisYearInfos() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateBirthdayThisYearInfos.setDisable(true);
        caller.generateBirthdayInfos(CURRENT_YEAR);
        generateBirthdayThisYearInfos.setDisable(false);
    }

    @FXML
    private void generateBirthdayNextYearInfos() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        generateBirthdayNextYearInfos.setDisable(true);
        caller.generateBirthdayInfos(CURRENT_YEAR + 1);
        generateBirthdayNextYearInfos.setDisable(false);
    }
}
