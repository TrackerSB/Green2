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
package bayern.steinbrecher.green2.elements.textfields;

import java.nio.charset.Charset;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;

/**
 * Represents a {@link TextField} for entering a {@link Charset}. It also checks whether the current system supports the
 * entered {@link Charset}.
 */
public final class CharsetTextField extends CheckedTextField {

    private BooleanProperty validCharset = new SimpleBooleanProperty(this, "invalid");

    public CharsetTextField() {
        this(Integer.MAX_VALUE);
    }

    public CharsetTextField(int maxColumnCount) {
        this(Integer.MAX_VALUE, "");
    }

    public CharsetTextField(int maxColumnCount, String text) {
        super(maxColumnCount, text);
        validCharset.bind(Bindings.createBooleanBinding(
                () -> !textProperty().get().isEmpty() && Charset.isSupported(textProperty().get()), textProperty()));
        addValidCondition(validCharset);
    }
}
