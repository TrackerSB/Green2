/*
 * Copyright (C) 2018 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.elements.textfields;

import bayern.steinbrecher.green2.utility.ElementsUtility;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a {@link CheckedRegexTextField} whose regex is unchangeable.
 *
 * @author Stefan Huber
 */
public class SpecificRegexTextField extends CheckedTextField {

    /**
     * Holds the string representation of the css class attribute added when the content of this text field does not
     * match the current regex.
     */
    public static final String CSS_CLASS_REGEX_NO_MATCH = "unmatch-regex";
    /**
     * The property holding the regex used for validation.
     */
    private final StringProperty regex = new SimpleStringProperty(this, "regex", ".*");
    private final BooleanProperty regexValid = new SimpleBooleanProperty(this, "regexValid");
    private final ObjectProperty<Pattern> pattern = new SimpleObjectProperty<>(this, "pattern");
    private final BooleanProperty eliminateSpaces = new SimpleBooleanProperty(this, "eliminateSpaces", false);

    /**
     * Constructs a new {@link CheckedRegexTextField} without initial content, maximum column count of
     * {@link Integer#MAX_VALUE} and an all accepting regex.
     */
    public SpecificRegexTextField() {
        this("");
    }

    /**
     * Creates a new {@link CheckedRegexTextField} with no initial content, a maximum column count of
     * {@link Integer#MAX_VALUE} and validated by {@code regex}.
     *
     * @param regex The regex for validating the input.
     */
    public SpecificRegexTextField(String regex) {
        this(Integer.MAX_VALUE, regex);
    }

    /**
     * Constructs a new {@link CheckedRegexTextField} with an max input length of {@code maxColumnCount}, no initial
     * content and regex {@code regex}.
     *
     * @param maxColumnCount The initial max input length.
     * @param regex The regex for validating the input.
     */
    public SpecificRegexTextField(int maxColumnCount, String regex) {
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
    public SpecificRegexTextField(int maxColumnCount, String text, String regex) {
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
    public SpecificRegexTextField(int maxColumnCount, String text, String regex, boolean eliminateSpaces) {
        super(maxColumnCount, text);
        this.regex.set(regex);
        this.eliminateSpaces.set(eliminateSpaces);
        initProperties();

        addValidCondition(regexValid);
    }

    private void initProperties() {
        pattern.bind(Bindings.createObjectBinding(() -> Pattern.compile(this.regex.get()), this.regex));
        regexValid.bind(Bindings.createBooleanBinding(() -> {
            Pattern patternValue = this.pattern.get();
            String regexText = textProperty().get();
            if (this.eliminateSpaces.get()) {
                regexText = regexText.replaceAll(" ", "");
            }
            return patternValue != null && patternValue.matcher(regexText).matches();
        }, pattern, textProperty(), this.eliminateSpaces));
        ElementsUtility.addCssClassIf(this, regexValid.not(), CSS_CLASS_REGEX_NO_MATCH);
    }

    /**
     * Returns the text of the {@link CheckedTextField} only if the currently inserted text is valid according to the
     * given regex.
     *
     * @return The text of the {@link CheckedTextField} only if the currently inserted text is valid according to the
     * given regex. Otherwise it returns an empty {@link String}.
     */
    public String getRegexValidText() {
        return isRegexValid() ? getText() : "";
    }

    /**
     * Returns the property representing the regex used for validation.
     *
     * @return The property representing the regex used for validation.
     */
    public ReadOnlyStringProperty regexProperty() {
        return regex;
    }

    /**
     * Returns a modifiable version of {@link #regexProperty()}. NOTE The only use of this method is provide subclasses
     * a way to implement write access to the regex.
     *
     * @return The modifiable property holding the regex used for validation.
     */
    protected StringProperty regexPropertyModifiable() {
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
    public boolean isEliminateSpaces() {
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
