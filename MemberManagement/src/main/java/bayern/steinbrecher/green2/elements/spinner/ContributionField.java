/*
 * Copyright (C) 2017 Steinbrecher
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
import bayern.steinbrecher.green2.utility.ElementsUtility;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Represents a contribution spinner. It is a {@code CheckedDoubleSpinner} which has a further field for associating a
 * color to the contribution.
 *
 * @author Stefan Huber
 */
public class ContributionField extends HBox implements Initializable {

    @FXML
    private CheckedDoubleSpinner contributionSpinner;
    private ObjectProperty<CheckedDoubleSpinner> contributionSpinnerProperty
            = new SimpleObjectProperty<>(this, "contributionSpinner");
    @FXML
    private ColorPicker colorPicker;
    private ObjectProperty<ColorPicker> colorPickerProperty = new SimpleObjectProperty<>(this, "colorPicker");
    private BooleanProperty valid = new SimpleBooleanProperty(this, "valid");
    private BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid");

    /**
     * Represents a combination of a spinner for entering a contribution and an associated color. The minimum value is
     * 0, the maximum value is 5000, the initial value is 10 and the amountToStepBy is 1.
     *
     * @see CheckedDoubleSpinner#CheckedDoubleSpinner(double, double, double, double)
     */
    public ContributionField() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("ContributionField.fxml"), EnvironmentHandler.RESOURCE_BUNDLE);
        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(ContributionField.class.getName())
                    .log(Level.SEVERE, "Could not load ContributionField", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        valid.bind(contributionSpinner.validProperty());
        invalid.bind(valid.not());

        contributionSpinnerProperty.set(contributionSpinner);
        colorPickerProperty.set(colorPicker);
        colorPicker.setValue(Color.FORESTGREEN); //TODO Default color is set manually and to fixed value

        ElementsUtility.addCssClassIf(contributionSpinner, invalid, ElementsUtility.CSS_CLASS_INVALID_CONTENT);
        ElementsUtility.addCssClassIf(colorPicker, invalid, ElementsUtility.CSS_CLASS_INVALID_CONTENT);
    }

    /**
     * Returns the property holding the currently inserted contribution.
     *
     * @return The property holding the currently inserted contribution.
     */
    public ReadOnlyObjectProperty<Double> contributionProperty() {
        return contributionSpinner.valueProperty();
    }

    /**
     * Returns the currently inserted contribution.
     *
     * @return The currently inserted contribution.
     */
    public double getContribution() {
        return contributionProperty().getValue();
    }

    /**
     * Returns the property holding the currently associated color.
     *
     * @return The property holding the currently associated color.
     */
    public ReadOnlyObjectProperty<Color> colorProperty() {
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
     * Returns the property holding the currently used spinner for entering the contribution.
     *
     * @return The property holding the currently used spinner for entering the contribution.
     */
    public ReadOnlyObjectProperty<CheckedDoubleSpinner> contributionSpinnerProperty() {
        return contributionSpinnerProperty;
    }

    /**
     * Returns the currently used spinner for entering the contribution.
     *
     * @return The currently used spinner for entering the contribution.
     */
    public CheckedDoubleSpinner getContributionSpinner() {
        return contributionSpinnerProperty().get();
    }

    /**
     * Returns the property holding the currently used {@code ColorPicker} for choosing an associated color.
     *
     * @return The property holding the currently used {@code ColorPicker} for choosing an associated color.
     */
    public ReadOnlyObjectProperty<ColorPicker> colorPickerProperty() {
        return colorPickerProperty;
    }

    /**
     * Returns the currently used {@code ColorPicker} for choosing an associated color.
     *
     * @return The currently used {@code ColorPicker} for choosing an associated color.
     */
    public ColorPicker getColorPicker() {
        return colorPickerProperty().get();
    }

    /**
     * Returns the property containing whether the current input is valid.
     *
     * @return The property containing whether the current input is valid.
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Checks whether the current input is valid.
     *
     * @return {@code true} only if the current input is valid.
     */
    public boolean isValid() {
        return valid.get();
    }
}
