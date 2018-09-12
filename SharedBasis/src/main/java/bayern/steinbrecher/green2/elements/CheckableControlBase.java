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

import bayern.steinbrecher.green2.elements.report.ReportableBase;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * This class represents an implementation of {@link CheckableControl} which can be used for delegation.
 *
 * @author Stefan Huber
 * @since 2u14
 * @param <C> The type of the control delegating to this class.
 */
//TODO May restrict to Control instead of Node (but ContributionField).
public class CheckableControlBase<C extends Node> extends ReportableBase<C> implements CheckableControl {

    private static final Logger LOGGER = Logger.getLogger(CheckableControlBase.class.getName());
    private static final PseudoClass CHECKED_PSEUDO_CLASS = PseudoClass.getPseudoClass("checked");
    private final C control;
    private final BooleanProperty checked = new SimpleBooleanProperty(this, "checked", true) {
        @Override
        protected void invalidated() {
            control.pseudoClassStateChanged(CHECKED_PSEUDO_CLASS, get());
        }
    };

    /**
     * Creates a {@link CheckableControlBase} which can be used for delgating calls when implementing
     * {@link CheckableControl}.
     *
     * @param control The control to add pseudo classes to.
     */
    public CheckableControlBase(C control) {
        super(control);
        this.control = control;
        control.getStyleClass().add("checked-control-base");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BooleanBinding createValidBinding() {
        BooleanBinding validBinding;
        if (checkedProperty() == null) {
            LOGGER.log(Level.WARNING, "CheckedProperty() returns null and could not be connected, yet.");
            validBinding = super.createValidBinding();
        } else {
            validBinding = super.createValidBinding()
                    .or(checkedProperty().not());
        }
        return validBinding;
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
        return checkedProperty().get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChecked(boolean checked) {
        checkedProperty().set(checked);
    }
}
