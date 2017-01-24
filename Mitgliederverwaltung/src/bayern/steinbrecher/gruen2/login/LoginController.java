/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.login;

import bayern.steinbrecher.gruen2.CheckedController;
import bayern.steinbrecher.gruen2.elements.CheckedTextField;
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
