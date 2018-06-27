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
package bayern.steinbrecher.green2.connection.scheme;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Represents patterns of columns which the application can actively handle.
 *
 * @author Stefan Huber
 * @param <T> The datatype of the values stored in the column.
 * @param <U> The type of object to set the content of this column to.
 * @since 2u14
 */
public abstract class ColumnPattern<T, U> {

    private final Pattern columnNamePattern;
    private final ColumnParser<T> parser;

    /**
     * Creates a column pattern describing possibly multiple columns.
     *
     * @param columnNamePattern The pattern column names have to match.
     * @param parser The parser for converting values from and to their SQL representation.
     */
    public ColumnPattern(String columnNamePattern, ColumnParser<T> parser) {
        if (!columnNamePattern.startsWith("^") || !columnNamePattern.endsWith("$")) {
            Logger.getLogger(ColumnPattern.class.getName())
                    .log(Level.WARNING,
                            "The pattern \"{0}\" is not encapsulated in \"^\" and \"$\".", columnNamePattern);
        }
        this.columnNamePattern = Pattern.compile(columnNamePattern);
        this.parser = parser;
    }

    /**
     * Returns the pattern for which these column attributes apply.
     *
     * @return The pattern for which these column attributes apply.
     */
    public Pattern getColumnNamePattern() {
        return columnNamePattern;
    }

    /**
     * Checks whether the given column name matches this pattern.
     *
     * @param columnName The column name to check.
     * @return {@code true} only if the given column name matches this pattern.
     * @see #getColumnNamePattern()
     */
    public boolean matches(String columnName) {
        return getColumnNamePattern().matcher(columnName)
                .matches();
    }

    /**
     * Returns the parser used for converting from and to an appropriate SQL representation.
     *
     * @return The parser used for converting from and to an appropriate SQL representation.
     */
    public ColumnParser<T> getParser() {
        return parser;
    }

    /**
     * Checks whether this pattern reflects the same column names as the given object. NOTE It is only checked whether
     * their regex are identical not whether they express the same column names.
     *
     * @param obj The object to compare this column pattern with.
     * @return {@code true} only if this pattern reflects the same column names as the given object.
     */
    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj instanceof ColumnPattern) {
            isEqual = ((ColumnPattern) obj).getColumnNamePattern()
                    .pattern()
                    .equals(columnNamePattern.pattern());
        }
        return isEqual;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getColumnNamePattern()
                .pattern();
    }
}
