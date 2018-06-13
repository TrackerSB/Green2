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
package bayern.steinbrecher.green2.elements.textfields;

import java.nio.charset.Charset;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Represents a {@link CheckedTextField} for entering a {@link Charset}. It also checks whether the current system
 * supports the entered {@link Charset}.
 */
public final class CharsetTextField extends CheckedTextField {

    private final BooleanProperty validCharset = new SimpleBooleanProperty(this, "invalid");

    /**
     * Creates a new {@link CharsetTextField} with a maximum column count of {@link Integer#MAX_VALUE} and no initial
     * text.
     */
    public CharsetTextField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a new {@link CharsetTextField} with a maximum column count of {@code maxColumnCount} and no initial text.
     *
     * @param maxColumnCount The maximum column count.
     */
    public CharsetTextField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Creates a new {@link CharsetTextField} with a maximum column count of {@code maxColumnCount} and the initial text
     * {@code text}.
     *
     * @param maxColumnCount The maximum column count.
     * @param text The initial text.
     */
    public CharsetTextField(int maxColumnCount, String text) {
        super(maxColumnCount, text);
        validCharset.bind(Bindings.createBooleanBinding(
                () -> !textProperty().get().isEmpty() && Charset.isSupported(textProperty().get()), textProperty()));
        addValidCondition(validCharset);
    }
}
