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
package bayern.steinbrecher.gruen2.menu;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.main.Main;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private Button checkData;
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
        List<Object[]> params = IntStream.rangeClosed(CURRENT_YEAR - 1, CURRENT_YEAR + 1)
                .boxed()
                .map(i -> new Object[]{i})
                .collect(Collectors.toList());

        List<String> values
                = DataProvider.getResourceValues("ofBirthdayMember", params);
        values.addAll(DataProvider.getResourceValues(
                "groupedBirthdayMember", params));
        Button[] buttons = new Button[]{generateAddressesBirthdayLastYear,
            generateAddressesBirthdayThisYear,
            generateAddressesBirthdayNextYear, generateBirthdayLastYearInfos,
            generateBirthdayThisYearInfos, generateBirthdayNextYearInfos};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setText(values.get(i));
        }
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
