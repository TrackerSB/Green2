/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.elements;

import com.sun.javafx.scene.control.behavior.PasswordFieldBehavior;
import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;

/**
 * Represents a password field which recognizes empty content and may set a css
 * class attribute to signal it.
 *
 * @author Stefan Huber
 */
public class CheckedPasswordField extends CheckedTextField {

    /**
     * Constructes a new {@code CheckedPasswordField} with an max input length
     * of {@code Integer.MAX_VALUE} and no initial content.
     */
    public CheckedPasswordField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructes a new {@code CheckedPasswordField} with a maximum column
     * count of {@code maxColumnCount} and no content.
     *
     * @param maxColumnCount The initial maximum column count.
     */
    public CheckedPasswordField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Constructes a new {@code CheckedPasswordField} with a maximum column
     * count of {@code maxColumnCount} and initial content {@code text}.
     *
     * @param maxColumnCount The initial maximum column count.
     * @param text The initial content.
     */
    public CheckedPasswordField(int maxColumnCount, String text) {
        super(maxColumnCount, text);
        setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
        setSkin(new PasswordFieldSkin(this));
    }

    /**
     * Does nothing for PasswordField.
     */
    @Override
    public void cut() {
        // No-op
    }

    /**
     * Does nothing for PasswordField.
     */
    @Override
    public void copy() {
        // No-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object queryAccessibleAttribute(
            AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
            case TEXT:
                return null;
            default:
                return super.queryAccessibleAttribute(attribute, parameters);
        }
    }

    /**
     * The Skin used to mask the checked passwordfield in order to hide the
     * password itself.
     */
    private class PasswordFieldSkin extends TextFieldSkin {

        /**
         * Constructes a new skin for masking {@code checkedPasswordField}.
         *
         * @param checkedPasswordField The passwordfield to mask.
         */
        public PasswordFieldSkin(CheckedPasswordField checkedPasswordField) {
            super(checkedPasswordField);
        }

        /**
         * Constructes a new skin for masking {@code checkedPasswordField} with
         * the given behavior.
         *
         * @param checkedPasswordField The passwordfield to mask.
         * @param behavior The behavior for masking the passwordfield.
         */
        public PasswordFieldSkin(CheckedPasswordField checkedPasswordField,
                PasswordFieldBehavior behavior) {
            super(checkedPasswordField, behavior);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String maskText(String password) {
            int length = password.length();
            StringBuilder passwordBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                passwordBuilder.append(TextFieldSkin.BULLET);
            }
            return passwordBuilder.toString();
        }
    }
}
