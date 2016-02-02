package bayern.steinbrecher.gruen2.main;

import bayern.steinbrecher.gruen2.Controller;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for Main.fxml.
 *
 * @author Stefan Huber
 */
public class MainController extends Controller {

    private Main caller;
    private ExecutorService exserv = Executors.newCachedThreadPool();
    @FXML
    private Button generateSepaButton;
    @FXML
    private Button generateDataButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void startSepa() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        exserv.execute(() -> {
            generateSepaButton.setDisable(true);
            caller.startSepa();
            generateSepaButton.setDisable(false);
        });
    }

    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
        stage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                exserv.shutdownNow();
            }
        });
    }

    @FXML
    private void generateSerialLetterData() {
        if (caller == null) {
            throw new IllegalStateException("caller is not set");
        }
        exserv.execute(() -> {
            generateDataButton.setDisable(true);
            caller.generateSerialLetterData();
            generateDataButton.setDisable(false);
        });
    }

    public void setCaller(Main caller) {
        this.caller = caller;
    }
}
