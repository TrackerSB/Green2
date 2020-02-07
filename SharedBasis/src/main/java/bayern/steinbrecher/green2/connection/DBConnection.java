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
package bayern.steinbrecher.green2.connection;

import bayern.steinbrecher.green2.connection.scheme.Queries;
import bayern.steinbrecher.green2.connection.scheme.SimpleColumnPattern;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.connection.scheme.Tables;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.PopulatingMap;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Represents a database connection.
 *
 * @author Stefan Huber
 */
public abstract class DBConnection implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private final String databaseName;
    private final SupportedDatabases dbms;
    /**
     * Caches the all really existing columns on every supported table in {@link Tables}. The cache is refreshed
     * whenever the currently loaded profile changes.
     */
    private final Map<Tables<?, ?>, List<Column<?>>> columnsCache;
    private final Set<String> tablesCache = new HashSet<>();

    /**
     * Creates a connection containing basic functionality independent from the way connecting to a database itself.
     *
     * @param databaseName The name of the database to connect to.
     * @param dbms The type of the database to connect to.
     */
    public DBConnection(String databaseName, SupportedDatabases dbms) {
        this.databaseName = databaseName;
        this.dbms = dbms;

        columnsCache = new PopulatingMap<>(table -> {
            if (table == null) {
                throw new IllegalArgumentException("Can not generate query controls for table null.");
            }
            List<Column<?>> entry;
            try {
                List<List<String>> result
                        = execQuery(table.generateQuery(Queries.GET_COLUMN_NAMES_AND_TYPES, dbms, databaseName));
                entry = result.stream()
                        .skip(1) //Skip headings
                        .map(list -> {
                            return dbms.getType(list.get(1))
                                    .map(ct -> new Column<>(list.get(0), ct));
                        })
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                entry = new ArrayList<>();
            }
            return entry;
        });
    }

    /**
     * Closes this connection.
     */
    @Override
    public abstract void close();

    /**
     * Executes a query and returns the result.
     *
     * @param sqlCode The sql code to execute.
     * @return Table containing the results AND the headings of each column. First dimension rows; second columns.
     * @throws SQLException Thrown if the sql code is invalid.
     */
    public abstract List<List<String>> execQuery(String sqlCode) throws SQLException;

    /**
     * Executes a command like INSERT INTO, UPDATE or CREATE.
     *
     * @param sqlCode The sql code to execute.
     * @throws SQLException Thrown if the sql code is invalid.
     */
    public abstract void execUpdate(String sqlCode) throws SQLException;

    private void populateTablesCache() {
        synchronized (tablesCache) {
            if (tablesCache.isEmpty()) {
                List<List<String>> result;
                try {
                    result = execQuery( //NOTE The concrete table does not matter.
                            Tables.MEMBER.generateQuery(Queries.GET_TABLE_NAMES, dbms, databaseName));
                    tablesCache.addAll(result.stream()
                            //Skip column name
                            .skip(1)
                            .map(list -> list.get(0))
                            .collect(Collectors.toList()));
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Checks if the connected database exists.
     *
     * @return {@code true} only if the connected database exists.
     */
    public boolean databaseExists() {
        List<List<String>> result;
        try {
            result = execQuery(Tables.MEMBER.generateQuery(Queries.CHECK_DBMS_EXISTS, dbms, databaseName));
            return !result.isEmpty() && !result.get(0).isEmpty();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Checks whether the given table exists. It DOES NOT check whether it has all needed columns and is configured
     * right.
     *
     * @param table The table to search for.
     * @return {@code true} only if the given table exist.
     */
    public boolean tableExists(Tables<?, ?> table) {
        populateTablesCache();
        return tablesCache.contains(table.getRealTableName());
    }

    /**
     * Creates all tables if they do not already exist and the user confirms the creation.
     *
     * @return {@code true} only if either no table is missing or the missing tables were created.
     * @throws SchemeCreationException Thrown, only if there are tables missing in the database, the user confirmed the
     * creation and they could not be created.
     */
    public boolean createTablesIfNeeded() throws SchemeCreationException {
        List<Tables<?, ?>> missingTables = Arrays.stream(Tables.values())
                .filter(table -> !tableExists(table))
                .collect(Collectors.toList());
        boolean tablesAreMissing = !missingTables.isEmpty();
        boolean tablesCreated = false;
        if (tablesAreMissing) {
            //FIXME Check rights for creating tables
            StringJoiner message = new StringJoiner("\n").add(EnvironmentHandler.getResourceValue("tablesMissing"));
            missingTables.forEach(table -> message.add(table.getRealTableName()));
            Optional<ButtonType> resultButton = DialogUtility.showAndWait(DialogUtility.createAlert(
                    Alert.AlertType.CONFIRMATION, message.toString(), ButtonType.YES, ButtonType.NO));
            if (resultButton.isPresent() && resultButton.get() == ButtonType.YES) {
                for (Tables<?, ?> table : missingTables) {
                    try {
                        execUpdate(table.generateQuery(Queries.CREATE_TABLE, dbms, databaseName));
                    } catch (SQLException ex) {
                        throw new SchemeCreationException("Could not create table " + table.getRealTableName(), ex);
                    }
                }
                tablesCreated = true;
            }
        }
        return !tablesAreMissing || tablesCreated;
    }

    /**
     * Returns a {@link String} containing a statement with SELECT, FROM and WHERE, but with no semicolon and no
     * trailing space at the end. The select statement contains only columns existing and associated with {@code table}.
     * The where clause excludes conditions references a column contained in {@code columnsToSelect} which do not exist.
     *
     * @param table The table to select from.
     * @param columnsToSelect The columns to select if they exist. If it is empty all available columns are queried.
     * @param conditions The conditions every row has to satisfy. NOTE Occurrences of identifiers with conditions are
     * not automatically quoted by this method.
     * @return The statement selecting all columns given in {@code columnsToSelect} satisfying all {@code conditions}.
     * Returns {@link Optional#empty()} if {@code columnsToSelect} contains no column.
     */
    // FIXME The method does not tell which condtions were excluded.
    public Optional<String> generateSearchQueryFromColumns(Tables<?, ?> table, Collection<Column<?>> columnsToSelect,
            Optional<Collection<String>> conditions) {
        Optional<String> searchQuery;
        if (columnsToSelect.isEmpty()) {
            LOGGER.log(Level.WARNING, "Generating search query without selecting any existing column.");
            searchQuery = Optional.empty();
        } else {
            StringBuilder sqlString = new StringBuilder("SELECT ")
                    .append(
                            columnsToSelect.stream()
                                    .map(Column::getName)
                                    .map(dbms::quoteIdentifier)
                                    .collect(Collectors.joining(", "))
                    )
                    .append(" FROM ")
                    .append(dbms.quoteIdentifier(table.getRealTableName()));
            if (conditions.isPresent() && !conditions.get().isEmpty()) {
                String conditionString = conditions.get()
                        .stream()
                        // FIXME This method does not check whether all the column identifiers in any conditions exist.
                        .collect(Collectors.joining(" AND "));
                sqlString.append(" WHERE ")
                        .append(conditionString);
            }
            searchQuery = Optional.of(sqlString.toString());
        }
        return searchQuery;
    }

    /**
     * Returns an object representing all current entries of the given table.
     *
     * @param <T> The type that represents the whole content of the given table.
     * @param table The table to query all its data from.
     * @return The table to request all data from.
     */
    public <T> T getTableContent(Tables<T, ?> table) {
        if (hasValidSchemes()) {
            T tableContent;
            Optional<String> searchQuery
                    = generateSearchQueryFromColumns(table, columnsCache.get(table), Optional.empty());
            if (searchQuery.isPresent()) {
                try {
                    tableContent = table.generateRepresentations(execQuery(searchQuery.get()));
                } catch (SQLException ex) {
                    throw new Error("Generated SQL-Code invalid", ex); //NOPMD - Indicates bug in hardcoded SQL.
                }
            } else {
                throw new Error("The cache of the columns of the table " + table.getRealTableName()
                        + " contains no columns that are part of the actual table.");
            }
            return tableContent;
        } else {
            throw new IllegalStateException("The data base has an invalid scheme.");
        }
    }

    /**
     * Checks whether all needed tables are accessible using this connection and have all required columns which are
     * also accessible.
     *
     * @return {@code true} only if all needed tables and their required columns exist and are accessible.
     */
    public boolean hasValidSchemes() {
        return !getMissingColumns().isPresent();
    }

    /**
     * Checks whether all needed tables are accessible using this connection and have all required columns which are
     * also accessible.
     *
     * @return {@link Optional#empty()} if all tables have all required columns. Otherwise returns an {@link Optional}
     * mapping invalid tables to the required columns missing.
     */
    public Optional<Map<Tables<?, ?>, List<SimpleColumnPattern<?, ?>>>> getMissingColumns() {
        Map<Tables<?, ?>, List<SimpleColumnPattern<?, ?>>> missingColumns = new HashMap<>();
        for (Tables<?, ?> table : Tables.values()) {
            List<Column<?>> cachedColumns = columnsCache.get(table);
            List<SimpleColumnPattern<?, ?>> currentMissingColumns = table.getRequiredColumns()
                    .stream()
                    .filter(scp -> cachedColumns.stream().noneMatch(column -> scp.matches(column.getName())))
                    .collect(Collectors.toList());
            if (!currentMissingColumns.isEmpty()) {
                missingColumns.put(table, currentMissingColumns);
            }
        }
        return Optional.ofNullable(missingColumns.isEmpty() ? null : missingColumns);
    }

    /**
     * Generates a {@link String} listing all required but missing columns of all tables.
     *
     * @return A {@link String} listing all required but missing columns of all tables.
     * @see #getMissingColumns()
     */
    public String getMissingColumnsString() {
        Optional<Map<Tables<?, ?>, List<SimpleColumnPattern<?, ?>>>> missingColumns = getMissingColumns();
        String missingColumnsString;
        if (missingColumns.isPresent()) {
            missingColumnsString = missingColumns.get().entrySet().parallelStream()
                    .map(entry -> entry.getKey().getRealTableName() + ":\n"
                    + entry.getValue().stream()
                            .map(SimpleColumnPattern::getRealColumnName)
                            .sorted()
                            .collect(Collectors.joining(", ")))
                    .collect(Collectors.joining("\n"));
        } else {
            missingColumnsString = "";
        }
        return missingColumnsString;
    }

    /**
     * Returns a {@link List} of all existing columns (not only that ones declared in the scheme) and their
     * <strong>actual</strong> types in the database which may defer from the one declared in the scheme of the table.
     *
     * @param table The table to get the columns for.
     * @return A {@link List} of all existing columns (not only that ones declared in the scheme).
     * @see Tables#getAllColumns()
     */
    public List<Column<?>> getAllColumns(Tables<?, ?> table) {
        return columnsCache.get(table);
    }

    /**
     * Returns all available table names of accessible over this connection.
     *
     * @return All available table names of accessible over this connection.
     */
    public Set<String> getAllTables() {
        populateTablesCache();
        return tablesCache;
    }

    /**
     * Returns the name of the database as used in SQL statements.
     *
     * @return The name of the database as used in SQL statements.
     */
    public String getDatabaseName() {
        return databaseName;
    }
}
