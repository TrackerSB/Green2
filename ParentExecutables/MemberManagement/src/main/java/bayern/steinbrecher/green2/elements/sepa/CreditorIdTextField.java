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
package bayern.steinbrecher.green2.elements.sepa;

import bayern.steinbrecher.green2.utility.SepaUtility;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Represents a {@link javafx.scene.control.TextField} for inserting a creditor id. Css class
 * {@link #CSS_CLASS_CREDITORID_TEXTFIELD} is added.
 *
 * @author Stefan Huber
 */
public final class CreditorIdTextField extends CheckedSepaTextField {

    /**
     * The CSS class representing objects of this class.
     */
    public static final String CSS_CLASS_CREDITORID_TEXTFIELD = "creditorid-textfield";
    private final BooleanProperty creditorIdValid = new SimpleBooleanProperty(this, "creditorIdValid");

    /**
     * Creates a new empty {@link CreditorIdTextField}.
     */
    public CreditorIdTextField() {
        this("");
    }

    /**
     * Creates a new {@link CreditorIdTextField} with initial text {@code text}.
     *
     * @param text The initial content of this field.
     */
    public CreditorIdTextField(String text) {
        super(Integer.MAX_VALUE, text);
        creditorIdValid.bind(Bindings.createBooleanBinding(
                () -> SepaUtility.isValidCreditorId(textProperty().get()), textProperty()));
        addValidCondition(creditorIdValid);
        getStyleClass().add(CSS_CLASS_CREDITORID_TEXTFIELD);
        getStylesheets().add(CreditorIdTextField.class.getResource("creditorIdTextField.css").toExternalForm());
    }
}
