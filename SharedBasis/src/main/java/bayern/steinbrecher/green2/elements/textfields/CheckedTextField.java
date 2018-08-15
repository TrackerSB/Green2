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

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.CheckedControl;
import bayern.steinbrecher.green2.elements.CheckedControlBase;
import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.report.Reportable;
import bayern.steinbrecher.green2.utility.BindingUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleRole;
import javafx.scene.control.TextField;
import javafx.util.Pair;

/**
 * Represents text fields that detects whether their input text is longer than a given maximum column count. These text
 * fields do not stop users from entering too long text.
 *
 * @author Stefan Huber
 */
public class CheckedTextField extends TextField implements CheckedControl, Reportable {

    private final CheckedControlBase<CheckedTextField> ccBase = new CheckedControlBase<>(this);
    private final IntegerProperty maxColumnCount = new SimpleIntegerProperty(this, "maxColumnCount");

    private static final PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");
    private final BooleanProperty emptyContent = new SimpleBooleanProperty(this, "emptyContent") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, get());
        }
    };

    private static final PseudoClass TO_LONG_PSEUDO_CLASS = PseudoClass.getPseudoClass("toLong");
    private final BooleanProperty toLongContent = new SimpleBooleanProperty(this, "toLongContent") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(TO_LONG_PSEUDO_CLASS, get());
        }

    };
    private final List<ObservableBooleanValue> validConditions = new ArrayList<>();
    private final BooleanProperty validCondition = new SimpleBooleanProperty(this, "validCondition", true);
    private final Map<String, Pair<ReportType, BooleanExpression>> reports
            = Map.of(EnvironmentHandler.getResourceValue("inputMissing"),
                    new Pair<>(ReportType.ERROR, emptyProperty().and(checkedProperty())),
                    EnvironmentHandler.getResourceValue("inputToLong"),
                    new Pair<>(ReportType.ERROR, toLongProperty().and(checkedProperty())),
                    EnvironmentHandler.getResourceValue("inputInvalid"),
                    new Pair<>(ReportType.ERROR, ccBase.invalidProperty().and(checkedProperty())));

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of {@link Integer#MAX_VALUE} and no initial
     * content.
     */
    public CheckedTextField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of {@code maxColumnCount} and no initial
     * content.
     *
     * @param maxColumnCount The initial max input length.
     */
    public CheckedTextField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of {@code maxColumnCount} and {@code text} as
     * initial content.
     *
     * @param maxColumnCount The initial max input length.
     * @param text The initial content.
     */
    public CheckedTextField(int maxColumnCount, String text) {
        super(text);
        setAccessibleRole(AccessibleRole.TEXT_FIELD);
        initProperties();

        setMaxColumnCount(maxColumnCount);
        getStyleClass().add("checked-textfield");
        getStylesheets().add(CharsetTextField.class.getResource("checkedTextField.css").toExternalForm());
    }

    /**
     * Sets up all properties and bindings.
     */
    private void initProperties() {
        emptyContent.bind(textProperty().isEmpty());
        toLongContent.bind(textProperty().length().greaterThan(maxColumnCount));
        ccBase.bindValidProperty(
                toLongContent.or(emptyContent).not().and(validCondition).or(ccBase.checkedProperty().not()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Pair<ReportType, BooleanExpression>> getReports() {
        return reports;
    }

    /**
     * Returns the property representing the maximum column count.
     *
     * @return The property representing the maximum column count.
     */
    public final IntegerProperty maxColumnCountProperty() {
        return maxColumnCount;
    }

    /**
     * Returns the current set maximum column count.
     *
     * @return The current set maximum column count.
     */
    public final int getMaxColumnCount() {
        return maxColumnCountProperty().get();
    }

    /**
     * Sets a new maximum column count.
     *
     * @param maxColumnCount The new maximum column count.
     */
    public final void setMaxColumnCount(int maxColumnCount) {
        if (maxColumnCount < 1) { //NOPMD - Force at least one character.
            throw new IllegalArgumentException("maxColumnCount must be at least 1");
        }
        maxColumnCountProperty().set(maxColumnCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanProperty checkedProperty() {
        return ccBase.checkedProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChecked() {
        return ccBase.isChecked();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChecked(boolean checked) {
        ccBase.setChecked(checked);
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
     * Returns the property representing whether the current content of the text field is too long.
     *
     * @return The property representing whether the current content of the text field is too long.
     */
    public ReadOnlyBooleanProperty toLongProperty() {
        return toLongContent;
    }

    /**
     * Checks whether the currently inserted text is too long. The input may be too long even if it is valid. E.g. when
     * the text field is not checked.
     *
     * @return {@code true} only if the current content is too long.
     */
    public boolean isToLong() {
        return toLongContent.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return ccBase.validProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return ccBase.isValid();
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
    protected final void addValidCondition(ObservableBooleanValue condition) {
        validConditions.add(condition);
        updateValidConditions();
    }

    /**
     * Removes the given element. The element will be compared according to {@code equals(...)}.
     *
     * @param condition The condition to remove.
     * @return {@code true} only if any element was removed.
     */
    protected final boolean removeValidCondition(ObservableBooleanValue condition) {
        boolean removedElement = validConditions.remove(condition);
        updateValidConditions();
        return removedElement;
    }
}
