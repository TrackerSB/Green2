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

import bayern.steinbrecher.green2.elements.textfields.CheckedRegexTextField;
import javafx.beans.property.StringProperty;

/**
 * Represents a {@link CheckedRegexTextField} whose regex is unchangeable.
 *
 * @author Stefan Huber
 */
public abstract class SpecificRegexTextField extends CheckedRegexTextField {

    /**
     * Only calls {@code super(...)}. Needed making subclasses beeing able to call {@code super}.
     *
     * {@inheritDoc}
     *
     * @see CheckedRegexTextField#CheckedRegexTextField(int, java.lang.String)
     */
    protected SpecificRegexTextField(int maxColumnCount, String regex) {
        this(maxColumnCount, "", regex);
    }

    /**
     * Only calls {@code super(...)}. Needed making subclasses beeing able to call {@code super}.
     *
     * {@inheritDoc}
     *
     * @see CheckedRegexTextField#CheckedRegexTextField(int, java.lang.String, java.lang.String)
     */
    protected SpecificRegexTextField(int maxColumnCount, String text, String regex) {
        super(maxColumnCount, text, regex);
    }

    /**
     * Only calls {@code super(...)}. Needed making subclasses beeing able to call {@code super}.
     *
     * {@inheritDoc}
     *
     * @see CheckedRegexTextField#CheckedRegexTextField(int, java.lang.String, java.lang.String, boolean)
     */
    protected SpecificRegexTextField(int maxColumnCount, String text, String regex, boolean eliminateSpaces) {
        super(maxColumnCount, text, regex, eliminateSpaces);
    }

    /**
     * Unsupported operation. You're not allowed to change the regex used for IBANs.
     *
     * @return Nothing. UnsupportedOperationException thrown.
     */
    @Override
    public final StringProperty regexProperty() {
        throw new UnsupportedOperationException("You're not allowed to change the regex used for IBANs");
    }

    /**
     * Unsupported operation. You're not allowed to change the regex used for IBANs.
     *
     * @param regex Ignored.
     */
    @Override
    public final void setRegex(String regex) {
        throw new UnsupportedOperationException("You're not allowed to change the regex used for IBANs");
    }
}
