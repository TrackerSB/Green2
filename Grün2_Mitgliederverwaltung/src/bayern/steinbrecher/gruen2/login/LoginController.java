package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.Controller;
import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;

/**
 * Represents a controller for a login.
 *
 * @author Stefan Huber
 */
public abstract class LoginController extends Controller {

    private static final BooleanBinding BINDING_IDENTITY
            = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return true;
        }
    };
    protected BooleanProperty allInputValid
            = new SimpleBooleanProperty(this, "allInputValid");
    protected List<CheckedTextField> textInputFields;

    protected void initAllValidProperty(CheckedTextField... fields) {
        textInputFields = Arrays.asList(fields);
        allInputValid.bind(textInputFields.stream()
                .map(tif -> tif.validProperty())
                .reduce(BINDING_IDENTITY, (bind, prop) -> bind.and(prop),
                        BooleanBinding::and));
    }

    public ReadOnlyBooleanProperty allInputValidProperty() {
        return allInputValid;
    }

    public boolean isAllInputValid() {
        return allInputValid.get();
    }

    /**
     * Closes the stage only if the inserted information is valid.
     */
    @FXML
    private void login() {
        checkStage();
        if (isAllInputValid()) {
            userConfirmed = true;
            stage.close();
        }
    }

    /**
     * Returns the currently entered login information. It returns
     * {@code Optional.empty()} only if the window was closed without pressing a
     * confirm button. That means if {@code userConfirmed} is {@code false}.
     *
     * @return The currently entered login information.
     */
    public abstract Optional<Map<LoginKey, String>> getLoginInformation();
}
