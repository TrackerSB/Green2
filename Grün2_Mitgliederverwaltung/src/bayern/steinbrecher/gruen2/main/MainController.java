package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.sepa.form.SepaModel;
import bayern.steinbrecher.gruen2.serialLetters.DataForSerialLetters;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Stefan Huber
 */
public class MainController implements Initializable {

    private Stage stage = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void showSepaDialog() {
        try {
            new SepaModel(DataProvider.useSsh()).start(new Stage());
            stage.close();
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void showSerialLettersDialog() {
        try {
            new DataForSerialLetters(DataProvider.useSsh()).start(new Stage());
            stage.close();
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
