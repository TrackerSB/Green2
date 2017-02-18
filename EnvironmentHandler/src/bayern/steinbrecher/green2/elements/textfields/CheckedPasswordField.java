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
package bayern.steinbrecher.green2.elements.textfields;

import com.sun.javafx.scene.control.behavior.PasswordFieldBehavior;
import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;

/**
 * Represents a password field which recognizes empty content and may set a css class attribute to signal it. It also
 * adds {@link CheckedPasswordField#CSS_CLASS_CHECKED_PASSWORD_FIELD}.
 *
 * @author Stefan Huber
 */
public class CheckedPasswordField extends CheckedTextField {

    /**
     * The CSS class identifying a {@link CheckedPasswordField}.
     */
    public static final String CSS_CLASS_CHECKED_PASSWORD_FIELD = "checked-password-field";

    /**
     * Constructs a new {@link CheckedPasswordField} with an max input length of {@link Integer#MAX_VALUE} and no
     * initial content.
     */
    public CheckedPasswordField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new {@link CheckedPasswordField} with a maximum column count of {@code maxColumnCount} and no
     * content.
     *
     * @param maxColumnCount The initial maximum column count.
     */
    public CheckedPasswordField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Constructs a new {@link CheckedPasswordField} with a maximum column count of {@code maxColumnCount} and initial
     * content {@code text}.
     *
     * @param maxColumnCount The initial maximum column count.
     * @param text The initial content.
     */
    public CheckedPasswordField(int maxColumnCount, String text) {
        super(maxColumnCount, text);
        setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
        setSkin(new PasswordFieldSkin(this));
        getStyleClass().add(CSS_CLASS_CHECKED_PASSWORD_FIELD);
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
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
            case TEXT:
                return null;
            default:
                return super.queryAccessibleAttribute(attribute, parameters);
        }
    }

    /**
     * The Skin used to mask the checked passwordfield in order to hide the password itself.
     */
    private class PasswordFieldSkin extends TextFieldSkin {

        /**
         * Constructs a new skin for masking {@link CheckedPasswordField}.
         *
         * @param checkedPasswordField The passwordfield to mask.
         */
        public PasswordFieldSkin(CheckedPasswordField checkedPasswordField) {
            super(checkedPasswordField);
        }

        /**
         * Constructs a new skin for masking {@link CheckedPasswordField} with the given behavior.
         *
         * @param checkedPasswordField The passwordfield to mask.
         * @param behavior The behavior for masking the passwordfield.
         */
        public PasswordFieldSkin(CheckedPasswordField checkedPasswordField, PasswordFieldBehavior behavior) {
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
