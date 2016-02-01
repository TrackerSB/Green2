package bayern.steinbrecher.gruen2;

import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 * Represents a controller.
 *
 * @author Stefan Huber
 */
public abstract class Controller implements Initializable {

    protected Stage stage = null;
    protected boolean userConfirmed = false;

    /**
     * Sets the stage the conroller can refer to. (E.g. for closing the stage)
     *
     * @param stage The stage to refer to.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                userConfirmed = false;
            }
        });
    }

    public boolean userConfirmed() {
        return userConfirmed;
    }
}
