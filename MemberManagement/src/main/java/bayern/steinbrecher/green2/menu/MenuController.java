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
package bayern.steinbrecher.green2.menu;

import bayern.steinbrecher.green2.Controller;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.green2.membermanagement.MemberManagement;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.VersionHandler;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

/**
 * Controller for Menu.fxml.
 *
 * @author Stefan Huber
 */
public class MenuController extends Controller {

    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private MemberManagement caller;
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
    @FXML
    private javafx.scene.control.Menu licensesMenu;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        StringBinding yearBinding = Bindings.createStringBinding(() -> {
            String year = "?";
            if (yearSpinner.isValid()) {
                year = yearSpinner.getValue().toString();
            }
            return year;
        }, yearSpinner.validProperty(), yearSpinner.valueProperty());
        generateBirthdayInfos.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("groupedBirthdayMember") + " ")
                        .concat(yearBinding));
        generateAddressesBirthday.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("birthdayExpression") + " ")
                        .concat(yearBinding));
        yearSpinner.getValueFactory().setValue(CURRENT_YEAR + 1);

        EnvironmentHandler.getLicenses().stream().forEach((license) -> {
            MenuItem item = new MenuItem(license.getName());
            item.setOnAction(aevt -> {
                try {
                    license.setWritable(false, false);
                    Desktop.getDesktop().open(license);
                } catch (IOException ex) {
                    Logger.getLogger(MenuController.class.getName()).log(Level.WARNING, "Could not open license", ex);
                }
            });
            licensesMenu.getItems().add(item);
        });
    }

    /**
     * Sets the caller which provides the functionality this controller has to use.
     *
     * @param caller The provider of the functionality.
     */
    public void setCaller(MemberManagement caller) {
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

    @FXML
    private void showCredits() {
        String credits = EnvironmentHandler.getResourceValue("credits");
        Alert alert = DialogUtility.createMessageAlert(
                stage, EnvironmentHandler.getResourceValue("creditsContent"), null, credits, credits);
        alert.show();
    }

    @FXML
    private void showVersion() {
        String version = EnvironmentHandler.getResourceValue("version");
        Alert alert = DialogUtility.createInfoAlert(stage, VersionHandler.getVersion(), version, version, version);
        alert.show();
    }
}
