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
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
    /**
     * Used as identity for sequence of or bindings.
     */
    private static final BooleanBinding FALSE_BINDING = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return false;
        }
    };
    private final ReadOnlyIntegerProperty maxCharMessageId
            = new SimpleIntegerProperty(this, "maxCharMessageId", 35);
    private final ReadOnlyIntegerProperty maxCharPmtInfId
            = new SimpleIntegerProperty(this, "maxCharPmtInfId", 35);
    private final ReadOnlyIntegerProperty maxCharNamePresenter
            = new SimpleIntegerProperty(this, "maxCharNamePresenter", 70);
    private BooleanProperty anyInputToLong = new SimpleBooleanProperty();
    private BooleanProperty anyInputMissing = new SimpleBooleanProperty();
    private BooleanProperty valid = new SimpleBooleanProperty();
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
    private Label presenterLabel;
    @FXML
    private Label messageIdLabel;
    @FXML
    private Label pmtInfIdLabel;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String maxCharCount
                = DataProvider.RESOURCE_BUNDLE.getString("maxCharCount");
        presenterLabel.setText(
                DataProvider.RESOURCE_BUNDLE.getString("nameOfPresenter") + "\n"
                + MessageFormat.format(
                        maxCharCount, getMaxCharNamePresenter()));
        pmtInfIdLabel.setText(
                DataProvider.RESOURCE_BUNDLE.getString("pmtInfId") + "\n"
                + MessageFormat.format(maxCharCount, getMaxCharPmtInfId()));

        String uniqueForDays
                = DataProvider.RESOURCE_BUNDLE.getString("uniqueForDays");
        messageIdLabel.setText(
                DataProvider.RESOURCE_BUNDLE.getString("messageId") + "\n"
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
                .or(executionDatePicker.validProperty()));
        valid.bind((anyInputToLong.or(anyInputToLong)).not());

        /*
         * Rawtype needed for adding as ChangeListener<String> and
         * ChangeListener<Boolean>
         */
 /*ChangeListener cl = (obs, oldVal, newVal) -> {
            boolean anyInputToLong = checkedTextFields.parallelStream()
                    .anyMatch(ctf -> !ctf.isValid() && ctf.isToLong());
            inputToLongLabel.setVisible(anyInputToLong);
            boolean anyInputMissing = checkedTextFields.parallelStream()
                    .anyMatch(ctf -> {
                        return !ctf.isValid() && ctf.isEmpty();
                    }) || !executionDatePicker.isValid();
            missingInputLabel.setVisible(anyInputMissing);
            readyButton.setDisable(anyInputToLong || anyInputMissing
                    || !executionDatePicker.isValid());
        };
        checkedTextFields.parallelStream()
                .map(CheckedTextField::textProperty)
                .forEach(tp -> tp.addListener(cl));
        executionDatePicker.validProperty().addListener(cl);*/
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
                + DataProvider.RESOURCE_BUNDLE.getString("contributions") + " "
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
     * @return
     */
    public Optional<Originator> getOriginator() {
        if (userConfirmed) {
            return Optional.of(originator);
        }
        return Optional.empty();
    }

    public ReadOnlyIntegerProperty maxCharMessageIdProperty() {
        return maxCharMessageId;
    }

    public int getMaxCharMessageId() {
        return maxCharMessageId.get();
    }

    public ReadOnlyIntegerProperty maxCharPmtInfIdProperty() {
        return maxCharPmtInfId;
    }

    public int getMaxCharPmtInfId() {
        return maxCharPmtInfId.get();
    }

    public ReadOnlyIntegerProperty maxCharNamePresenterProperty() {
        return maxCharNamePresenter;
    }

    public int getMaxCharNamePresenter() {
        return maxCharNamePresenter.get();
    }

    public ReadOnlyBooleanProperty anyInputToLongProperty() {
        return anyInputToLong;
    }

    public boolean isAnyInputToLong() {
        return anyInputToLong.get();
    }

    public ReadOnlyBooleanProperty anyInputMissingProperty() {
        return anyInputMissing;
    }

    public boolean isAnyInputMissing() {
        return anyInputMissing.get();
    }

    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    public boolean isValid() {
        return valid.get();
    }
}
