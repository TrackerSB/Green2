/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.elements;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.regex.Pattern;

/**
 * Represents a {@code CheckedTextField} which also checks whether the current
 * text is matching a given regex.
 *
 * @author Stefan Huber
 */
public class CheckedRegexTextField extends CheckedTextField {

    /**
     * Holds the string representation of the css class attribute added when the
     * content of this text field does not match the current regex.
     */
    public static final String CSS_CLASS_REGEX_NO_MATCH = "unmatchRegex";
    private StringProperty regex
            = new SimpleStringProperty(this, "regex");
    private BooleanProperty regexValid
            = new SimpleBooleanProperty(this, "regexValid");
    private ObjectProperty<Pattern> pattern
            = new SimpleObjectProperty<>(this, "pattern");

    /**
     * Constructes a new {@code CheckedRegexTextField} without initial content,
     * maximum column count of {@code Integer.MAX_VALUE} and a all accepting
     * regex.
     */
    public CheckedRegexTextField() {
        this("");
    }

    /**
     * Creates a new {@code CheckedRegexTextField} with no initial content, a
     * maximum column count of {@code Integer.MAX_VALUE} and validated by
     * {@code regex}.
     *
     * @param regex The regex for validating the input.
     */
    public CheckedRegexTextField(String regex) {
        this(Integer.MAX_VALUE, regex);
    }

    /**
     * Constructes a new {@code CheckedRegexTextField} with an max input length
     * of {@code maxColumnCount}, no initial content and regex {@code regex}.
     *
     * @param maxColumnCount The initial max input length.
     * @param regex The regex for validating the input.
     */
    public CheckedRegexTextField(int maxColumnCount, String regex) {
        this(maxColumnCount, "", regex);
    }

    /**
     * Constructes a new {@code CheckedRegexTextField} with an max input length
     * of {@code maxColumnCount}, {@code text} as initial content and regex
     * {@code regex}.
     *
     * @param maxColumnCount The initial max input length.
     * @param text The initial content.
     * @param regex The regex for validating the input.
     */
    public CheckedRegexTextField(int maxColumnCount, String text,
            String regex) {
        super(maxColumnCount, text);
        this.regex.addListener((obs, oldVal, newVal) -> {
            pattern.set(Pattern.compile(newVal));
        });
        this.regex.set(regex);
        textProperty().addListener((obs, oldVal, newVal) -> {
            regexValid.set(pattern.get().matcher(newVal).matches());
        });
        pattern.addListener((obs, oldVal, newVal) -> {
            regexValid.set(newVal.matcher(this.regex.get()).matches());
        });
        valid.bind(regexValid);
        valid.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                getStyleClass().remove(CSS_CLASS_REGEX_NO_MATCH);
            } else if (!regexValid.get()) {
                getStyleClass().add(CSS_CLASS_REGEX_NO_MATCH);
            }
        });
    }

    /**
     * Returns the property representing the regex used for validation.
     *
     * @return The property representing the regex used for validation.
     */
    public StringProperty regexProperty() {
        return regex;
    }

    /**
     * Returns the regex used for validation.
     *
     * @return The regex used for validation.
     */
    public String getRegex() {
        return regex.get();
    }

    /**
     * Sets the regex used for validation.
     *
     * @param regex The new regex to be used for validation.
     */
    public void setRegex(String regex) {
        this.regex.set(regex);
    }

    /**
     * Returns the property containing whether the current input is valid
     * according to the current regex.
     *
     * @return The property containing whether the current input is valid
     * according to the current regex.
     * @see CheckedRegexTextField#getRegex()
     */
    public ReadOnlyBooleanProperty regexValidProperty() {
        return regexValid;
    }

    /**
     * Checks whether the current input is valid according to the current regex.
     *
     * @return {@code true} only if the current regex matches the current input.
     */
    public boolean isRegexValid() {
        return regexValid.get();
    }
}
