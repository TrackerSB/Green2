/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.elements.sepa;

import bayern.steinbrecher.green2.elements.textfields.CheckedRegexTextField;
import bayern.steinbrecher.green2.utility.SepaUtility;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Represents a {@link CheckedRegexTextField} specialized for IBANs. CSS class {@link #CSS_CLASS_IBAN_TEXTFIELD} is
 * added.
 *
 * @author Stefan Huber
 */
public final class IbanTextField extends SpecificRegexTextField {

    /**
     * The CSS class representing objects of this class.
     */
    public static final String CSS_CLASS_IBAN_TEXTFIELD = "iban-textfield";
    private final BooleanProperty ibanValid = new SimpleBooleanProperty(this, "ibanValid");

    /**
     * Constructs an {@link IbanTextField} with no initial content.
     */
    public IbanTextField() {
        this("");
    }

    /**
     * Constructs an {@link IbanTextField} with given content.
     *
     * @param text The initial content.
     */
    public IbanTextField(String text) {
        super(SepaUtility.MAX_CHAR_IBAN, text, SepaUtility.IBAN_REGEX);
        ibanValid.bind(Bindings.createBooleanBinding(
                () -> SepaUtility.isValidIban(textProperty().get()), textProperty()));
        addValidCondition(ibanValid);
        getStyleClass().add(CSS_CLASS_IBAN_TEXTFIELD);
    }
}
