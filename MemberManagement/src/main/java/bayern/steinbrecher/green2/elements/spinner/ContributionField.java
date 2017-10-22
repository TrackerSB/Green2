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
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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

    public ContributionField(@NamedArg("min") double min,
            @NamedArg("max") double max,
            @NamedArg("initialValue") double initialValue,
            @NamedArg("amountToStepBy") double amountToStepBy) {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contributionSpinnerProperty.set(contributionSpinner);
        colorPickerProperty.set(colorPicker);
        colorPicker.setValue(Color.FORESTGREEN); //TODO Default color is set manually and to fixed value

        ElementsUtility.addCssClassIf(contributionSpinner, contributionSpinner.validProperty().not(),
                ElementsUtility.CSS_CLASS_INVALID_CONTENT);
    }

    public ReadOnlyObjectProperty<Double> contributionProperty() {
        return contributionSpinner.valueProperty();
    }

    public double getContribution() {
        return contributionProperty().getValue();
    }

    public ReadOnlyObjectProperty<Color> colorProperty() {
        return colorPicker.valueProperty();
    }

    public Color getColor() {
        return colorProperty().get();
    }

    public ReadOnlyObjectProperty<CheckedDoubleSpinner> contributionSpinnerProperty() {
        return contributionSpinnerProperty;
    }

    public CheckedDoubleSpinner getContributionSpinner() {
        return contributionSpinnerProperty().get();
    }

    public ReadOnlyObjectProperty<ColorPicker> colorPickerProperty() {
        return colorPickerProperty;
    }

    public ColorPicker getColorPicker() {
        return colorPickerProperty().get();
    }
}
