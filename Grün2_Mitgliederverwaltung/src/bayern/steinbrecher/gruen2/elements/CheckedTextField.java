package bayern.steinbrecher.gruen2.elements;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.scene.control.TextField;

/**
 * Represents text fields that detect whether their input text is longer than a
 * given maximum column count. These text fields do not stop users from entering
 * too long text. On the one hand they can tell you whether the input is too
 * long, on the other hand they set {@code CSS_CLASS_TOO_LONG_CONTENT} when the
 * content is too long and {@code CSS_CLASS_NO_CONTENT} when there´s no content
 * as one of their css classes.
 *
 * @author Stefan Huber
 */
public class CheckedTextField extends TextField {

    /**
     * Holds the string representation of the css class attribute added when the
     * content of this text field is too long.
     */
    public static final String CSS_CLASS_TOO_LONG_CONTENT = "tooLongContent";
    /**
     * Holds the string representation of the css class attribute added when
     * there´s no content in this field.
     */
    public static final String CSS_CLASS_NO_CONTENT = "empty";
    private IntegerProperty maxColumnCountProperty
            = new IntegerPropertyBase() {
        @Override
        public Object getBean() {
            return CheckedTextField.this;
        }

        @Override
        public String getName() {
            return "maxColumnCount";
        }
    };
    /**
     * {@code true} only if the content has to be checked.
     */
    private final BooleanProperty checkedProperty
            = new BooleanPropertyBase(true) {
        @Override
        public Object getBean() {
            return CheckedTextField.this;
        }

        @Override
        public String getName() {
            return "checked";
        }
    };
    private final BooleanProperty validProperty
            = new BooleanPropertyBase(false) {
        @Override
        public Object getBean() {
            return CheckedTextField.this;
        }

        @Override
        public String getName() {
            return "valid";
        }
    };

    /**
     * Creates a new {@code CheckedTextField} with no initial content and a
     * maximum column count of {@code Integer.MAX_VALUE}.
     */
    public CheckedTextField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructes a new {@code CheckedTextField} with an max input length of
     * {@code maxColumnCount} and no initial content.
     *
     * @param maxColumnCount The initial max input length.
     */
    public CheckedTextField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Constructes a new {@code CheckedTextField} with an max input length of
     * {@code maxColumnCount} and {@code text} as initial content.
     *
     * @param maxColumnCount The initial max input length.
     * @param text The initial content.
     */
    public CheckedTextField(int maxColumnCount, String text) {
        super(text);
        setMaxColumnCount(maxColumnCount);
        textProperty().addListener((obs, oldVal, newVal) -> checkValid());
        checkedProperty.addListener((obs, oldVal, newVal) -> checkValid());
        checkValid();
    }

    /**
     * Returns the current set maximum column count.
     *
     * @return The current set maximum column count.
     */
    public int getMaxColumnCount() {
        return maxColumnCountProperty.get();
    }

    /**
     * Sets a new maximum column count.
     *
     * @param maxColumnCount The new maximum column count.
     */
    public void setMaxColumnCount(int maxColumnCount) {
        if (maxColumnCount < 1) {
            throw new IllegalArgumentException(
                    "maxColumnCount must be at least 1");
        }
        maxColumnCountProperty.set(maxColumnCount);
    }

    public IntegerProperty maxColumnCount() {
        return maxColumnCountProperty;
    }

    /**
     * Checks whether the current content is valid.
     *
     * @return {@code true} if one of the following is true:
     * <ol>
     * <li>This field is not checked</li>
     * <li>It is not empty and the content is not too long</li>
     * </ol>
     */
    public boolean isValid() {
        return validProperty.get();
    }

    public BooleanProperty validProperty() {
        return validProperty;
    }

    private void checkValid() {
        int textLength = getText() == null ? 0 : getText().length();

        //Update validProperty
        validProperty.set(!checkedProperty.get() || (textLength > 0
                && textLength <= maxColumnCountProperty.get()));

        //Update css classes
        if (textLength > maxColumnCountProperty.get()
                && checkedProperty.get()) {
            if (!getStyleClass().contains(
                    CheckedTextField.CSS_CLASS_TOO_LONG_CONTENT)) {
                getStyleClass().add(
                        CheckedTextField.CSS_CLASS_TOO_LONG_CONTENT);
            }
        } else {
            getStyleClass().remove(CheckedTextField.CSS_CLASS_TOO_LONG_CONTENT);
        }

        if (textLength == 0 && checkedProperty.get()) {
            if (!getStyleClass().contains(
                    CheckedTextField.CSS_CLASS_NO_CONTENT)) {
                getStyleClass().add(CheckedTextField.CSS_CLASS_NO_CONTENT);
            }
        } else {
            getStyleClass().remove(CheckedTextField.CSS_CLASS_NO_CONTENT);
        }
    }

    /**
     * Sets whether to check the content of this field or not.
     *
     * @param checked {@code true} only if the content of this field has to be
     * checked.
     */
    public void setChecked(boolean checked) {
        if (checkedProperty.get() != checked) {
            checkedProperty.set(checked);
        }
    }
}
