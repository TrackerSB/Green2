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
package bayern.steinbrecher.green2.connection.scheme;

import bayern.steinbrecher.green2.connection.DBConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This enum lists all tables needed.
 *
 * @author Stefan Huber
 */
public enum Tables {
    //FIXME JDK9 allows <> with anonymous inner classes.
    MEMBER("Mitglieder", new HashMap<Columns<?>, Boolean>() {
        {
            put(Columns.MEMBERSHIPNUMBER, true);
            put(Columns.PRENAME, true);
            put(Columns.LASTNAME, true);
            put(Columns.TITLE, true);
            put(Columns.IS_MALE, true);
            put(Columns.BIRTHDAY, true);
            put(Columns.STREET, true);
            put(Columns.HOUSENUMBER, true);
            put(Columns.CITY_CODE, true);
            put(Columns.CITY, true);
            put(Columns.IS_CONTRIBUTIONFREE, true);
            put(Columns.IBAN, true);
            put(Columns.BIC, true);
            put(Columns.ACCOUNTHOLDER_PRENAME, true);
            put(Columns.ACCOUNTHOLDER_LASTNAME, true);
            put(Columns.MANDAT_SIGNED, true);
            put(Columns.CONTRIBUTION, false);
            put(Columns.IS_ACTIVE, false);
        }
    }),
    //FIXME JDK9 allows <> with anonymous inner classes.
    NICKNAMES("Spitznamen", new HashMap<Columns<?>, Boolean>() {
        {
            put(Columns.NAME, true);
            put(Columns.NICKNAME, true);
        }
    });

    private final Map<Columns<?>, Boolean> columns;
    private final String realTableName;

    /**
     * Creates a representation of a scheme of a table.
     *
     * @param realTableName The name of the table in a database.
     * @param columns A map containing the columns of this table and whether they are required. {@code true} means
     * required; {@code false} means optional.
     */
    private Tables(String realTableName, Map<Columns<?>, Boolean> columns) {
        if (columns.values().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Found a column which is neither marked as required nor as optional in table " + realTableName);
        }
        this.realTableName = realTableName;
        this.columns = columns;
    }

    private void throwIfInvalid(DBConnection connection) {
        if (!isValid(connection)) {
            throw new IllegalStateException(realTableName + " is missing required columns.");
        }
    }

    /**
     * Checks whether the scheme of this table contains the given column. NOTE: This does not confirm that the this
     * column exists in the real table. It only states that the scheme can have such a column.
     *
     * @param column The column to check.
     * @return {@code true} only if this table contains {@code column}.
     */
    public boolean contains(Columns<?> column) {
        return columns.containsKey(column);
    }

    /**
     * Checks whether this table exists and has all required columns when using the given connection.
     *
     * @param connection The connection to use for checking.
     * @return {@code true} only if the table accessible using {@code connection} exists and has all required columns.
     */
    public boolean isValid(DBConnection connection) {
        return !getMissingColumns(connection).isPresent();
    }

    /**
     * Returns all required but missing or unaccessible columns of this table using the given connection.
     *
     * @param connection The connection to use.
     * @return {@link Optional#empty()} if no required column is missing or unaccessible. Otherwise an {@link Optional}
     * containing a list of these columns.
     */
    public Optional<List<Columns<?>>> getMissingColumns(DBConnection connection) {
        List<Columns<?>> missingColumns = columns.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .filter(column -> !connection.columnExists(this, column))
                .collect(Collectors.toList());
        if (missingColumns.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(missingColumns);
        }
    }

    /**
     * Checks whether the given column is an optional column of this table.
     *
     * @param column The column to check.
     * @return {@code true} only if this column is an optional column of this table.
     */
    public boolean isOptional(Columns column) {
        if (contains(column)) {
            return !columns.get(column);
        } else {
            throw new IllegalArgumentException(column + " is no column of " + realTableName);
        }
    }

    /**
     * Returns a {@link String} containing a statement with SELECT and FROM but without WHERE, with no semicolon and no
     * trailing space at the end. The select statement contains only columns existing when using {@code connection}.
     *
     * @param connection The connection to use.
     * @param columnsToSelect The columns to select when they exist.
     * @return The statement selecting all existing columns of {@code columnsToSelect}. Returns {@link Optional#empty()}
     * if {@code columnsToSelect} contains no column which exists in the scheme accessible through {@code connection}.
     * @see #generateQuery(bayern.steinbrecher.green2.connection.DBConnection,
     * bayern.steinbrecher.green2.connection.scheme.Columns...)
     */
    public Optional<String> generateQuery(DBConnection connection, Collection<Columns<?>> columnsToSelect) {
        throwIfInvalid(connection);
        List<Columns> existingColumns = columnsToSelect.stream()
                .filter(c -> connection.columnExists(this, c))
                .collect(Collectors.toList());
        if (existingColumns.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of("SELECT " + existingColumns.stream()
                    .map(Columns::getRealColumnName)
                    .collect(Collectors.joining(","))
                    + " FROM " + realTableName);
        }
    }

    /**
     * Returns a {@link String} containing a statement with SELECT and FROM but without WHERE, with no semicolon and no
     * trailing space at the end. The select statement contains only columns existing when using {@code connection}.
     *
     * @param connection The connection to use.
     * @param columnsToSelect The columns to select when they exist.
     * @return The statement selecting all existing columns of {@code columnsToSelect}. Returns {@link Optional#empty()}
     * if {@code columnsToSelect} contains no column which exists in the scheme accessible through {@code connection}.
     * @see #generateQuery(bayern.steinbrecher.green2.connection.DBConnection, java.util.Collection)
     */
    public Optional<String> generateQuery(DBConnection connection, Columns... columnsToSelect) {
        return generateQuery(connection, Arrays.asList(columnsToSelect));
    }

    /**
     * Returns the name of the table in the database scheme.
     *
     * @return The name of the table in the database scheme.
     */
    public String getRealTableName() {
        return realTableName;
    }

    /**
     * Returns the {@link Set} containing all columns this table can have according to its scheme.
     *
     * @return The {@link Set} containing all columns this table can have according to its scheme.
     */
    public Set<Columns<?>> getAllColumns() {
        return columns.keySet();
    }
}
