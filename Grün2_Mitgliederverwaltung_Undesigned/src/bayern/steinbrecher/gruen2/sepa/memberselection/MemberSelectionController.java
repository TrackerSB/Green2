package bayern.steinbrecher.gruen2.sepa.memberselection;

import bayern.steinbrecher.gruen2.data.DataProvider;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * @author Stefan Huber
 */
public class MemberSelectionController implements Initializable {

    private MemberSelectionModel memberSelectionModel;
    @FXML
    private TextField contributionTextField;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    @FXML
    private ListView memberListView;
    @FXML
    private Label missingInputLabel,
            badInputLabel;
    @FXML
    private Button readyButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        contributionTextField.setTextFormatter(new TextFormatter<>(c -> {
            ParsePosition parsePosition = new ParsePosition(0);
            Object parsed = DECIMAL_FORMAT.parse(c.getControlNewText(), parsePosition);

            return (parsed == null || parsePosition.getIndex() < c.getControlNewText().length()) ? null : c;
        }));
        contributionTextField.textProperty().addListener(obs -> {
            checkAllFields();
        });
    }

    @FXML
    private void closeIfValid() {
        if (checkAllFields()) {
            try {
                memberSelectionModel.callCallable();
            } catch (NullPointerException ex) {
                Logger.getLogger(MemberSelectionController.class.getName()).log(Level.SEVERE, "Model was not set...", ex);
            }
            contributionTextField.getScene().getWindow().hide();
        }
    }

    /**
     * Setzt das Model und aktualisiert die Liste mit den Informationen des
     * neuen Models
     *
     * @param memberSelectionModel Das neue Model
     */
    public void setModel(MemberSelectionModel memberSelectionModel) {
        this.memberSelectionModel = memberSelectionModel;
        updateComponents();
    }

    private boolean checkAllFields() {
        boolean isBadInput = false,
                isInputMissing = false;
        try {
            Double contribution = Double.parseDouble(contributionTextField.getText().replace(',', '.'));
            if (contribution <= 0) {
                throw new NumberFormatException("Value must be greater than zero");
            } else {
                memberSelectionModel.setContribution(contribution);
                DataProvider.changeBadInputStyleClass(contributionTextField, false);
            }
        } catch (NumberFormatException ex) {
            isBadInput = true;
            DataProvider.changeBadInputStyleClass(contributionTextField, true);
        }

        if (memberSelectionModel.getSelectedMember().isEmpty()) {
            isInputMissing = true;
            DataProvider.changeMissingInputStyleClass(memberListView, true);
        } else {
            DataProvider.changeMissingInputStyleClass(memberListView, false);
        }

        readyButton.setDisable(isBadInput || isInputMissing);
        missingInputLabel.setVisible(isInputMissing);
        badInputLabel.setVisible(isBadInput);
        return !(isBadInput || isInputMissing);
    }

    private void updateComponents() {
        memberListView.getItems().clear();
        memberListView.getItems().addAll(
                memberSelectionModel.getAllMember().stream().sorted().map(m -> {
                    CheckBox checkbox = new CheckBox(m.toString());
                    checkbox.selectedProperty().addListener(obs -> {
                        if (checkbox.isSelected()) {
                            memberSelectionModel.setSelectedMember(m);
                        } else {
                            memberSelectionModel.setUnselectedMember(m);
                        }
                        checkAllFields();
                    });
                    return checkbox;
                }).toArray()
        );
    }

    @FXML
    private void selectAllMember() {
        memberListView.getItems().parallelStream().forEach(i -> {
            ((CheckBox) i).setSelected(true);
        });
    }
}
