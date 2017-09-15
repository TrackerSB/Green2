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
package bayern.steinbrecher.green2.elements.sepa;

import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;

/**
 * Represents a {@link CheckedTextField} which contains a BIC. Currently it is completely the same but it adds an
 * additional CSS style class.
 */
public class BicTextField extends CheckedTextField {

    /**
     * The CSS class representing this class.
     */
    public static final String CSS_CLASS_BIC_TEXTFIELD = "bic-textfield";

    /**
     * Constructs a new {@link BicTextField} with an max input length of {@link Integer#MAX_VALUE} and no initial
     * content.
     */
    public BicTextField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new {@link BicTextField} with an max input length of {@code maxColumnCount} and no initial content.
     *
     * @param maxColumnCount The initial max input length.
     */
    public BicTextField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Constructs a new {@link BicTextField} with an max input length of {@code maxColumnCount} and {@code text} as
     * initial content.
     *
     * @param maxColumnCount The initial max input length.
     * @param text The initial content.
     */
    public BicTextField(int maxColumnCount, String text) {
        super(maxColumnCount, text);
        getStyleClass().add(CSS_CLASS_BIC_TEXTFIELD);
    }
}
