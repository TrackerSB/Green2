/* 
 * Copyright (C) 2017 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.login;

import bayern.steinbrecher.green2.CheckedController;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import bayern.steinbrecher.green2.utility.BindingUtility;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.fxml.FXML;

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
     * Adds all given textfields into {@code textInputFields} and sets up the properties {@code anyInputMissing},
     * {@code anyInputToLong} and {@code valid}.
     *
     * @param fields The textfields to use for setup.
     */
    protected void initProperties(CheckedTextField... fields) {
        textInputFields = Arrays.asList(fields);
        anyInputMissing.bind(textInputFields.stream()
                .map(CheckedTextField::emptyProperty)
                .reduce(BindingUtility.FALSE_BINDING, BooleanExpression::or, BooleanBinding::or));
        anyInputToLong.bind(textInputFields.stream()
                .map(CheckedTextField::toLongProperty)
                .reduce(BindingUtility.FALSE_BINDING, BooleanExpression::or, BooleanBinding::or));
        valid.bind(textInputFields.stream()
                .map(CheckedTextField::validProperty)
                .reduce(BindingUtility.TRUE_BINDING, BooleanExpression::and, BooleanBinding::and));
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
     * Returns the currently entered login information. It returns {@link Optional#empty()} only if the window was
     * closed without pressing a confirm button. That means if {@code userConfirmed} is {@code false}.
     *
     * @return The currently entered login information.
     */
    public abstract Optional<Map<LoginKey, String>> getLoginInformation();
}
