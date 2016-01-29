package bayern.steinbrecher.gruen2.elements;

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
    public static final String CSS_CLASS_NO_CONTENT = "noContent";
    private int maxColumnCount;

    /**
     * Constructes a new {@code CheckedTextField} with an max input length of
     * {@code maxColumnCount} and no initial content.
     *
     * @param maxColumnCount The initial max input length.
     */
    public CheckedTextField(int maxColumnCount) {
        this(maxColumnCount, null);
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
        textProperty().addListener(obs -> checkLength());
    }

    /**
     * Returns the current set maximum column count.
     *
     * @return The current set maximum column count.
     */
    public int getMaxColumnCount() {
        return maxColumnCount;
    }

    /**
     * Sets a new maximum column count.
     *
     * @param maxColumnCount The new maximum column count.
     */
    public void setMaxColumnCount(int maxColumnCount) {
        if (maxColumnCount < 0) {
            throw new IllegalArgumentException(
                    "maxColumnCount must not be negative");
        }
        this.maxColumnCount = maxColumnCount;
        checkLength();
    }

    /**
     * Checks whether the current content is too long.
     *
     * @return {@code true} only if the current content is too long.
     */
    public boolean isTooLong() {
        return this.getLength() > maxColumnCount;
    }

    private void checkLength() {
        if (getText().length() > maxColumnCount) {
            if (!getStyleClass().contains(
                    CheckedTextField.CSS_CLASS_TOO_LONG_CONTENT)) {
                getStyleClass().add(
                        CheckedTextField.CSS_CLASS_TOO_LONG_CONTENT);
            }
        } else {
            getStyleClass().remove(CheckedTextField.CSS_CLASS_TOO_LONG_CONTENT);
        }

        if (getText().length() == 0) {
            if (!getStyleClass().contains(
                    CheckedTextField.CSS_CLASS_NO_CONTENT)) {
                getStyleClass().add(CheckedTextField.CSS_CLASS_NO_CONTENT);
            }
        } else {
            getStyleClass().remove(CheckedTextField.CSS_CLASS_NO_CONTENT);
        }
    }
}
