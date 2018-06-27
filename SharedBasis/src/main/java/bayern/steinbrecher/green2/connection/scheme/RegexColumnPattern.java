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

import bayern.steinbrecher.green2.utility.TriFunction;
import java.util.function.Function;

/**
 * Represents a {@link ColumnPattern} which may match a range of column names instead of a specific one like
 * {@link SimpleColumnPattern}.
 *
 * @author Stefan Huber
 * @see SimpleColumnPattern
 * @since 2u14
 * @param <T> The type of the column content.
 * @param <U> The type of object to set the content of this column to.
 * @param <K> The type of the key to distinguish the columns matching this pattern.
 */
public class RegexColumnPattern<T, U, K> extends ColumnPattern<T, U> {

    private final TriFunction<U, K, T, U> setter;
    private final Function<String, K> keyExtractor;

    /**
     * Creates a column pattern possibly matching multiple column names.
     *
     * @param columnNamePattern The pattern of column names to match.
     * @param keywords The keywords to specify when creating a column matching this pattern.
     * @param parser The parser to convert values from and to a SQL representation.
     * @param setter The function used to set a parsed value to a given object. The setter should only return a new
     * object of type {@link U} if the handed in one is immutable.
     */
    //TODO How to introduce the consumer version (to spare some "return this") without ambigousity?
//    public RegexColumnPattern(String columnNamePattern, Set<Keywords> keywords, ColumnParser<T> parser,
//            TriConsumer<U, K, T> setter) {
//        this(columnNamePattern, keywords, parser, setter, null);
//    }
    /**
     * Creates a column pattern possibly matching multiple column names. This constructor may be used if {@link U} is an
     * immutable type.
     *
     * @param columnNamePattern The pattern of column names to match.
     * @param parser The parser to convert values from and to a SQL representation.
     * @param setter The function used to set a parsed value to a given object. The setter should only return a new
     * object of type {@link U} if the handed in one is immutable.
     * @param keyExtractor Extracts the key for a given column name matching this pattern.
     * @see ColumnPattern#ColumnPattern(java.lang.String, java.util.Set,
     * bayern.steinbrecher.green2.connection.scheme.ColumnParser, java.util.Optional)
     */
    public RegexColumnPattern(String columnNamePattern, ColumnParser<T> parser, TriFunction<U, K, T, U> setter,
            Function<String, K> keyExtractor) {
        super(columnNamePattern, parser);
        this.setter = setter;
        this.keyExtractor = keyExtractor;
    }

    /**
     * Parses the given value and sets it to the object of type {@link U}.
     *
     * @param toSet The object to set the parsed value to.
     * @param columnName The column name matching this pattern to extract the key from.
     * @param value The value to parse and to set.
     * @return The resulting object of type {@link U}.
     */
    public U combine(U toSet, String columnName, String value) {
        if (getColumnNamePattern().matcher(columnName).matches()) {
            K key = keyExtractor.apply(columnName);
            T parsedValue = getParser().parse(value)
                    .orElseThrow(() -> new IllegalArgumentException("Can not parse " + value));
            return setter.accept(toSet, key, parsedValue);
        } else {
            throw new IllegalArgumentException("The given column name does not match this pattern.");
        }
    }
}
