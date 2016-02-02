package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.Controller;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

/**
 * Controller for Main.fxml.
 *
 * @author Stefan Huber
 */
public class MainController extends Controller {

    private Main caller;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void startSepa() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        caller.startSepa();
    }

    @FXML
    private void generateSerialLetterData() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        caller.generateSerialLetterData();
    }

    public void setCaller(Main caller) {
        this.caller = caller;
    }
}
