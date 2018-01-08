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

import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases.Keywords;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases.Queries;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javafx.util.Pair;

/**
 * This enum lists all tables needed.
 *
 * @author Stefan Huber
 */
public enum Tables {
    MEMBER("Mitglieder", Map.ofEntries(
            Map.entry(Columns.MEMBERSHIPNUMBER, new Pair<>(true, Set.of(Keywords.NOT_NULL, Keywords.PRIMARY_KEY))),
            Map.entry(Columns.PRENAME, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.LASTNAME, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.TITLE, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.IS_MALE, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.BIRTHDAY, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.STREET, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.HOUSENUMBER, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.CITY_CODE, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.CITY, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.IS_CONTRIBUTIONFREE, new Pair<>(true, Set.of(Keywords.DEFAULT, Keywords.NOT_NULL))),
            Map.entry(Columns.IBAN, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.BIC, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.ACCOUNTHOLDER_PRENAME, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.ACCOUNTHOLDER_LASTNAME, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.MANDAT_SIGNED, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.CONTRIBUTION, new Pair<>(false, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.IS_ACTIVE, new Pair<>(false, Set.of(Keywords.NOT_NULL)))
    )),
    NICKNAMES("Spitznamen", Map.of(
            Columns.NAME, new Pair<>(true, Set.of(Keywords.NOT_NULL, Keywords.PRIMARY_KEY)),
            Columns.NICKNAME, new Pair<>(true, Set.of(Keywords.NOT_NULL))
    ));

    private final Map<Columns<?>, Pair<Boolean, Set<Keywords>>> columns;
    private final String realTableName;

    /**
     * Creates a representation of a scheme of a table.
     *
     * @param realTableName The name of the table in a database.
     * @param columns A map containing the columns of this table, whether they are required ({@code true} means
     * required; {@code false} means optional.) and their attributes.
     * @param createTemplate The template of the CREATE statement of a table.
     */
    private Tables(String realTableName, Map<Columns<?>, Pair<Boolean, Set<Keywords>>> columns) {
        if (columns.values().stream().anyMatch(Objects::isNull)) {
            throw new Error(
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
                .filter(entry -> entry.getValue().getKey())
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
            return !columns.get(column).getKey();
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
     * @see #generateSearchQuery(bayern.steinbrecher.green2.connection.DBConnection,
     * bayern.steinbrecher.green2.connection.scheme.Columns[])
     */
    public Optional<String> generateSearchQuery(DBConnection connection, Collection<Columns<?>> columnsToSelect) {
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
     * @see #generateSearchQuery(bayern.steinbrecher.green2.connection.DBConnection, java.util.Collection)
     */
    public Optional<String> generateSearchQuery(DBConnection connection, Columns... columnsToSelect) {
        return generateSearchQuery(connection, Arrays.asList(columnsToSelect));
    }

    private String generateCreateStatement(SupportedDatabases dbms) {
        String columnList = getAllColumns().stream()
                .map(column -> new StringJoiner(" ")
                .add(column.getRealColumnName())
                .add(dbms.getType(column))
                .add(dbms.getKeywords(columns.get(column).getValue()).stream().collect(Collectors.joining(" ")))
                .toString())
                .collect(Collectors.joining(", "));
        return dbms.getTemplate(SupportedDatabases.Queries.CREATE_TABLE, getRealTableName(), columnList);
    }

    /**
     * Generates a statement for the given query.
     *
     * @param query The query to generate a statement for.
     * @param dbms The dbms to create the statement for.
     * @param databaseName The name of the database to use.
     * @return The generated statement.
     */
    //FIXME Think about where to move these generateQuery methods
    public String generateQuery(Queries query, SupportedDatabases dbms, String databaseName) {
        String statement;
        switch (query) {
            case CREATE_TABLE:
                statement = generateCreateStatement(dbms);
                break;
            case GET_COLUMN_NAMES:
                statement = dbms.getTemplate(Queries.GET_COLUMN_NAMES, databaseName, getRealTableName());
                break;
            case GET_TABLE_NAMES:
                statement = dbms.getTemplate(Queries.GET_TABLE_NAMES, databaseName, getRealTableName());
                break;
            default:
                throw new UnsupportedOperationException("The query " + query + " is not implemented, yet.");
        }
        return statement;
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
