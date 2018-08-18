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
package bayern.steinbrecher.green2.elements.spinner;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.CheckedControl;
import bayern.steinbrecher.green2.elements.CheckedControlBase;
import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.report.Reportable;
import bayern.steinbrecher.green2.utility.ElementsUtility;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.Pair;

/**
 * Extends the class {@link Spinner} with a valid property and sets {@link ElementsUtility#CSS_CLASS_INVALID_CONTENT} if
 * the content of the spinner is not valid.
 *
 * @author Stefan Huber
 * @param <T> The type of the values to spin.
 */
public class CheckedSpinner<T> extends Spinner<T> implements CheckedControl, Reportable {

    private final CheckedControlBase<CheckedSpinner<T>> ccBase = new CheckedControlBase<>(this);
    private final Map<String, Pair<ReportType, BooleanExpression>> reports = new HashMap<>(Map.of(
            EnvironmentHandler.getResourceValue("inputMissing"),
            new Pair<>(ReportType.ERROR, valueProperty().isNull().and(checkedProperty())),
            EnvironmentHandler.getResourceValue("inputInvalid"),
            new Pair<>(ReportType.ERROR, ccBase.invalidProperty().and(checkedProperty()))
    ));

    /**
     * Constructs a new {@code CheckedSpinner}.
     *
     * @param factory The factory generating values.
     * @param parser The function to parse the content of the {@link Spinner}.
     */
    public CheckedSpinner(SpinnerValueFactory<T> factory, Function<String, Optional<T>> parser) {
        super(factory);
        initProperties(factory, parser);
        getStyleClass().add("checked-spinner");
    }

    private void initProperties(SpinnerValueFactory<T> factory, Function<String, Optional<T>> parser) {
        ccBase.addValidCondition(Bindings.createBooleanBinding(() -> {
            Optional<T> parsed = parser.apply(getEditor().textProperty().get());
            parsed.ifPresent(p -> factory.setValue(p));
            return parsed.isPresent();
        }, getEditor().textProperty()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Pair<ReportType, BooleanExpression>> getReports() {
        return reports;
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
        return validProperty().get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addValidCondition(ObservableBooleanValue condition) {
        ccBase.addValidCondition(condition);
    }
}
