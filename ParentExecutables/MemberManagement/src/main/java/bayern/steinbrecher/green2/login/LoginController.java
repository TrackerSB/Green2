/*
 * Copyright (C) 2018 Stefan Huber
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

import bayern.steinbrecher.green2.WizardableController;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import bayern.steinbrecher.green2.utility.BindingUtility;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXML;

/**
 * Represents a controller for a login.
 *
 * @author Stefan Huber
 */
public abstract class LoginController extends WizardableController<Optional<Map<LoginKey, String>>> {

    /**
     * Adds all given textfields into {@code textInputFields} and sets up the properties {@code anyInputMissing},
     * {@code anyInputToLong} and {@code valid}.
     *
     * @param fields The textfields to use for setup.
     */
    protected void initProperties(CheckedTextField... fields) {
        List<CheckedTextField> textInputFields = Arrays.asList(fields);

        bindValidProperty(BindingUtility.reduceAnd(textInputFields.stream().map(CheckedTextField::validProperty)));
    }

    /**
     * Closes the stage only if the inserted information is valid.
     */
    @FXML
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "It is called by an appropriate fxml file")
    @SuppressWarnings("unused")
    private void login() {
        if (isValid()) {
            getStage().close();
        }
    }
}
