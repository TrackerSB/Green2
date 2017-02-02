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

import bayern.steinbrecher.green2.elements.CheckedRegexTextField;
import bayern.steinbrecher.green2.utility.SepaUtility;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a {@code TextField} for entering a message id. {@code CSS_CLASS_MESSAGE_ID} is added.
 *
 * @author Stefan Huber
 */
public class MessageIdTextField extends CheckedRegexTextField {
    public static final String CSS_CLASS_MESSAGE_ID = "messageIdTextField";
    private BooleanProperty messageIdValid = new SimpleBooleanProperty(this, "messageIdValid");

    /**
     * Constructs an {@code IbanTextField} with no initial content.
     */
    public MessageIdTextField() {
        this("");
    }

    /**
     * Constructs an {@code IbanTextField} with given content.
     *
     * @param text The initial content.
     */
    public MessageIdTextField(String text) {
        super(SepaUtility.MAX_CHAR_MESSAGE_ID, text, SepaUtility.MESSAGE_ID_REGEX);
        messageIdValid.bind(Bindings.createBooleanBinding(
                () -> SepaUtility.isValidMessageId(textProperty().get()), textProperty()));
        addValidCondition(messageIdValid);
        getStyleClass().add(CSS_CLASS_MESSAGE_ID);
    }

    /**
     * Unsupported operation. You're not allowed to change the regex used for IBANs.
     *
     * @return Nothing. UnsupportedOperationException thrown.
     */
    @Override
    public StringProperty regexProperty() {
        throw new UnsupportedOperationException("You're not allowed to change the regex used for IBANs");
    }

    /**
     * Unsupported operation. You're not allowed to change the regex used for IBANs.
     *
     * @param regex Ignored.
     */
    @Override
    public void setRegex(String regex) {
        throw new UnsupportedOperationException("You're not allowed to change the regex used for IBANs");
    }
}
