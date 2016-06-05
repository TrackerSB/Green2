package bayern.steinbrecher.gruen2.menu;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.main.Main;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for Menu.fxml.
 *
 * @author Stefan Huber
 */
public class MenuController extends Controller {

    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private Main caller;
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
        String resourceValue = DataProvider.RESOURCE_BUNDLE
                .getString("ofBirthdayMember");
        generateAddressesBirthdayLastYear.setText(
                MessageFormat.format(resourceValue, CURRENT_YEAR - 1));
        generateAddressesBirthdayThisYear.setText(
                MessageFormat.format(resourceValue, CURRENT_YEAR));
        generateAddressesBirthdayNextYear.setText(
                MessageFormat.format(resourceValue, CURRENT_YEAR + 1));

        resourceValue = DataProvider.RESOURCE_BUNDLE
                .getString("groupedBirthdayMember");
        generateBirthdayLastYearInfos.setText(
                MessageFormat.format(resourceValue, CURRENT_YEAR - 1));
        generateBirthdayThisYearInfos.setText(
                MessageFormat.format(resourceValue, CURRENT_YEAR));
        generateBirthdayNextYearInfos.setText(
                MessageFormat.format(resourceValue, CURRENT_YEAR + 1));
    }

    /**
     * Sets the caller which provides the funcionality this controller has to
     * use.
     *
     * @param caller The provider of the functionality.
     */
    public void setCaller(Main caller) {
        this.caller = caller;
    }

    private void checkCaller() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
    }

    @FXML
    private void generateContributionSepa() {
        checkCaller();
        generateContribution.setDisable(true);
        caller.generateContributionSepa();
        generateContribution.setDisable(false);
    }

    @FXML
    private void generateUniversalSepa() {
        checkCaller();
        generateUniversalSepa.setDisable(true);
        caller.generateUniversalSepa();
        generateUniversalSepa.setDisable(false);
    }

    @FXML
    private void checkIban() {
        checkCaller();
        checkIban.setDisable(true);
        caller.checkIban();
        checkIban.setDisable(false);
    }

    @FXML
    private void generateAddressesAll() {
        checkCaller();
        generateAllAddresses.setDisable(true);
        caller.generateAddressesAll();
        generateAllAddresses.setDisable(false);
    }

    @FXML
    private void generateAddressesBirthdayLastYear() {
        checkCaller();
        generateAddressesBirthdayLastYear.setDisable(true);
        caller.generateAddressesBirthday(CURRENT_YEAR - 1);
        generateAddressesBirthdayLastYear.setDisable(false);
    }

    @FXML
    private void generateAddressesBirthdayThisYear() {
        checkCaller();
        generateAddressesBirthdayThisYear.setDisable(true);
        caller.generateAddressesBirthday(CURRENT_YEAR);
        generateAddressesBirthdayThisYear.setDisable(false);
    }

    @FXML
    private void generateAddressesBirthdayNextYear() {
        checkCaller();
        generateAddressesBirthdayNextYear.setDisable(true);
        caller.generateAddressesBirthday(CURRENT_YEAR + 1);
        generateAddressesBirthdayNextYear.setDisable(false);
    }

    @FXML
    private void generateBirthdayLastYearInfos() {
        checkCaller();
        generateBirthdayLastYearInfos.setDisable(true);
        caller.generateBirthdayInfos(CURRENT_YEAR - 1);
        generateBirthdayLastYearInfos.setDisable(false);
    }

    @FXML
    private void generateBirthdayThisYearInfos() {
        checkCaller();
        generateBirthdayThisYearInfos.setDisable(true);
        caller.generateBirthdayInfos(CURRENT_YEAR);
        generateBirthdayThisYearInfos.setDisable(false);
    }

    @FXML
    private void generateBirthdayNextYearInfos() {
        checkCaller();
        generateBirthdayNextYearInfos.setDisable(true);
        caller.generateBirthdayInfos(CURRENT_YEAR + 1);
        generateBirthdayNextYearInfos.setDisable(false);
    }
}
