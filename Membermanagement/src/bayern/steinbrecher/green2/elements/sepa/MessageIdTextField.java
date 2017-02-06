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
 * Represents a {@link CheckedRegexTextField} for entering a message id. {@link #CSS_CLASS_MESSAGE_ID} is added.
 *
 * @author Stefan Huber
 */
public final class MessageIdTextField extends SpecificRegexTextField {
    public static final String CSS_CLASS_MESSAGE_ID = "messageIdTextField";
    private BooleanProperty messageIdValid = new SimpleBooleanProperty(this, "messageIdValid");

    /**
     * Constructs an {@link MessageIdTextField} with no initial content.
     */
    public MessageIdTextField() {
        this("");
    }

    /**
     * Constructs an {@link MessageIdTextField} with given content.
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
}
