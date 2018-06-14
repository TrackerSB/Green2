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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javafx.util.Pair;

/**
 * This enum lists all tables and their schemes needed.
 *
 * @author Stefan Huber
 */
public enum Tables {
    /**
     * Represents the table of members.
     */
    MEMBER("Mitglieder", Map.ofEntries(
            Map.entry(Columns.MEMBERSHIPNUMBER, new Pair<>(true, Set.of(Keywords.NOT_NULL, Keywords.PRIMARY_KEY))),
            Map.entry(Columns.PRENAME, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.LASTNAME, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.TITLE, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.IS_MALE, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.BIRTHDAY, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
            Map.entry(Columns.MEMBER_SINCE, new Pair<>(true, Set.of(Keywords.NOT_NULL))),
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
    /**
     * Represents a table mapping names to nicknames.
     */
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
     */
    Tables(String realTableName, Map<Columns<?>, Pair<Boolean, Set<Keywords>>> columns) {
        if (columns.values().stream().anyMatch(Objects::isNull)) {
            throw new AssertionError(
                    "Found a column which is neither marked as required nor as optional in table " + realTableName);
        }
        this.realTableName = realTableName;
        this.columns = columns;
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

    private String generateCreateStatement(SupportedDatabases dbms) {
        String columnList = getAllColumns().stream()
                .map(column -> new StringJoiner(" ")
                .add(column.getRealColumnName())
                .add(dbms.getType(column))
                .add(dbms.getKeywords(columns.get(column).getValue(), column).stream().collect(Collectors.joining(" ")))
                .toString())
                .collect(Collectors.joining(", "));
        return dbms.getTemplate(Queries.CREATE_TABLE, getRealTableName(), columnList);
    }

    /**
     * Generates a statement for the given connection independent query.
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
            case GET_COLUMN_NAMES_AND_TYPES:
                statement = dbms.getTemplate(Queries.GET_COLUMN_NAMES_AND_TYPES, databaseName, getRealTableName());
                break;
            case GET_TABLE_NAMES:
                statement = dbms.getTemplate(Queries.GET_TABLE_NAMES, databaseName);
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
