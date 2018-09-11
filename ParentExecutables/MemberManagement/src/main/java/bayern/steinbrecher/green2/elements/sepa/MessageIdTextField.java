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

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.textfields.SpecificRegexTextField;
import bayern.steinbrecher.green2.utility.SepaUtility;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;

/**
 * Represents a {@link bayern.steinbrecher.green2.elements.textfields.CheckedRegexTextField} for entering a message id.
 *
 * @author Stefan Huber
 */
public final class MessageIdTextField extends SpecificRegexTextField {

    private final BooleanProperty invalidMessageId = new SimpleBooleanProperty(this, "invalidMessageId");

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
        invalidMessageId.bind(Bindings.createBooleanBinding(
                () -> !SepaUtility.isValidMessageId(textProperty().get()), textProperty()));
        addReport(EnvironmentHandler.getResourceValue("invalidMessageId"),
                new Pair<>(ReportType.ERROR, invalidMessageId));
        getStyleClass().add("messageIdTextField");
    }
}
