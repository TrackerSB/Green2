package bayern.steinbrecher.gruen2.sepa.form;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import bayern.steinbrecher.gruen2.sepa.OriginatorIinfo;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Stefan Huber
 */
public class SepaController implements Initializable {

    private static final SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyy-MM-dd");
    private OriginatorIinfo eigeninfo = new OriginatorIinfo(DataProvider.getAppDataPath() + "/Eigeninfos.txt");
    private SepaModel sepa; //Das Model
    @FXML
    private TextField creditorTextField,
            ibanTextField,
            bicTextField,
            purposeTextField,
            trusterIdTextField;
    @FXML
    private CheckedTextField creatorTextField,
            messageIdTextField,
            pmtInfIdTextField;
    @FXML
    private DatePicker executionDatePicker;
    //FIXME Breite und Umrandung des DatePickers korrigieren
    @FXML
    private Label inputToLongLabel,
            missingInputLabel;
    @FXML
    private Button nextButton;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<TextField> textFields = new ArrayList<>();
    private ArrayList<CheckedTextField> checkedTextFields = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Collections.addAll(checkedTextFields, creatorTextField, messageIdTextField, pmtInfIdTextField);
        Collections.addAll(textFields, creditorTextField, ibanTextField, bicTextField, purposeTextField, trusterIdTextField);
        textFields.addAll(checkedTextFields);

        try {
            eigeninfo.readInEigeninfosTxt();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SepaController.class.getName()).log(Level.SEVERE, null, ex);
        }
        insertEigeninfosToFields();

        textFields.forEach(f -> f.textProperty().addListener(obs -> checkAllFields()));
        executionDatePicker.valueProperty().addListener(obs -> checkAllFields());
    }

    public void setModel(SepaModel sepa) {
        this.sepa = sepa;
    }

    @FXML
    private void next() {
        if (checkAllFields()) {
            updateEigeninfo();
            try {
                sepa.callCallable();
            } catch (NullPointerException ex) {
                Logger.getLogger(SepaController.class.getName()).log(Level.SEVERE, "Model was not set...", ex);
            }
            creatorTextField.getScene().getWindow().hide();
        }
    }

    private boolean checkAllFields() {
        boolean noMissingInput = checkMissingInput(executionDatePicker)
                //Ja koa lazy && mehr!. Sonst suachst wieda sooo ewig lang bis woaßd warum der DatePicker spinnt!!!
                & textFields.parallelStream().map(tf -> checkMissingInput(tf)).allMatch(b -> b),
                noInputToLong = checkedTextFields.parallelStream().map(ctf -> checkInputLength(ctf)).allMatch(b -> b);
        missingInputLabel.setVisible(!noMissingInput);
        inputToLongLabel.setVisible(!noInputToLong);
        boolean testResult = noInputToLong && noMissingInput;
        nextButton.setDisable(!testResult);
        return testResult;
    }

    /**
     * Liefert true, wenn die Inputlänge korrekt ist, d.h. der Test erfolgreich
     * war.
     */
    private boolean checkInputLength(CheckedTextField field) {
        boolean toLongInput = field.isToLong();
        Platform.runLater(() -> {
            DataProvider.changeBadInputLengthStyleClass(field, toLongInput);
        });
        return !toLongInput;
    }

    /**
     * Liefert true, wenn der Inhalt in picker nicht null ist, d.h. wenn der
     * Test erfolgreich war.
     */
    private boolean checkMissingInput(DatePicker picker) {
        boolean isMissingInput = picker.getValue() == null;
        Platform.runLater(() -> {
            DataProvider.changeMissingInputStyleClass(picker, isMissingInput);
        });
        return !isMissingInput;
    }

    /**
     * Liefert true, wenn der Inhalt in field nicht leer ist, d.h. wenn der Test
     * erfolgreich war.
     */
    private boolean checkMissingInput(TextField field) {
        boolean isMissingInput = field.getLength() == 0;
        Platform.runLater(() -> {
            DataProvider.changeMissingInputStyleClass(field, isMissingInput);
        });
        return !isMissingInput;
    }

    public OriginatorIinfo getOriginatorInfo() {
        return eigeninfo;
    }

    private void insertEigeninfosToFields() {
        creatorTextField.setText(eigeninfo.getCreator());
        creditorTextField.setText(eigeninfo.getCreditor());
        ibanTextField.setText(eigeninfo.getIban());
        bicTextField.setText(eigeninfo.getBic());
        purposeTextField.setText(eigeninfo.getPurpose());
        trusterIdTextField.setText(eigeninfo.getTrusterId());
        messageIdTextField.setText(eigeninfo.getMsgId());
        pmtInfIdTextField.setText(eigeninfo.getPmtInfId());
        try {
            executionDatePicker.setValue(LocalDate.parse(eigeninfo.getExecutiondate()));
        } catch (DateTimeParseException ex) {
            Logger.getLogger(SepaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateEigeninfo() {
        eigeninfo.setCreator(creatorTextField.getText());
        eigeninfo.setCreditor(creditorTextField.getText());
        eigeninfo.setIban(ibanTextField.getText());
        eigeninfo.setBic(bicTextField.getText());
        eigeninfo.setPurpose(purposeTextField.getText());
        eigeninfo.setTrusterId(trusterIdTextField.getText());
        eigeninfo.setMsgId(messageIdTextField.getText());
        eigeninfo.setExecutiondate(executionDatePicker.getValue().toString());
        eigeninfo.setPmtInfId(pmtInfIdTextField.getText());
        eigeninfo.updateEigeninfosTxt();
    }

    @FXML
    private void generateMessageId() {
        Calendar today = Calendar.getInstance();
        String datestring = yearMonthDayFormat.format(today.getTime());
        messageIdTextField.setText(datestring + " Mitgliedsbeitrag " + today.get(Calendar.YEAR));
    }
}
