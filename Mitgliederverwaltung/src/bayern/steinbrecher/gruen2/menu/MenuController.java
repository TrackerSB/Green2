/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
