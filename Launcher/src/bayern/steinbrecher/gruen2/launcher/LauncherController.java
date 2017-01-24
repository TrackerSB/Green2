/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.launcher;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

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
    }

    /**
     * Returns the property containing the current value representing the
     * progress of download.
     *
     * @return The property containing the current value representing the
     * progress of download.
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
     * @param steps The count of steps 100% is splitted.
     */
    public void incPercentage(int steps) {
        if (percentage.get() < 1) {
            percentage.set(percentage.get() + 1.0 / steps);
        }
    }

    /**
     * Returns the property which contains a {@code String} representation of
     * the value hold by {@code percentageProperty}.
     *
     * @return The property which contains a {@code String} representation of
     * the value hold by {@code percentageProperty}.
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
