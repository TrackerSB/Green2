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
package bayern.steinbrecher.gruen2.sepaform;

import bayern.steinbrecher.gruen2.CheckedController;
import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.elements.CheckedDatePicker;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.people.Originator;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Represents Controller for SepaForm.fxml.
 *
 * @author Stefan Huber
 */
public class SepaFormController extends CheckedController {

    private static final SimpleDateFormat YEAR_MONTH_DAY
            = new SimpleDateFormat("yyyy-MM-dd");
    private static final int UNIQUE_DAYS_PMTINFID = 15;
    /**
     * The maximum length of the message id.
     */
    public static final int MAX_CHAR_MESSAGE_ID = 35;
    /**
     * The maximum length of payment information id (PmtInfId).
     */
    public static final int MAX_CHAR_PMTINFID = 35;
    /**
     * The maximum length of the name of the party creating the SEPA Direct
     * Debit.
     */
    public static final int MAX_CHAR_NAME_OF_INITIATING_PARTY = 70;
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
    private CheckedTextField trusterIdTextField;
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
        initiatingPartyLabel.setText(
                DataProvider.getResourceValue("nameOfInitiatingParty") + "\n"
                + MessageFormat.format(
                        maxCharCount, getMaxCharNameInitiatingParty()));
        pmtInfIdLabel.setText(
                DataProvider.getResourceValue("pmtInfId") + "\n"
                + MessageFormat.format(maxCharCount, getMaxCharPmtInfId()));

        String uniqueForDays = DataProvider.getResourceValue("uniqueForDays");
        messageIdLabel.setText(DataProvider.getResourceValue("messageId") + "\n"
                + MessageFormat.format(maxCharCount, getMaxCharMessageId())
                + "\n"
                + MessageFormat.format(uniqueForDays, UNIQUE_DAYS_PMTINFID));

        checkedTextFields = Arrays.asList(creatorTextField, creditorTextField,
                ibanTextField, bicTextField, trusterIdTextField,
                purposeTextField, messageIdTextField, pmtInfIdTextField);

        anyInputToLong.bind(checkedTextFields.stream()
                .map(ctf -> ctf.toLongProperty())
                .reduce(FALSE_BINDING, (bind, prop) -> bind.or(prop),
                        BooleanBinding::or));
        anyInputMissing.bind(checkedTextFields.stream()
                .map(ctf -> ctf.emptyProperty())
                .reduce(FALSE_BINDING, (bind, prop) -> bind.or(prop),
                        BooleanBinding::or)
                .or(executionDatePicker.emptyProperty()));
        valid.bind(((anyInputToLong.or(anyInputMissing)).not())
                .and(executionDatePicker.validProperty()));

        DataProvider profile = DataProvider.getProfile();

        try {
            originator = Originator.readOriginatorInfo(
                    profile.getOriginatorInfoPath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SepaFormController.class.getName())
                    .log(Level.INFO, null, ex);
            originator = new Originator(profile.getOriginatorInfoPath());
        }
        creatorTextField.setText(originator.getCreator());
        creditorTextField.setText(originator.getCreditor());
        ibanTextField.setText(originator.getIban());
        bicTextField.setText(originator.getBic());
        trusterIdTextField.setText(originator.getTrusterId());
        purposeTextField.setText(originator.getPurpose());
        messageIdTextField.setText(originator.getMsgId());
        pmtInfIdTextField.setText(originator.getPmtInfId());
        executionDatePicker.setValue(originator.getExecutiondate());
    }

    @FXML
    private void generateMessageId() {
        messageIdTextField.setText(YEAR_MONTH_DAY.format(new Date()) + " "
                + DataProvider.getResourceValue("contributions") + " "
                + Calendar.getInstance().get(Calendar.YEAR));
    }

    private void saveOriginator() {
        if (isValid()) {
            originator.setCreator(creatorTextField.getText());
            originator.setCreditor(creditorTextField.getText());
            originator.setIban(ibanTextField.getText());
            originator.setBic(bicTextField.getText());
            originator.setTrusterId(trusterIdTextField.getText());
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

    /**
     * Returns the constant {@code MAX_CHAR_MESSAGE_ID}. Only needed for use in
     * FXML.
     *
     * @return The constant {@code MAX_CHAR_MESSAGE_ID}.
     */
    public int getMaxCharMessageId() {
        return MAX_CHAR_MESSAGE_ID;
    }

    /**
     * Returns the constant {@code MAX_CHAR_PMTINFID}. Only needed for use in
     * FXML.
     *
     * @return The constant {@code MAX_CHAR_PMTINFID}.
     */
    public int getMaxCharPmtInfId() {
        return MAX_CHAR_PMTINFID;
    }

    /**
     * Returns the constant {@code MAX_CHAR_NAME_OF_INITATING_PARTY}. Only
     * needed for use in FXML.
     *
     * @return The constant {@code MAX_CHAR_NAME_OF_INITATING_PARTY}.
     */
    public int getMaxCharNameInitiatingParty() {
        return MAX_CHAR_NAME_OF_INITIATING_PARTY;
    }
}
