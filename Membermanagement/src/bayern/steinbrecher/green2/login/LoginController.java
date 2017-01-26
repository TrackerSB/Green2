/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package bayern.steinbrecher.green2.login;

import bayern.steinbrecher.green2.CheckedController;
import bayern.steinbrecher.green2.elements.CheckedTextField;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a controller for a login.
 *
 * @author Stefan Huber
 */
public abstract class LoginController extends CheckedController {

    /**
     * An useful list of all textfields which the login contains.
     */
    protected List<CheckedTextField> textInputFields;

    /**
     * Adds all given textfields into {@code textInputFields} and sets up the
     * properties {@code anyInputMissing}, {@code anyInputToLong} and
     * {@code valid}.
     *
     * @param fields The textfields to use for setup.
     */
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
