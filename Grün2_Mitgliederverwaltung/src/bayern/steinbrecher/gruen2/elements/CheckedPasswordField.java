package bayern.steinbrecher.gruen2.elements;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.TextField;

/**
 * Represents a password field which recognizes empty content and may set a css
 * class attribute to signal it.
 *
 * @author Stefan Huber
 */
public class CheckedPasswordField extends CheckedTextField {

    public CheckedPasswordField() {
        super();
        //FIXME Show bullets instead of clear text
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

    private class PasswordFieldSkin extends TextFieldSkin {

        public PasswordFieldSkin(TextField textField) {
            super(textField);
        }

        public PasswordFieldSkin(TextField textField,
                TextFieldBehavior behavior) {
            super(textField, behavior);
        }

        @Override
        protected String maskText(String txt) {
            int length = txt.length();
            StringBuilder passwordBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                passwordBuilder.append(TextFieldSkin.BULLET);
            }
            return passwordBuilder.toString();
        }
    }
}
