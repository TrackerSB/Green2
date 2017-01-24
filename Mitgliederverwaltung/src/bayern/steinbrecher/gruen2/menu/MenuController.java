/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.gruen2.menu;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.elements.CheckedIntegerSpinner;
import bayern.steinbrecher.gruen2.main.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

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
    private Button checkData;
    @FXML
    private Button generateAddressesBirthday;
    @FXML
    private Button generateBirthdayInfos;
    @FXML
    private CheckedIntegerSpinner yearSpinner;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        yearSpinner.getValueFactory().setValue(CURRENT_YEAR + 1);
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
    private void checkData() {
        checkCaller();
        checkData.setDisable(true);
        caller.checkData();
        checkData.setDisable(false);
    }

    @FXML
    private void generateAddressesAll() {
        checkCaller();
        generateAllAddresses.setDisable(true);
        caller.generateAddressesAll();
        generateAllAddresses.setDisable(false);
    }

    @FXML
    private void generateAddressesBirthday() {
        checkCaller();
        if (yearSpinner.isValid()) {
            caller.generateAddressesBirthday(yearSpinner.getValue());
        }
    }

    @FXML
    private void generateBirthdayInfos() {
        checkCaller();
        if (yearSpinner.isValid()) {
            caller.generateBirthdayInfos(yearSpinner.getValue());
        }
    }
}
