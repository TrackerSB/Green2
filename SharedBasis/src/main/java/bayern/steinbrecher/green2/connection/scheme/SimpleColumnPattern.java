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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Represents a {@link ColumnPattern} representing a <strong>specific</strong> column name instead of an actual pattern
 * for column names.
 *
 * @author Stefan Huber
 * @since 2u14
 * @param <T> The type of the contents this column holds.
 * @param <U> The type of object to set the content of this column to.
 */
public class SimpleColumnPattern<T, U> extends ColumnPattern<T, U> {

    private final String realColumnName;
    private final Optional<Optional<T>> defaultValue;
    private final Set<Keywords> keywords;
    private final BiFunction<U, T, U> setter;

    /**
     * Creates a new simple column pattern, i.e. a pattern which specifies a specific column name. This constructor may
     * be used if {@link U} is an immutable type.
     *
     * @param realColumnName The exact name of the column to match.
     * @param keywords The keywords to specify when creating a column matching this pattern.
     * @param parser The parser to convert values from and to a SQL representation.
     * @param setter The function used to set a parsed value to a given object. The setter should only return a new
     * object of type {@link U} if the handed in one is immutable.
     */
    public SimpleColumnPattern(String realColumnName, Set<Keywords> keywords, ColumnParser<T> parser,
            BiFunction<U, T, U> setter) {
        this(realColumnName, keywords, parser, setter, Optional.empty());
    }

    /**
     * Creates a new simple column pattern, i.e. a pattern which specifies a specific column name. This constructor may
     * be used if {@link U} is an immutable type.
     *
     * @param realColumnName The exact name of the column to match.
     * @param keywords The keywords to specify when creating a column matching this pattern.
     * @param parser The parser to convert values from and to a SQL representation.
     * @param setter The function used to set a parsed value to a given object. The setter should only return a new
     * object of type {@link U} if the handed in one is immutable.
     * @param defaultValue The default value of this column. {@link Optional#empty()} represents explicitely no default
     * value. An {@link Optional} of an {@link Optional#empty()} represents {@code null} as default value. Otherwise the
     * value of the inner {@link Optional} represents the default value.
     * @see ColumnPattern#ColumnPattern(java.lang.String, bayern.steinbrecher.green2.connection.scheme.ColumnParser)
     */
    public SimpleColumnPattern(String realColumnName, Set<Keywords> keywords, ColumnParser<T> parser,
            BiFunction<U, T, U> setter, Optional<Optional<T>> defaultValue) {
        super("^\\Q" + realColumnName + "\\E$", parser);
        Set<Keywords> keywordsCopy = new HashSet<>(keywords);
        //Make sure DEFAULT keyword is added when a default value is specified.
        if (defaultValue != null || !keywordsCopy.contains(Keywords.NOT_NULL)) {
            keywordsCopy.add(Keywords.DEFAULT);
        }
        this.realColumnName = realColumnName;
        this.defaultValue = defaultValue;
        this.keywords = keywordsCopy;
        this.setter = setter;
    }

    /**
     * Parses the given value and sets it to the object of type {@link U}.
     *
     * @param toSet The object to set the parsed value to.
     * @param value The value to parse and to set.
     * @return The resulting object of type {@link U}.
     */
    public U combine(U toSet, String value) {
        T parsedValue = getParser().parse(value)
                .orElseThrow(() -> new IllegalArgumentException(getRealColumnName() + " can not parse " + value));
        return setter.apply(toSet, parsedValue);
    }

    /**
     * Checks whether a default value is set for this column
     *
     * @return {@code true} only if a default value is associated with this column.
     */
    public boolean hasDefaultValue() {
        return getDefaultValue().isPresent();
    }

    /**
     * Returns the {@link String} representation of the default value suitable for SQL.
     *
     * @return The {@link String} representation of the default value suitable for SQL. If the default value is
     * {@code null} the {@link String} "NULL" (without quotes) is returned.
     * @see #getDefaultValue()
     */
    public String getDefaultValueSql() {
        return getDefaultValue()
                .map(value -> getParser().toString(value.orElse(null)))
                .orElseThrow();
    }

    /**
     * Returns the default value to set when creating a table containing this column.
     *
     * @return The default value to set when creating a table containing this column. It returns {@code null} only if
     * {@code null} is the explicitely specified default value.
     */
    public Optional<Optional<T>> getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the real column name of this column.
     *
     * @return The real column name of this column.
     */
    public String getRealColumnName() {
        return realColumnName;
    }

    /**
     * Returns the SQL keywords associated with columns matching this pattern name.
     *
     * @return The SQL keywords associated with columns matching this pattern name.
     */
    public Set<Keywords> getKeywords() {
        return keywords;
    }
}
