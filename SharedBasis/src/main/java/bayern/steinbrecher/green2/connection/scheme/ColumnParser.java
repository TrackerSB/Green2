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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains singeltons for converting objects from and to their SQL representation.
 *
 * @author Stefan Huber
 * @since 2u14
 * @param <T> The type to convert from and to a SQL representation.
 */
//TODO Wait for generic enums
public abstract class ColumnParser<T> {

    public static final ColumnParser<String> STRING_COLUMN_PARSER = new ColumnParser<String>() {
        @Override
        public Optional<String> parse(String value) {
            return Optional.of(value);
        }

        @Override
        protected String toStringImpl(String value) {
            /*
             * Single quotes for Strings should be preffered since single quotes always work in ANSI SQL. In MySQL they
             * may be quoted in double quotes which is only working if ANSI_QUOTES is NOT enabled (it is per default
             * disabled).
             */
            return "'" + value + "'";
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    };
    public static final ColumnParser<Integer> INTEGER_COLUMN_PARSER = new ColumnParser<Integer>() {
        @Override
        public Optional<Integer> parse(String value) {
            Optional<Integer> parsedValue;
            try {
                parsedValue = Optional.of(Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                Logger.getLogger(ColumnParser.class.getName()).log(Level.WARNING, null, ex);
                parsedValue = Optional.empty();
            }
            return parsedValue;
        }

        @Override
        public Class<Integer> getType() {
            return Integer.class;
        }
    };
    public static final ColumnParser<Boolean> BOOLEAN_COLUMN_PARSER = new ColumnParser<Boolean>() {
        @Override
        public Optional<Boolean> parse(String value) {
            return Optional.of(value.equalsIgnoreCase("1"));
        }

        @Override
        protected String toStringImpl(Boolean value) {
            return value ? "TRUE" : "FALSE";
        }

        @Override
        public Class<Boolean> getType() {
            return Boolean.class;
        }
    };
    public static final ColumnParser<LocalDate> LOCALDATE_COLUMN_PARSER = new ColumnParser<LocalDate>() {
        @Override
        public Optional<LocalDate> parse(String value) {
            LocalDate date = null;
            try {
                if (value == null) {
                    //NOTE This case is intruduced to throw a DateTimeParseException instead of a NPE.
                    throw new DateTimeParseException("Can´t parse null", "null", 0);
                } else {
                    date = LocalDate.parse(value);
                }
            } catch (DateTimeParseException ex) {
                Logger.getLogger(ColumnParser.class.getName()).log(Level.WARNING, value + " is an invalid date", ex);
            }
            return Optional.ofNullable(date);
        }

        @Override
        protected String toStringImpl(LocalDate value) {
            return "'" + String.valueOf(value) + "'";
        }

        @Override
        public Class<LocalDate> getType() {
            return LocalDate.class;
        }
    };
    public static final ColumnParser<Double> DOUBLE_COLUMN_PARSER = new ColumnParser<Double>() {
        @Override
        public Optional<Double> parse(String value) {
            Optional<Double> parsedValue;
            try {
                parsedValue = Optional.of(Double.parseDouble(value));
            } catch (NumberFormatException ex) {
                Logger.getLogger(ColumnParser.class.getName()).log(Level.WARNING, null, ex);
                parsedValue = Optional.empty();
            }
            return parsedValue;
        }

        @Override
        public Class<Double> getType() {
            return Double.class;
        }
    };

    private ColumnParser() {
        //Prohibit construction of additional parser outside this class
    }

    /**
     * Parses the given value to the appropriate type of this column if possible. Returns {@link Optional#empty()} if it
     * could not be converted.
     *
     * @param value The value to parse.
     * @return The typed value represented by {@code value}.
     */
    public abstract Optional<T> parse(String value);

    /**
     * Returns the {@link String} representation of the default value suitable for SQL.NOTE: For implementation it can
     * be assumed that the value is not {@code null} since this is handled by {@link #toString(java.lang.Object)}. The
     * default implementation just calls {@link String#valueOf(java.lang.Object)}.
     *
     * @param value The value to convert.
     * @return The {@link String} representation of the given value suitable for SQL.
     * @see #getDefaultValue()
     */
    protected String toStringImpl(T value) {
        return String.valueOf(value);
    }

    /**
     * Parses the given value into a {@link String} representation suitable for SQL. Returns the {@link String} "NULL"
     * (without quotes) if {@code value} is {@code null}.
     *
     * @param value The value to convert.
     * @return A {@link String} representation of the given value suitable for SQL.
     */
    public final String toString(T value) {
        String valueSql;
        if (value == null) {
            valueSql = "NULL";
        } else {
            valueSql = toStringImpl(value);
        }
        return valueSql;
    }

    /**
     * Returns the generic type of the class. This method is needed since type ereasure takes place.
     *
     * @return The generic type of the class.
     */
    public abstract Class<T> getType();
}
