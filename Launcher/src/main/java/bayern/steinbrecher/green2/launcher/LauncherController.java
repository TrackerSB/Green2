/* 
 * Copyright (C) 2017 Stefan Huber
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
package bayern.steinbrecher.green2.launcher;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;

/**
 * Controller of download dialog.
 *
 * @author Stefan Huber
 */
public class LauncherController implements Initializable {

    private static final DecimalFormat FORMAT = new DecimalFormat("#0.0");
    private final DoubleProperty percentage = new SimpleDoubleProperty();
    private final StringProperty percentageString
            = new SimpleStringProperty("0%");

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        percentage.addListener(
                (obs, oldVal, newVal) -> percentageString.set(FORMAT.format(newVal.doubleValue() * 100) + "% "));
    }

    /**
     * Returns the property containing the current value representing the progress of download.
     *
     * @return The property containing the current value representing the progress of download.
     */
    public ReadOnlyDoubleProperty percentageProperty() {
        return percentage;
    }

    /**
     * Returns the value hold by {@code percentageProperty}.
     *
     * @return The value hold by {@code percentageProperty}.
     * @see #percentageProperty()
     */
    public double getPercentage() {
        return percentage.get();
    }

    /**
     * Increases the value of {@code percentageProperty()} by 1.0/{@code steps}.
     *
     * @param steps The count of steps 100% is split.
     */
    public void incPercentage(int steps) {
        if (percentage.get() < 1) {
            percentage.set(percentage.get() + 1.0 / steps);
        }
    }

    /**
     * Returns the property which contains a {@link String} representation of the value hold by
     * {@code percentageProperty}.
     *
     * @return The property which contains a {@link String} representation of the value hold by
     * {@code percentageProperty}.
     * @see #percentageProperty()
     */
    public ReadOnlyStringProperty percentageStringProperty() {
        return percentageString;
    }

    /**
     * Returns the value hold by {@code percentageStringProperty()}.
     *
     * @return The value hold by {@code percentageStringProperty()}.
     * @see #percentageStringProperty()
     */
    public String getPercentageString() {
        return percentageString.get();
    }
}
