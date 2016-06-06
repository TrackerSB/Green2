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
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javax.xml.soap.MessageFactory;

/**
 * Represents Controller for SepaForm.fxml.
 *
 * @author Stefan Huber
 */
public class SepaFormController extends Controller {

    private static final SimpleDateFormat YEAR_MONTH_DAY
            = new SimpleDateFormat("yyyy-MM-dd");
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
    private Label inputTooLongLabel;
    @FXML
    private Label missingInputLabel;
    @FXML
    private Label presenterLabel;
    @FXML
    private Label messageIdLabel;
    @FXML
    private Label pmtInfIdLabel;
    @FXML
    private Button readyButton;
    private ReadOnlyIntegerProperty maxCharMessageId = new SimpleIntegerProperty(this, "maxCharMessageId", 35);
    private ReadOnlyIntegerProperty maxCharPmtInfId = new SimpleIntegerProperty(this, "maxCharPmtInfId", 35);

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String maxCharCount = DataProvider.RESOURCE_BUNDLE.getString("maxCharCount");
        presenterLabel.setText(DataProvider.RESOURCE_BUNDLE.getString("nameOfPresenter") + "\n" + MessageFormat.format(maxCharCount, 70));
        pmtInfIdLabel.setText(DataProvider.RESOURCE_BUNDLE.getString("pmtInfId") + "\n" + MessageFormat.format(maxCharCount, getMaxCharPmtInfId()));
        
        String uniqueForDays = DataProvider.RESOURCE_BUNDLE.getString("uniqueForDays");
        messageIdLabel.setText(DataProvider.RESOURCE_BUNDLE.getString("messageId") + "\n" + MessageFormat.format(maxCharCount, getMaxCharMessageId()) + "\n" + MessageFormat.format(uniqueForDays, 15));
        
        checkedTextFields = Arrays.asList(creatorTextField, creditorTextField,
                ibanTextField, bicTextField, trusterIdTextField,
                purposeTextField, messageIdTextField, pmtInfIdTextField);

        /*
         * Rawtype needed for adding as ChangeListener<String> and
         * ChangeListener<Boolean>
         */
        ChangeListener cl = (obs, oldVal, newVal) -> {
            boolean anyInputTooLong = checkedTextFields.parallelStream()
                    .anyMatch(ctf -> !ctf.isValid() && ctf.isTooLong());
            inputTooLongLabel.setVisible(anyInputTooLong);
            boolean anyInputMissing = checkedTextFields.parallelStream()
                    .anyMatch(ctf -> {
                        return !ctf.isValid() && ctf.isEmpty();
                    }) || !executionDatePicker.isValid();
            missingInputLabel.setVisible(anyInputMissing);
            readyButton.setDisable(anyInputTooLong || anyInputMissing
                    || !executionDatePicker.isValid());
        };
        checkedTextFields.parallelStream()
                .map(CheckedTextField::textProperty)
                .forEach(tp -> tp.addListener(cl));
        executionDatePicker.validProperty().addListener(cl);

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
        messageIdTextField.setText(YEAR_MONTH_DAY.format(new Date())
                + " Mitgliedsbeitrag "
                + Calendar.getInstance().get(Calendar.YEAR));
    }

    @FXML
    private void ready() {
        checkStage();
        if (!readyButton.isDisabled()) {
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
     * @return
     */
    public Optional<Originator> getOriginator() {
        if (userConfirmed) {
            return Optional.of(originator);
        }
        return Optional.empty();
    }
    
    public ReadOnlyIntegerProperty maxCharMessageIdProperty(){
        return maxCharMessageId;
    }

    public int getMaxCharMessageId() {
        return maxCharMessageId.get();
    }
    
    public ReadOnlyIntegerProperty maxCharPmtInfIdProperty(){
        return maxCharPmtInfId;
    }

    public int getMaxCharPmtInfId() {
        return maxCharPmtInfId.get();
    }
}
