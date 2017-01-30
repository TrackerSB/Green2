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
import bayern.steinbrecher.green2.elements.CheckedTextField;
import bayern.steinbrecher.green2.people.Originator;
import bayern.steinbrecher.green2.utility.BindingUtility;
import bayern.steinbrecher.green2.utility.SepaUtility;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.FileNotFoundException;
import java.net.URL;
import java.text.MessageFormat;
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
    private CheckedTextField ibanTextField;
    @FXML
    private CheckedTextField bicTextField;
    @FXML
    private CheckedTextField creditorIdTextField;
    @FXML
    private CheckedTextField purposeTextField;
    @FXML
    private CheckedTextField messageIdTextField;
    @FXML
    private CheckedTextField pmtInfIdTextField;
    private List<CheckedTextField> checkedTextFields;
    @FXML
    private CheckedDatePicker executionDatePicker;
    @FXML
    private Label initiatingPartyLabel;
    @FXML
    private Label messageIdLabel;
    @FXML
    private Label pmtInfIdLabel;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String maxCharCount = DataProvider.getResourceValue("maxCharCount");
        initiatingPartyLabel.setText(DataProvider.getResourceValue("nameOfInitiatingParty") + "\n"
                + MessageFormat.format(maxCharCount, SepaUtility.MAX_CHAR_NAME_OF_INITIATING_PARTY));
        pmtInfIdLabel.setText(DataProvider.getResourceValue("pmtInfId") + "\n"
                + MessageFormat.format(maxCharCount, SepaUtility.MAX_CHAR_PMTINFID));

        String uniqueForDays = DataProvider.getResourceValue("uniqueForDays");
        messageIdLabel.setText(DataProvider.getResourceValue("messageId") + "\n"
                + MessageFormat.format(maxCharCount, SepaUtility.MAX_CHAR_MESSAGE_ID) + "\n"
                + MessageFormat.format(uniqueForDays, SepaUtility.UNIQUE_DAYS_PMTINFID));

        checkedTextFields = Arrays.asList(creatorTextField, creditorTextField, ibanTextField, bicTextField,
                creditorIdTextField, purposeTextField, messageIdTextField, pmtInfIdTextField);

        pmtInfIdTextField.setMaxColumnCount(SepaUtility.MAX_CHAR_PMTINFID);
        creatorTextField.setMaxColumnCount(SepaUtility.MAX_CHAR_NAME_OF_INITIATING_PARTY);
        messageIdTextField.setMaxColumnCount(SepaUtility.MAX_CHAR_MESSAGE_ID);
        /*messageIdTextField.setValidCondition(Bindings.createBooleanBinding(
                () -> SepaUtility.isValidMessageId(messageIdTextField.getText()), messageIdTextField.textProperty()));*/

        anyInputToLong.bind(checkedTextFields.stream()
                .map(CheckedTextField::toLongProperty)
                .reduce(BindingUtility.FALSE_BINDING, BooleanExpression::or, BooleanBinding::or));
        anyInputMissing.bind(checkedTextFields.stream()
                .map(CheckedTextField::emptyProperty)
                .reduce(BindingUtility.FALSE_BINDING, BooleanExpression::or, BooleanBinding::or)
                .or(executionDatePicker.emptyProperty()));
        valid.bind(executionDatePicker.validProperty()
                .and(checkedTextFields.stream()
                        .map(CheckedTextField::validProperty)
                        .reduce(BindingUtility.TRUE_BINDING, BooleanExpression::and, BooleanExpression::and)));

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
     * Returns the currently set originator. Returns {@code Optional.empty} if
     * the user did not confirm input.
     *
     * @return The currently set originator or {@code Optional.empty()}.
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
