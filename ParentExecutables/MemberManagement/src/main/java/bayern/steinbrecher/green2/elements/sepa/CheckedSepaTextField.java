/*
 * Copyright (C) 2018 Steinbrecher
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

import bayern.steinbrecher.green2.elements.report.ReportEntry;
import bayern.steinbrecher.green2.elements.report.ReportType;
import bayern.steinbrecher.green2.elements.textfields.CheckedTextField;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;

/**
 * Represents a {@link CheckedTextField} which may be only valid if no by SEPA unsupported charaters are used. This
 * implementation refers to the list found at
 * https://www.hettwer-beratung.de/sepa-spezialwissen/sepa-technische-anforderungen/sepa-utf-8-zeichensatz/.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class CheckedSepaTextField extends CheckedTextField {

    /**
     * This list contains all regions of codepoints containing symbols not accepted by SEPA. First and last codepoint
     * are both included.
     */
    private static final List<Pair<Character, Character>> INVALID_CHAR_REGIONS = List.of(
            new Pair<>('\u007F', '\u009F'),
            new Pair<>('\u00A4', '\u00A6'),
            new Pair<>('\u00A8', '\u00AF'),
            new Pair<>('\u00B1', '\u00BE'),
            new Pair<>('\u00D7', '\u00D7'),
            new Pair<>('\u00F7', '\u00F7'),
            new Pair<>('\u0108', '\u0109'),
            new Pair<>('\u0114', '\u0115'),
            new Pair<>('\u0120', '\u0121'),
            new Pair<>('\u0124', '\u0129'),
            new Pair<>('\u012C', '\u012D'),
            new Pair<>('\u0134', '\u0135'),
            new Pair<>('\u0138', '\u0138'),
            new Pair<>('\u013F', '\u0140'),
            new Pair<>('\u0149', '\u014F'),
            new Pair<>('\u0156', '\u0157'),
            new Pair<>('\u015C', '\u015D'),
            new Pair<>('\u0166', '\u0169'),
            new Pair<>('\u016C', '\u016D'),
            new Pair<>('\u0174', '\u0177'),
            new Pair<>('\u017F', '\u0217'),
            new Pair<>('\u0296', '\u0385'),
            new Pair<>('\u0387', '\u0387'),
            new Pair<>('\u038B', '\u038B'),
            new Pair<>('\u038D', '\u038D'),
            new Pair<>('\u03CE', '\u040F'),
            new Pair<>('\u042B', '\u042B'),
            new Pair<>('\u042D', '\u042D'),
            new Pair<>('\u044B', '\u044B'),
            new Pair<>('\u044D', '\u044D'),
            new Pair<>('\u0450', '\u20AB'),
            new Pair<>('\u20AD', '\uFFFF')
    //NOTE Disallow characters until \u10FFFF (not representable by char)
    );
    private final BooleanProperty unsupportedSymbols = new SimpleBooleanProperty(this, "unsupportedSymbols");

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of {@link Integer#MAX_VALUE} and no initial
     * content.
     */
    public CheckedSepaTextField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of {@code maxColumnCount} and no initial
     * content.
     *
     * @param maxColumnCount The initial max input length.
     */
    public CheckedSepaTextField(int maxColumnCount) {
        this(maxColumnCount, "");
    }

    /**
     * Constructs a new {@link CheckedTextField} with an max input length of {@code maxColumnCount} and {@code text} as
     * initial content.
     *
     * @param maxColumnCount The initial max input length.
     * @param text The initial content.
     */
    public CheckedSepaTextField(int maxColumnCount, String text) {
        super(maxColumnCount, text);
        unsupportedSymbols.bind(Bindings.createBooleanBinding(() -> {
            return textProperty().get().chars()
                    .parallel()
                    .anyMatch(codepoint -> {
                        return INVALID_CHAR_REGIONS.stream()
                                .parallel()
                                .anyMatch(range -> range.getKey() <= codepoint && codepoint <= range.getValue());
                    });
        }, textProperty()));
        initProperties();

        getStyleClass().add("checked-sepa-textfield");
    }

    private void initProperties() {
        addReport(new ReportEntry("unsupportedSymbols", ReportType.ERROR, unsupportedSymbols));
    }
}
