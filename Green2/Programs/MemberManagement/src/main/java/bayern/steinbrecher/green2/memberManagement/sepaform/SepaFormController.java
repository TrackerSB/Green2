package bayern.steinbrecher.green2.memberManagement.sepaform;

import bayern.steinbrecher.checkedElements.CheckedDatePicker;
import bayern.steinbrecher.checkedElements.buttons.HelpButton;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.checkedElements.textfields.sepa.CreditorIdTextField;
import bayern.steinbrecher.checkedElements.textfields.sepa.IbanTextField;
import bayern.steinbrecher.checkedElements.textfields.sepa.MessageIdTextField;
import bayern.steinbrecher.green2.memberManagement.people.Originator;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.javaUtility.BindingUtility;
import bayern.steinbrecher.sepaxmlgenerator.MessageId;
import bayern.steinbrecher.sepaxmlgenerator.PaymentInformationId;
import bayern.steinbrecher.sepaxmlgenerator.SepaDocumentDescription;
import bayern.steinbrecher.wizard.WizardPageController;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Represents Controller for SepaForm.fxml.
 *
 * @author Stefan Huber
 */
public class SepaFormController extends WizardPageController<Optional<Originator>> {

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
    @FXML
    private CheckedDatePicker executionDatePicker;
    @FXML
    private HelpButton creatorHelpButton;
    @FXML
    private HelpButton messageIdHelpButton;
    @FXML
    private HelpButton pmtInfIdHelpButton;

    @FXML
    public void initialize() {
        List<CheckedTextField> checkedTextFields = Arrays.asList(creatorTextField, creditorTextField, ibanTextField,
                bicTextField, creditorIdTextField, purposeTextField, messageIdTextField, pmtInfIdTextField);

        pmtInfIdTextField.setMaxColumnCount(PaymentInformationId.MAX_CHAR_PMTINFID);
        pmtInfIdHelpButton.setHelpMessage(EnvironmentHandler.getResourceValue(
                "helpPmtInfId", PaymentInformationId.UNIQUE_MONTH_PMTINFID, PaymentInformationId.MAX_CHAR_PMTINFID));
        creatorTextField.setMaxColumnCount(SepaDocumentDescription.MAX_CHAR_NAME_FIELD);
        creatorHelpButton.setHelpMessage(
                EnvironmentHandler.getResourceValue("helpCreator", SepaDocumentDescription.MAX_CHAR_NAME_FIELD));
        creditorTextField.setMaxColumnCount(SepaDocumentDescription.MAX_CHAR_NAME_FIELD);
        messageIdHelpButton.setHelpMessage(EnvironmentHandler.getResourceValue(
                "helpMessageId", MessageId.UNIQUE_DAYS_MESSAGEID, MessageId.MAX_CHAR_MESSAGE_ID));

        bindValidProperty(executionDatePicker.validProperty()
                .and(BindingUtility.reduceAnd(checkedTextFields.stream()
                        .map(CheckedTextField::validProperty))));

        originator = Originator.readCurrentOriginatorInfo().orElse(new Originator());
        creatorTextField.setText(originator.getCreator());
        creditorTextField.setText(originator.getCreditor());
        ibanTextField.setText(originator.getIban());
        bicTextField.setText(originator.getBic());
        creditorIdTextField.setText(originator.getCreditorId());
        purposeTextField.setText(originator.getPurpose());
        messageIdTextField.setText(originator.getMsgId());
        pmtInfIdTextField.setText(originator.getPmtInfId());
        executionDatePicker.setValue(originator.getExecutionDate());

        Platform.runLater(() -> creatorTextField.requestFocus());
    }

    @FXML
    @SuppressWarnings("unused")
    private void generateMessageId() {
        messageIdTextField.setText(DateTimeFormatter.ISO_DATE.format(LocalDate.now()) + " "
                + EnvironmentHandler.getResourceValue("contributions_sepaChars") + " "
                + Calendar.getInstance().get(Calendar.YEAR));
    }

    /**
     * If the currently inserted data is complete and valid it is written to the model representing the originator and
     * the file holding the model is updated.
     */
    protected void saveOriginator() {
        if (isValid()) {
            originator.setCreator(creatorTextField.getText());
            originator.setCreditor(creditorTextField.getText());
            originator.setIban(ibanTextField.getText());
            originator.setBic(bicTextField.getText());
            originator.setCreditorId(creditorIdTextField.getText());
            originator.setPurpose(purposeTextField.getText());
            originator.setMsgId(messageIdTextField.getText());
            originator.setPmtInfId(pmtInfIdTextField.getText());
            originator.setExecutionDate(executionDatePicker.getValue());
            originator.saveOriginator();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Originator> calculateResult() {
        saveOriginator();
        return Optional.of(originator);
    }
}
