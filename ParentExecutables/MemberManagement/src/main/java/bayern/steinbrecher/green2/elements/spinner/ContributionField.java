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
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * Represents a contribution spinner. It is a {@link CheckedDoubleSpinner} which has a further field for associating a
 * color to the contribution.
 *
 * @author Stefan Huber
 */
public class ContributionField extends HBox implements Initializable, CheckedControl, Reportable {

    private static final Logger LOGGER = Logger.getLogger(ContributionField.class.getName());
    private final CheckedControlBase<ContributionField> ccBase = new CheckedControlBase<>(this);
    @FXML
    private CheckedDoubleSpinner contributionSpinner;
    @FXML
    private ColorPicker colorPicker;

    /**
     * Represents a combination of a spinner for entering a contribution and an associated color. The minimum value is
     * 0, the maximum value is 5000, the initial value is 10 and the amountToStepBy is 1. The default color associated
     * is {@link Color#TRANSPARENT}.
     *
     * @see CheckedDoubleSpinner#CheckedDoubleSpinner(double, double, double, double)
     */
    public ContributionField() {
        super();
        loadFXML();
    }

    private void loadFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                ContributionField.class.getResource("ContributionField.fxml"), EnvironmentHandler.RESOURCE_BUNDLE);
        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not load ContributionField", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ccBase.bindValidProperty(contributionSpinner.validProperty().or(ccBase.checkedProperty().not()));

        colorPicker.setValue(Color.TRANSPARENT);

        ElementsUtility.addCssClassIf(
                contributionSpinner, ccBase.invalidProperty(), ElementsUtility.CSS_CLASS_INVALID_CONTENT);
        ElementsUtility.addCssClassIf(colorPicker, ccBase.invalidProperty(), ElementsUtility.CSS_CLASS_INVALID_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Pair<ReportType, BooleanExpression>> getReports() {
        return contributionSpinner.getReports();
    }

    /**
     * Returns the property holding the currently inserted contribution.
     *
     * @return The property holding the currently inserted contribution.
     */
    public ObjectProperty<Double> contributionProperty() {
        return contributionSpinner.getValueFactory().valueProperty();
    }

    /**
     * Returns the currently inserted contribution.
     *
     * @return The currently inserted contribution. Returns {@link Optional#empty()} if and only if no value is
     * inserted.
     */
    public Optional<Double> getContribution() {
        return Optional.ofNullable(contributionProperty().getValue());
    }

    /**
     * Sets a new contribution to show.
     *
     * @param contribution The new contribution to set.
     */
    public void setContribution(double contribution) {
        contributionProperty().set(contribution);
    }

    /**
     * Returns the property holding the currently associated color.
     *
     * @return The property holding the currently associated color.
     */
    public ObjectProperty<Color> colorProperty() {
        return colorPicker.valueProperty();
    }

    /**
     * Returns the currently associated color.
     *
     * @return The currently associated color.
     */
    public Color getColor() {
        return colorProperty().get();
    }

    /**
     * Sets the color of the {@link ColorPicker}
     *
     * @param color The color to set.
     */
    public void setColor(Color color) {
        colorProperty().set(color);
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
        return ccBase.isValid();
    }
}
