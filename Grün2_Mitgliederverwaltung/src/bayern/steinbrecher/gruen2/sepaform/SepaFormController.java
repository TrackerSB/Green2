package bayern.steinbrecher.gruen2.sepaform;

import bayern.steinbrecher.gruen2.Controller;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Represents Controller for SepaForm.fxml.
 *
 * @author Stefan Huber
 */
public class SepaFormController extends Controller {

    private static final SimpleDateFormat YEAR_MONTH_DAY
            = new SimpleDateFormat("yyyy-MM-dd");
    private static final int UNIQUE_DAYS_PMTINFID = 15;
    public static final int bla = 20;
    /**
     * Used as identity for sequence of or bindings.
     */
    private static final BooleanBinding FALSE_BINDING = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return false;
        }
    };
    public static final int MAX_CHAR_MESSAGE_ID = 35;
    public static final int MAX_CHAR_PMTINFID = 35;
    public static final int MAX_CHAR_NAME_OF_INITIATING_PARTY = 70;
    private final BooleanProperty anyInputToLong
            = new SimpleBooleanProperty(this, "anyInputToLong");
    private final BooleanProperty anyInputMissing
            = new SimpleBooleanProperty(this, "anyInputMissing");
    private final BooleanProperty valid
            = new SimpleBooleanProperty(this, "valid");
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

        String originatorInfoPath = DataProvider.getAppDataPath()
                + "/originator.properties";
        try {
            originator = Originator.readOriginatorInfo(originatorInfoPath);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SepaFormController.class.getName())
                    .log(Level.SEVERE, null, ex);
            originator = new Originator(originatorInfoPath);
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

    @FXML
    private void ready() {
        checkStage();
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
            userConfirmed = true;
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
        if (userConfirmed) {
            return Optional.of(originator);
        }
        return Optional.empty();
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

    /**
     * Returns the property reprsenting a boolean value indicating whether any
     * input is to long.
     *
     * @return The property reprsenting a boolean value indicating whether any
     * input is to long.
     */
    public ReadOnlyBooleanProperty anyInputToLongProperty() {
        return anyInputToLong;
    }

    /**
     * Checks whether any input is to long.
     *
     * @return {@code true} only if any input is to long.
     */
    public boolean isAnyInputToLong() {
        return anyInputToLong.get();
    }

    /**
     * Returns the property reprsenting a boolean value indicating whether any
     * input is missing.
     *
     * @return The property reprsenting a boolean value indicating whether any
     * input is missing.
     */
    public ReadOnlyBooleanProperty anyInputMissingProperty() {
        return anyInputMissing;
    }

    /**
     * Checks whether any input is missing.
     *
     * @return {@code true} only if any input is missing.
     */
    public boolean isAnyInputMissing() {
        return anyInputMissing.get();
    }

    /**
     * Returns the property reprsenting a boolean value indicating whether all
     * input is valid.
     *
     * @return The property reprsenting a boolean value indicating whether all
     * input is valid.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether all inserted data is valid.
     *
     * @return {@code true} only if all inserted data is valid.
     */
    public boolean isValid() {
        return valid.get();
    }
}
