package bayern.steinbrecher.gruen2.elements;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.scene.AccessibleRole;
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
    public static final String CSS_CLASS_NO_CONTENT = "emptyTextField";
    /**
     * Represents the maximum column count.
     */
    private IntegerProperty maxColumnCountProperty
            = new IntegerPropertyBase() {
        @Override
        public CheckedTextField getBean() {
            return CheckedTextField.this;
        }

        @Override
        public String getName() {
            return "maxColumnCount";
        }
    };
    /**
     * Holds {@code true} only if the content has to be checked.
     */
    private final BooleanProperty checkedProperty
            = new BooleanPropertyBase(true) {
        @Override
        public CheckedTextField getBean() {
            return CheckedTextField.this;
        }

        @Override
        public String getName() {
            return "checked";
        }
    };
    /**
     * Holds {@code true} only if the text field is empty.
     */
    private final BooleanProperty emptyProperty
            = new BooleanPropertyBase() {
        @Override
        public CheckedTextField getBean() {
            return CheckedTextField.this;
        }

        @Override
        public String getName() {
            return "emptyContent";
        }
    };
    /**
     * Holds {@code true} only if the text of the text field is too long.
     */
    private final BooleanProperty tooLongProperty
            = new BooleanPropertyBase() {
        @Override
        public CheckedTextField getBean() {
            return CheckedTextField.this;
        }

        @Override
        public String getName() {
            return "tooLongContent";
        }
    };
    /**
     * Holds {@code true} only if the content is valid. {@code true} if one of
     * the following is true:
     * <ol>
     * <li>This field is not checked</li>
     * <li>It is not empty and the content is not too long</li>
     * </ol>
     */
    private final BooleanProperty validProperty = new BooleanPropertyBase() {
        @Override
        public CheckedTextField getBean() {
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
        setAccessibleRole(AccessibleRole.TEXT_FIELD);
        initProperties();

        //Validate properties
        setMaxColumnCount(maxColumnCount);
        setText(text + " ");
        setText(text);
    }

    /**
     * Sets up all properties and bindings.
     */
    private void initProperties() {
        emptyProperty.bind(textProperty().isEmpty());
        textProperty().addListener((obs, oldVal, newVal) -> {
            tooLongProperty.set(newVal != null
                    && newVal.length() > maxColumnCountProperty.get());
        });
        maxColumnCountProperty.addListener((obs, oldVal, newVal) -> {
            tooLongProperty.setValue(
                    textProperty().get().length() > newVal.intValue());
        });
        validProperty.bind(tooLongProperty.or(emptyProperty)
                .and(checkedProperty).not());
        validProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                getStyleClass().removeAll(
                        CSS_CLASS_NO_CONTENT, CSS_CLASS_TOO_LONG_CONTENT);
            } else {
                if (emptyProperty.get()) {
                    getStyleClass().add(CSS_CLASS_NO_CONTENT);
                }
                if (tooLongProperty.get()) {
                    getStyleClass().add(CSS_CLASS_TOO_LONG_CONTENT);
                }
            }
        });
    }

    /**
     * Returns the property representing the maximum column count.
     *
     * @return The property representing the maximum column count.
     */
    public IntegerProperty maxColumnCount() {
        return maxColumnCountProperty;
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

    /**
     * Represents whether this text field is checked or not.
     *
     * @return The property representing whether this text field is checked or
     * not.
     */
    public BooleanProperty checkedProperty() {
        return checkedProperty;
    }

    /**
     * Checks whether the text field is checked.
     *
     * @return {@code true} only if the text field is checked.
     */
    public boolean isChecked() {
        return checkedProperty.get();
    }

    /**
     * Sets whether to check the content of this field or not.
     *
     * @param checked {@code true} only if the content of this field has to be
     * checked.
     */
    public void setChecked(boolean checked) {
        checkedProperty.set(checked);
    }

    /**
     * Returns the property representing whether there´s no text inserted.
     *
     * @return The property representing whether there´s no text inserted.
     */
    public BooleanProperty emptyProperty() {
        return emptyProperty;
    }

    /**
     * Checks whether the text field is empty.
     *
     * @return {@code true} only if the text field is empty.
     */
    public boolean isEmpty() {
        return emptyProperty.get();
    }

    /**
     * Returns the property representing whether the current content of the text
     * field is too long.
     *
     * @return The property representing whether the current content of the text
     * field is too long.
     */
    public BooleanProperty tooLongProperty() {
        return tooLongProperty;
    }

    /**
     * Checks whether the currently inserted text is too long. The input may be
     * too long even if it is valid. E.g. when the text field is not checked.
     *
     * @return {@code true} only if the current content is too long.
     */
    public boolean isTooLong() {
        return tooLongProperty.get();
    }

    /**
     * Returns the binding representing the validity of the inserted content.
     *
     * @return The binding representing the validity of the inserted content.
     */
    public BooleanProperty validProperty() {
        return validProperty;
    }

    /**
     * Checks whether the current content is valid.
     *
     * @return {@code true } only if the current content is valid.
     * @see #validProperty
     */
    public boolean isValid() {
        return validProperty.get();
    }
}
