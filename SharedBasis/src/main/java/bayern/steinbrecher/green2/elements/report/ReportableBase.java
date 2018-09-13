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
package bayern.steinbrecher.green2.elements.report;

import bayern.steinbrecher.green2.utility.BindingUtility;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.stage.Popup;

/**
 * Contains a basic implementation of {@link Reportable} which may be used for delegation.
 *
 * @author Stefan Huber
 * @param <C> The type of the control delegating to this class.
 * @since 2u14
 */
//TODO May restrict to Control instead of Node (but ContributionField).
public class ReportableBase<C extends Node> implements Reportable {

    private final C control;
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");
    private final ObservableList<ObservableBooleanValue> validConditions = FXCollections.observableArrayList();

    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");
    private final ReadOnlyBooleanWrapper invalid = new ReadOnlyBooleanWrapper(this, "invalid") {
        @Override
        protected void invalidated() {
            control.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, get());
        }
    };
    private final ListProperty<ReportEntry> reports = new SimpleListProperty<>(
            this, "reports", FXCollections.observableArrayList(
                    report -> new Observable[]{report.reportTriggerProperty().get()}));

    /**
     * Creates a {@link ReportableBase} which can be used for delgating calls when implementing {@link Reportable}.
     *
     * @param control The control to add pseudo classes and the {@link ReportBubble} to.
     */
    public ReportableBase(C control) {
        this.control = control;
        initProperties();
    }

    /**
     * Creates the binding for determining whether the input of the control is valid. It may be overriden in order to
     * extend the binding by further restrictions of its validity.
     *
     * @return The binding for determining whether the input of the control is valid.
     */
    protected BooleanBinding createValidBinding() {
        return BindingUtility.reduceAnd(validConditions.stream());
    }

    private void initProperties() {
        validConditions.addListener((ListChangeListener.Change<? extends ObservableBooleanValue> c) -> {
            valid.bind(createValidBinding());
        });
        invalid.bind(valid.not());
        validConditions.add(BindingUtility.TRUE_BINDING); //Trigger init of property valid

        ReportBubble reportBubble = new ReportBubble(this);
        invalidProperty().and(control.focusedProperty())
                .addListener((obs, oldVal, newVal) -> {
                    Popup bubble = reportBubble.getBubble();
                    if (newVal) {
                        bubble.show(control, 0, 0);
                    } else {
                        bubble.hide();
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservableList<ReportEntry> getReports() {
        return FXCollections.unmodifiableObservableList(reports);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addReport(ReportEntry report) {
        boolean isDuplicate = reports.stream()
                .anyMatch(entry -> entry.getMessageKey().equals(report.getMessageKey()));
        if (isDuplicate) {
            throw new IllegalArgumentException(
                    "A report for \"" + report.getMessageKey() + "\" is already registered.");
        } else {
            boolean gotAdded = reports.add(report);
            if (gotAdded && report.getType() == ReportType.ERROR) { //FIXME A change of the type breaks the validation.
                gotAdded &= validConditions.add(report.getReportTrigger().not());
            }
            return gotAdded;
        }
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
        return validProperty().get();
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
