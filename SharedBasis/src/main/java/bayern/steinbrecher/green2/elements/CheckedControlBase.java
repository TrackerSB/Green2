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

import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.report.ReportableBase;
import bayern.steinbrecher.green2.utility.BindingUtility;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.util.Pair;

/**
 * Represents a base implementation of {@link CheckedControl} which can be used for delegation.
 *
 * @author Stefan Huber
 * @since 2u14
 * @param <C> The type of the control delegating to this class.
 */
//TODO May restrict to Control instead of Node (but ContributionField).
//FIXME Has this base any use since the checked property can´t be influenced? (The appropriate interfaces have use)
public class CheckedControlBase<C extends Node> implements CheckedControl {

    private final ReportableBase reportBase = new ReportableBase();
    private final C control;
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");
    private final ObservableList<ObservableBooleanValue> validConditions
            = FXCollections.observableArrayList(BindingUtility.TRUE_BINDING);

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
        validConditions.addListener((ListChangeListener.Change<? extends ObservableBooleanValue> c) -> {
            valid.bind(BindingUtility.reduceAnd(validConditions.stream()).or(checked.not()));
        });
        invalid.bind(valid.not());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservableMap<String, Pair<ReportType, BooleanExpression>> getReports() {
        return reportBase.getReports();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addReport(String message, Pair<ReportType, BooleanExpression> report) {
        reportBase.addReport(message, report);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanProperty checkedProperty() {
        //FIXME Shouldn´t this be ReadOnlyBooleanProperty?
        return checked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChecked() {
        return checkedProperty().get();
    }

    /**
     * Returns the property holding whether the current state of the control is valid. It is valid if and only if all
     * {@link #validCondition}s hold and the control {@link #isChecked()}.
     *
     * @return The property holding whether the current state of the control is valid.
     * @see #addValidCondition(javafx.beans.value.ObservableBooleanValue)
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
        return validProperty().get();
    }

    /**
     * Adds a condition to the set of conditions to be met to be a valid control, i.e. the input the control represents
     * is valid.
     *
     * @param condition The condition to add.
     */
    @Override
    public void addValidCondition(ObservableBooleanValue condition) {
        validConditions.add(condition);
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
