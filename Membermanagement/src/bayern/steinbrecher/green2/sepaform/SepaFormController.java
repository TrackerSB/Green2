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

package bayern.steinbrecher.green2.sepaform;

import bayern.steinbrecher.green2.CheckedController;
import bayern.steinbrecher.green2.data.DataProvider;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.elements.CheckedDatePicker;
import bayern.steinbrecher.green2.elements.buttons.HelpButton;
import bayern.steinbrecher.green2.elements.sepa.CreditorIdTextField;
import bayern.steinbrecher.green2.elements.sepa.IbanTextField;
import bayern.steinbrecher.green2.elements.sepa.MessageIdTextField;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import bayern.steinbrecher.green2.people.Originator;
import bayern.steinbrecher.green2.utility.BindingUtility;
import bayern.steinbrecher.green2.utility.SepaUtility;
import javafx.fxml.FXML;

import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents Controller for SepaForm.fxml.
 *
 * @author Stefan Huber
 */
public class SepaFormController extends CheckedController {

    private Originator originator;
    @FXML
    private CheckedTextField creatorTextField;
    @FXML
    private CheckedTextField creditorTextField;
    @FXML
    private IbanTextField ibanTextField;
    @FXML
    private CheckedTextField bicTextField;
    @FXML
    private CreditorIdTextField creditorIdTextField;
    @FXML
    private CheckedTextField purposeTextField;
    @FXML
    private MessageIdTextField messageIdTextField;
    @FXML
    private CheckedTextField pmtInfIdTextField;
    private List<CheckedTextField> checkedTextFields;
    @FXML
    private CheckedDatePicker executionDatePicker;
    @FXML
    private HelpButton creatorHelpButton;
    @FXML
    private HelpButton messageIdHelpButton;
    @FXML
    private HelpButton pmtInfIdHelpButton;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkedTextFields = Arrays.asList(creatorTextField, creditorTextField, ibanTextField, bicTextField,
                creditorIdTextField, purposeTextField, messageIdTextField, pmtInfIdTextField);

        pmtInfIdTextField.setMaxColumnCount(SepaUtility.MAX_CHAR_PMTINFID);
        pmtInfIdHelpButton.setHelpMessage(DataProvider.getResourceValue(
                "helpPmtInfId", SepaUtility.UNIQUE_MONTH_PMTINFID, SepaUtility.MAX_CHAR_PMTINFID));
        creatorTextField.setMaxColumnCount(SepaUtility.MAX_CHAR_NAME_OF_CREATOR);
        creatorHelpButton.setHelpMessage(
                DataProvider.getResourceValue("helpCreator", SepaUtility.MAX_CHAR_NAME_OF_CREATOR));
        messageIdHelpButton.setHelpMessage(DataProvider.getResourceValue(
                "helpMessageId", SepaUtility.MAX_CHAR_MESSAGE_ID, SepaUtility.UNIQUE_DAYS_MESSAGEID));

        anyInputToLong.bind(BindingUtility.reduceOr(checkedTextFields.stream()
                .map(CheckedTextField::toLongProperty)));
        anyInputMissing.bind(executionDatePicker.emptyProperty()
                .or(BindingUtility.reduceOr(checkedTextFields.stream()
                        .map(CheckedTextField::emptyProperty))));
        valid.bind(executionDatePicker.validProperty()
                .and(BindingUtility.reduceAnd(checkedTextFields.stream()
                        .map(CheckedTextField::validProperty))));

        Profile profile = DataProvider.getProfile();

        try {
            originator = Originator.readOriginatorInfo(profile.getOriginatorInfoPath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SepaFormController.class.getName()).log(Level.INFO, null, ex);
            originator = new Originator(profile.getOriginatorInfoPath());
        }
        creatorTextField.setText(originator.getCreator());
        creditorTextField.setText(originator.getCreditor());
        ibanTextField.setText(originator.getIban());
        bicTextField.setText(originator.getBic());
        creditorIdTextField.setText(originator.getCreditorId());
        purposeTextField.setText(originator.getPurpose());
        messageIdTextField.setText(originator.getMsgId());
        pmtInfIdTextField.setText(originator.getPmtInfId());
        executionDatePicker.setValue(originator.getExecutiondate());
    }

    @FXML
    private void generateMessageId() {
        messageIdTextField.setText(DateTimeFormatter.ISO_DATE.format(LocalDate.now()) + " "
                + DataProvider.getResourceValue("contributions_sepaChars") + " "
                + Calendar.getInstance().get(Calendar.YEAR));
    }

    private void saveOriginator() {
        if (isValid()) {
            originator.setCreator(creatorTextField.getText());
            originator.setCreditor(creditorTextField.getText());
            originator.setIban(ibanTextField.getText());
            originator.setBic(bicTextField.getText());
            originator.setCreditorId(creditorIdTextField.getText());
            originator.setPurpose(purposeTextField.getText());
            originator.setMsgId(messageIdTextField.getText());
            originator.setPmtInfId(pmtInfIdTextField.getText());
            originator.setExecutiondate(executionDatePicker.getValue());
            originator.saveOriginator();
        }
    }

    @FXML
    private void ready() {
        checkStage();
        if (isValid()) {
            saveOriginator();
            stage.close();
        }
    }

    /**
     * Returns the currently set originator. Returns {@link Optional#empty()} if
     * the user did not confirm input.
     *
     * @return The currently set originator or {@link Optional#empty()}.
     */
    public Optional<Originator> getOriginator() {
        if (userAbborted()) {
            return Optional.empty();
        } else {
            saveOriginator();
            return Optional.of(originator);
        }
    }
}
