/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.launcher;

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
        percentage.addListener((obs, oldVal, newVal) -> {
            percentageString.set(FORMAT.format(newVal.doubleValue() * 100)
                    + "% ");
        });
        percentage.addListener(obs -> {
            percentageString.set(FORMAT.format(percentage.get() * 100)
                    + "% ");
        });
    }

    public ReadOnlyDoubleProperty percentageProperty() {
        return percentage;
    }

    public double getPercentage() {
        return percentage.get();
    }

    public void incPercentage(int steps) {
        if (percentage.get() < 1) {
            percentage.set(percentage.get() + 1.0 / steps);
        }
    }

    public ReadOnlyStringProperty percentageStringProperty() {
        return percentageString;
    }

    public String getPercentageString() {
        return percentageString.get();
    }
}
