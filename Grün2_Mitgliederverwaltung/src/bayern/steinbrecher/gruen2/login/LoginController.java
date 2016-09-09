package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.CheckedController;
import bayern.steinbrecher.gruen2.data.LoginKey;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;

/**
 * Represents a controller for a login.
 *
 * @author Stefan Huber
 */
public abstract class LoginController extends CheckedController {

    protected List<CheckedTextField> textInputFields;

    protected void initProperties(CheckedTextField... fields) {
        textInputFields = Arrays.asList(fields);
        anyInputMissing.bind(textInputFields.stream()
                .map(tif -> tif.emptyProperty())
                .reduce(TRUE_BINDING, (bind, prop) -> bind.or(prop),
                        BooleanBinding::or));
        anyInputToLong.bind(textInputFields.stream()
                .map(tif -> tif.toLongProperty())
                .reduce(TRUE_BINDING, (bind, prop) -> bind.or(prop),
                        BooleanBinding::or));
        valid.bind(textInputFields.stream()
                .map(tif -> tif.validProperty())
                .reduce(TRUE_BINDING, (bind, prop) -> bind.and(prop),
                        BooleanBinding::and));
    }

    /**
     * Closes the stage only if the inserted information is valid.
     */
    @FXML
    private void login() {
        checkStage();
        if (isValid()) {
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
