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

package bayern.steinbrecher.green2.elements;

import bayern.steinbrecher.green2.utility.BindingUtility;
import bayern.steinbrecher.green2.utility.ElementsUtility;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.AccessibleRole;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents text fields that detect whether their input text is longer than a
 * given maximum column count. These text fields do not stop users from entering
 * too long text. On the one hand they can tell you whether the input is too
 * long, on the other hand they set {@link #CSS_CLASS_TOO_LONG_CONTENT} when the
 * content is too long and {@link #CSS_CLASS_NO_CONTENT} when there´s no content
 * as one of their css classes if checked is set to {@code true}.
 * If any condition is false, {@link ElementsUtility#CSS_CLASS_INVALID_CONTENT} is set.
 *
 * @author Stefan Huber
 */
public class CheckedTextField extends TextField {

    /**
     * Holds the string representation of the css class attribute added when the
     * content of this text field is too long.
     */
    public static final String CSS_CLASS_TOO_LONG_CONTENT = "toLongContent";
    /**
     * Holds the string representation of the css class attribute added when
     * there´s no content in this field.
     */
    public static final String CSS_CLASS_NO_CONTENT = "emptyTextField";
    /**
     * Represents the maximum column count.
     */
    private IntegerProperty maxColumnCount = new SimpleIntegerProperty(this, "maxColumnCount");
    /**
     * Holds {@code true} only if the content has to be checked.
     */
    private final BooleanProperty checked = new SimpleBooleanProperty(this, "checked", true);
    /**
     * Holds {@code true} only if the text field is empty.
     */
    private final BooleanProperty emptyContent = new SimpleBooleanProperty(this, "emptyContent");
    /**
     * Holds {@code true} only if the text of the text field is too long.
     */
    private final BooleanProperty toLongContent = new SimpleBooleanProperty(this, "toLongContent");
    private final Property<ObservableBooleanValue> condition = new SimpleObjectProperty<>(this, "condition");
    /**
     * Holds {@code true} only if the content is valid. {@code true} if one of
     * the following is true (as implemented by this class):
     * <ol>
     * <li>This field is not checked</li>
     * <li>It is not empty and the content is not too long</li>
     * </ol>
     */
    private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid");
    private final List<ObservableBooleanValue> validConditions = new ArrayList<>();
    private final BooleanProperty validCondition = new SimpleBooleanProperty(this, "validCondition", true);
    private final BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid");

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of
     * {@link Integer#MAX_VALUE} and no initial content.
     */
    public CheckedTextField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of
     * {@code maxColumnCount} and no initial content.
     *
     * @param maxColumnCount The initial max input length.
     */
    public CheckedTextField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of
     * {@code maxColumnCount} and {@code text} as initial content.
     *
     * @param maxColumnCount The initial max input length.
     * @param text           The initial content.
     */
    public CheckedTextField(int maxColumnCount, String text) {
        super(text);
        setAccessibleRole(AccessibleRole.TEXT_FIELD);
        initProperties();

        setMaxColumnCount(maxColumnCount);
    }

    /**
     * Sets up all properties and bindings.
     */
    private void initProperties() {
        emptyContent.bind(textProperty().isEmpty());
        toLongContent.bind(textProperty().length().greaterThan(maxColumnCount));
        valid.bind((toLongContent.or(emptyContent).and(checked).not()).and(validCondition));
        invalid.bind(valid.not());
        ElementsUtility.addCssClassIf(this, invalid, ElementsUtility.CSS_CLASS_INVALID_CONTENT);
        ElementsUtility.addCssClassIf(this, emptyContent, CSS_CLASS_NO_CONTENT);
        ElementsUtility.addCssClassIf(this, toLongContent, CSS_CLASS_TOO_LONG_CONTENT);
    }

    /**
     * Returns the property representing the maximum column count.
     *
     * @return The property representing the maximum column count.
     */
    public IntegerProperty maxColumnCountProperty() {
        return maxColumnCount;
    }

    /**
     * Returns the current set maximum column count.
     *
     * @return The current set maximum column count.
     */
    public int getMaxColumnCount() {
        return maxColumnCount.get();
    }

    /**
     * Sets a new maximum column count.
     *
     * @param maxColumnCount The new maximum column count.
     */
    public void setMaxColumnCount(int maxColumnCount) {
        if (maxColumnCount < 1) {
            throw new IllegalArgumentException("maxColumnCount must be at least 1");
        }
        this.maxColumnCount.set(maxColumnCount);
    }

    /**
     * Represents whether this text field is checked or not.
     *
     * @return The property representing whether this text field is checked or
     * not.
     */
    public BooleanProperty checkedProperty() {
        return checked;
    }

    /**
     * Checks whether the text field is checked.
     *
     * @return {@code true} only if the text field is checked.
     */
    public boolean isChecked() {
        return checked.get();
    }

    /**
     * Sets whether to check the content of this field or not.
     *
     * @param checked {@code true} only if the content of this field has to be
     *                checked.
     */
    public void setChecked(boolean checked) {
        this.checked.set(checked);
    }

    /**
     * Returns the property representing whether there´s no text inserted.
     *
     * @return The property representing whether there´s no text inserted.
     */
    public ReadOnlyBooleanProperty emptyProperty() {
        return emptyContent;
    }

    /**
     * Checks whether the text field is empty.
     *
     * @return {@code true} only if the text field is empty.
     */
    public boolean isEmpty() {
        return emptyContent.get();
    }

    /**
     * Returns the property representing whether the current content of the text
     * field is too long.
     *
     * @return The property representing whether the current content of the text
     * field is too long.
     */
    public ReadOnlyBooleanProperty toLongProperty() {
        return toLongContent;
    }

    /**
     * Checks whether the currently inserted text is too long. The input may be
     * too long even if it is valid. E.g. when the text field is not checked.
     *
     * @return {@code true} only if the current content is too long.
     */
    public boolean isToLong() {
        return toLongContent.get();
    }

    /**
     * Returns the property representing the validity of the inserted content.
     *
     * @return The property representing the validity of the inserted content.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether the current content is valid.
     *
     * @return {@code true } only if the current content is valid.
     * @see #valid
     */
    public boolean isValid() {
        return valid.get();
    }

    private void updateValidConditions() {
        validCondition.unbind();
        validCondition.bind(BindingUtility.reduceAnd(validConditions.stream()));
    }

    /**
     * Adds the given condition and binds it to {@code validProperty}.
     *
     * @param condition The condition to add.
     */
    protected void addValidCondition(ObservableBooleanValue condition) {
        validConditions.add(condition);
        updateValidConditions();
    }

    /**
     * Removes the given element. The element will be compared according to {@code equals(...)}.
     *
     * @param condition The condition to remove.
     * @return {@code true} only if any element was removed.
     */
    protected boolean removeValidCondition(ObservableBooleanValue condition) {
        boolean removedElement = validConditions.remove(condition);
        updateValidConditions();
        return removedElement;
    }
}
