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
package bayern.steinbrecher.green2.elements;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * This class represents an implementation of {@link CheckedControl} which can be used for delegation.
 *
 * @author Stefan Huber
 * @since 2u14
 * @param <C> The type of the control delegating to this class.
 */
//TODO May restrict to Control instead of Node (but ContributionField).
public class CheckedControlBase<C extends Node> implements CheckedControl {

    private final C control;
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");

    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");
    private final ReadOnlyBooleanWrapper invalid = new ReadOnlyBooleanWrapper(this, "invalid") {
        @Override
        protected void invalidated() {
            control.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, get());
        }
    };

    private static final PseudoClass CHECKED_PSEUDO_CLASS = PseudoClass.getPseudoClass("checked");
    private final BooleanProperty checked = new SimpleBooleanProperty(this, "checked", true) {
        @Override
        protected void invalidated() {
            control.pseudoClassStateChanged(CHECKED_PSEUDO_CLASS, get());
        }
    };

    /**
     * Creates a basic object which can be used for delegation by classes implementing {@link CheckedControl}.
     *
     * @param control The control delegating to this class.
     */
    public CheckedControlBase(C control) {
        this.control = control;
        control.getStyleClass().add("checked-control-base");
        invalid.bind(valid.not());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanProperty checkedProperty() {
        return checked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChecked() {
        return checkedProperty().getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChecked(boolean checked) {
        checkedProperty().setValue(checked);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return validProperty().getValue();
    }

    /**
     * Binds the valid property to an value to observe. This method may be needed by any class using this class for
     * delegation.
     *
     * @param bind The valid to bind the valid property to.
     * @see #validProperty()
     */
    public void bindValidProperty(ObservableValue<? extends Boolean> bind) {
        valid.bind(bind);
    }

    /**
     * Returns the property holding the inverse value of {@link #validProperty()}. This method may be used for
     * convenience.
     *
     * @return The property holding the inverse value of {@link #validProperty()}.
     */
    public ReadOnlyBooleanProperty invalidProperty() {
        return invalid.getReadOnlyProperty();
    }

    /**
     * Returns the opposite value of {@link #isValid()}. This method may be used for convenience.
     *
     * @return The opposite value of {@link #isValid()}.
     */
    public boolean isInvalid() {
        return invalidProperty().getValue();
    }
}
