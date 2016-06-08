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
     * Constructes a new {@code CheckedPasswordField} with no maximum column
     * count and no content.
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
