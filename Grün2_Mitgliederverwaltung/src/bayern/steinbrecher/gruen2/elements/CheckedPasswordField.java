package bayern.steinbrecher.gruen2.elements;

import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;

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
}
