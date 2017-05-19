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

import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a {@link CheckedTextField} which also checks whether the current text is matching a given regex.
 *
 * @author Stefan Huber
 */
public class CheckedRegexTextField extends CheckedTextField {

    /**
     * Holds the string representation of the css class attribute added when the content of this text field does not
     * match the current regex.
     */
    //public static final String CSS_CLASS_REGEX_NO_MATCH = "unmatchRegex";
    private StringProperty regex = new SimpleStringProperty(this, "regex", ".*");
    private BooleanProperty regexValid = new SimpleBooleanProperty(this, "regexValid");
    private ObjectProperty<Pattern> pattern = new SimpleObjectProperty<>(this, "pattern");
    private BooleanProperty eliminateSpaces = new SimpleBooleanProperty(this, "eliminateSpaces", false);

    /**
     * Constructs a new {@link CheckedRegexTextField} without initial content, maximum column count of
     * {@link Integer#MAX_VALUE} and an all accepting regex.
     */
    public CheckedRegexTextField() {
        this("");
    }

    /**
     * Creates a new {@link CheckedRegexTextField} with no initial content, a maximum column count of
     * {@link Integer#MAX_VALUE} and validated by {@code regex}.
     *
     * @param regex The regex for validating the input.
     */
    public CheckedRegexTextField(String regex) {
        this(Integer.MAX_VALUE, regex);
    }

    /**
     * Constructs a new {@link CheckedRegexTextField} with an max input length of {@code maxColumnCount}, no initial
     * content and regex {@code regex}.
     *
     * @param maxColumnCount The initial max input length.
     * @param regex The regex for validating the input.
     */
    public CheckedRegexTextField(int maxColumnCount, String regex) {
        this(maxColumnCount, "", regex);
    }

    /**
     * Constructs a new {@link CheckedRegexTextField} with an max input length of {@code maxColumnCount}, {@code text}
     * as initial content and regex {@code regex}.
     *
     * @param maxColumnCount The initial max input length.
     * @param text The initial content.
     * @param regex The regex for validating the input.
     */
    public CheckedRegexTextField(int maxColumnCount, String text, String regex) {
        this(maxColumnCount, text, regex, false);
    }

    /**
     * Constructs a new {@link CheckedRegexTextField} with an max input length of {@code maxColumnCount}, {@code text}
     * as initial content and regex {@code regex}.
     *
     * @param maxColumnCount The initial max input length.
     * @param text The initial content.
     * @param regex The regex for validating the input.
     * @param eliminateSpaces Indicates whether spaces have to be removed before checking using the given regex.
     */
    public CheckedRegexTextField(int maxColumnCount, String text, String regex, boolean eliminateSpaces) {
        super(maxColumnCount, text);
        this.regex.set(regex);
        this.eliminateSpaces.set(eliminateSpaces);
        pattern.bind(Bindings.createObjectBinding(() -> Pattern.compile(this.regex.get()), this.regex));
        regexValid.bind(Bindings.createBooleanBinding(() -> {
            Pattern patternValue = this.pattern.get();
            String regexText = textProperty().get();
            if (this.eliminateSpaces.get()) {
                regexText = regexText.replaceAll(" ", "");
            }
            return patternValue != null && patternValue.matcher(regexText).matches();
        }, pattern, textProperty(), this.eliminateSpaces));
        addValidCondition(regexValid);
        //ElementsUtility.addCssClassIf(this, regexValid.not(), CSS_CLASS_REGEX_NO_MATCH);
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
     * Returns the property containing whether the current input is valid according to the current regex.
     *
     * @return The property containing whether the current input is valid according to the current regex.
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

    /**
     * Returns the property containing a value indicating whether spaces are eliminated before checking using the given
     * regex.
     *
     * @return The property containing a value indicating whether spaces are eliminated before checking using the given
     * regex.
     */
    public BooleanProperty eliminateSpacesProperty() {
        return eliminateSpaces;
    }

    /**
     * Checks whether currently spaces are removed before checking using the given regex.
     *
     * @return {@code true} only if spaces are removed before checking using the given regex.
     */
    public boolean isElininateSpaces() {
        return eliminateSpaces.get();
    }

    /**
     * Sets the value indicating whether spaces are removed before checking using the given regex.
     *
     * @param eliminateSpaces {@code true} only if spaces have to be removed before checking using the given regex.
     */
    public void setEliminateSpaces(boolean eliminateSpaces) {
        this.eliminateSpaces.set(eliminateSpaces);
    }
}
