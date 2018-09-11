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
package bayern.steinbrecher.green2.elements.sepa;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.textfields.SpecificRegexTextField;
import bayern.steinbrecher.green2.utility.SepaUtility;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;

/**
 * Represents a {@link bayern.steinbrecher.green2.elements.textfields.CheckedTextField} which contains a BIC. Currently
 * it is completely the same but it adds an additional CSS style class.
 */
public class BicTextField extends SpecificRegexTextField {

    private final BooleanProperty invalidBic = new SimpleBooleanProperty(this, "invalidBic");

    /**
     * Constructs a new {@link BicTextField} with an max input length of {@link Integer#MAX_VALUE} and no initial
     * content.
     */
    public BicTextField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new {@link BicTextField} with an max input length of {@code maxColumnCount} and no initial content.
     *
     * @param maxColumnCount The initial max input length.
     */
    public BicTextField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Constructs a new {@link BicTextField} with an max input length of {@code maxColumnCount} and {@code text} as
     * initial content.
     *
     * @param maxColumnCount The initial max input length.
     * @param text The initial content.
     */
    public BicTextField(int maxColumnCount, String text) {
        super(maxColumnCount, text, SepaUtility.BIC_REGEX, false);
        getStyleClass().add("bic-textfield");
        invalidBic.bind(Bindings.createBooleanBinding(
                () -> !SepaUtility.isValidBic(textProperty().get()), textProperty()));
        getStylesheets().add(BicTextField.class.getResource("bicTextField.css").toExternalForm());
        initProperties();
    }

    private void initProperties() {
        addReport(EnvironmentHandler.getResourceValue("invalidBic"), new Pair<>(ReportType.ERROR, invalidBic));
    }
}
