package bayern.steinbrecher.gruen2;

import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 * Represents a controller all other controller have to extend.
 *
 * @author Stefan Huber
 */
public abstract class Controller implements Initializable {

    /**
     * The stage the controller has to interact with.
     */
    protected Stage stage = null;
    /**
     * Only {@code true} when the user explicitly confirmed his input. (E.g.
     * pressing "Login" or "Auswählen" confirms; closing the window with "X"
     * does not confirm.)
     */
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

    /**
     * Throws a {@code IllegalStateException} only if stage is {@code null}.
     */
    public void checkStage() {
        if (stage == null) {
            throw new IllegalStateException(
                    "You have to call setStage(...) first");
        }
    }

    /**
     * Checks whether the user confirmed his input.
     *
     * @return {@code true} only if the user confirmed his input explicitly.
     */
    public boolean userConfirmed() {
        return userConfirmed;
    }
}
