package bayern.steinbrecher.gruen2.elements;

import javafx.scene.control.TextField;

/**
 * Diese Klasse stellt ein Textfield mit beschränkter (maximaler) Inputlänge
 * dar. D.h. nicht, dass man nur Text in dieser Länge eingeben kann. Es heißt
 * lediglich, dass dieses Textfeld erkennt, wenn der Input zu lang ist.
 *
 * @author Stefan Huber
 */
public class CheckedTextField extends TextField {

    private int maxColumnCount;

    /**
     * Constructes a new object with an max input length of
     * {@code Integer.MAX_VALUE}.
     */
    public CheckedTextField() {
        super();
        this.maxColumnCount = Integer.MAX_VALUE;
    }

    /**
     * Constructes a new object with an max input length of
     * {@code Integer.MAX_VALUE} and initial text content.
     *
     * @param text A string for text content.
     */
    public CheckedTextField(String text) {
        super(text);
        this.maxColumnCount = Integer.MAX_VALUE;
    }

    /**
     * Constructes a new object with an max input length of
     * {@code maxColumnCount} and no initial text content.
     *
     * @param maxColumnCount The initial max input length.
     */
    public CheckedTextField(int maxColumnCount) {
        this(maxColumnCount, null);
    }

    public CheckedTextField(int maxColumnCount, String text) {
        super(text);
        if (maxColumnCount < 0) {
            throw new IllegalArgumentException(
                    "maxColumnCount must not be negative");
        }
        this.maxColumnCount = maxColumnCount;
    }

    public int getMaxColumnCount() {
        return maxColumnCount;
    }

    public void setMaxColumnCount(int maxColumnCount) {
        if (maxColumnCount < 0) {
            throw new IllegalArgumentException(
                    "maxColumnCount must not be negative");
        }
        this.maxColumnCount = maxColumnCount;
    }

    public boolean isToLong() {
        return this.getLength() > maxColumnCount;
    }
}
