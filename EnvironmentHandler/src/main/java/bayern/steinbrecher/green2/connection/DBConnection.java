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

import bayern.steinbrecher.green2.connection.scheme.Columns;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases.Queries;
import bayern.steinbrecher.green2.connection.scheme.Tables;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.generator.MemberGenerator;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.utility.DialogUtility;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;

/**
 * Represents a database connection.
 *
 * @author Stefan Huber
 */
public abstract class DBConnection implements AutoCloseable {

    /**
     * Caches the names of all columns (not only the ones defined in {@link Columns}). The cache is refreshed whenever
     * the currently loaded profile changes.
     */
    private static final Map<Tables, List<String>> EXISTING_COLUMNS_CACHE = new HashMap<>();
    private static final List<String> TABLES_CACHE = new ArrayList<>();
    private static Pair<String, SupportedDatabases> nameAndTypeOfDatabaseCache = null;

    static {
        EnvironmentHandler.loadedProfileProperty().addListener(change -> {
            EXISTING_COLUMNS_CACHE.clear();
            TABLES_CACHE.clear();
            nameAndTypeOfDatabaseCache = null;
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

    /**
     * Returns a {@link Map} containing entries assigning the entries of {@code headings} which contain the headings of
     * a query the number of the column. It stores no entry if this entry of {@code headings} contains a column which is
     * not part of the scheme. It also stores no entry if an entry of the scheme is not found in {@code headings}.
     *
     * @param table The table containing the scheme to use.
     * @param headings The headings of the query to map column numbers to.
     * @return The {@link Map} containing entries assigning the entries of {@code headings} which contain the headings
     * of a query the number of the column.
     */
    public static Map<Columns<?>, Integer> generateColumnMapping(Tables table, List<String> headings) {
        Map<Columns<?>, Integer> columnsMapping = new HashMap<>();
        table.getAllColumns().forEach(column -> {
            for (int i = 0; i < headings.size(); i++) {
                if (column.getRealColumnName().equalsIgnoreCase(headings.get(i))) {
                    columnsMapping.put(column, i);
                    break;
                }
            }
        });

        return columnsMapping;
    }

    private Pair<String, SupportedDatabases> getNameAndTypeOfDatabase() {
        if (nameAndTypeOfDatabaseCache == null) {
            Profile profile = EnvironmentHandler.getProfile();
            nameAndTypeOfDatabaseCache
                    = new Pair<>(profile.get(ProfileSettings.DATABASE_NAME), profile.get(ProfileSettings.DBMS));
        }
        return nameAndTypeOfDatabaseCache;
    }

    /**
     * Checks whether the given table exists. It DOES NOT check whether it has all needed columns and is configured
     * right.
     *
     * @param table The table to search for.
     * @return {@code true} only if the given table exist.
     */
    public boolean tableExists(Tables table) {
        synchronized (TABLES_CACHE) {
            if (TABLES_CACHE.isEmpty()) {
                Pair<String, SupportedDatabases> profileInfo = getNameAndTypeOfDatabase();
                List<List<String>> result;
                try {
                    result = execQuery(
                            table.generateQuery(Queries.GET_TABLE_NAMES, profileInfo.getValue(), profileInfo.getKey()));
                    TABLES_CACHE.addAll(result.stream()
                            //Skip column name
                            .skip(1)
                            .map(list -> list.get(0))
                            .collect(Collectors.toList()));
                } catch (SQLException ex) {
                    Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return TABLES_CACHE.contains(table.getRealTableName());
    }

    /**
     * Creates all tables if they do not already exist and the user confirms the creation.
     *
     * @return {@code true} only if either no table is missing or the missing tables were created.
     * @throws SchemeCreationException Thrown, only if there are tables missing in the database, the user confirmed the
     * creation and they could not be created.
     */
    public boolean createTablesIfNeeded() throws SchemeCreationException {
        List<Tables> missingTables = Arrays.stream(Tables.values())
                .filter(table -> !tableExists(table))
                .collect(Collectors.toList());
        boolean tablesAreMissing = !missingTables.isEmpty();
        boolean tablesCreated = false;
        if (tablesAreMissing) {
            //FIXME Check rights for creating tables
            StringJoiner message = new StringJoiner("\n").add(EnvironmentHandler.getResourceValue("tablesMissing"));
            missingTables.forEach(table -> message.add(table.getRealTableName()));
            FutureTask<Optional<ButtonType>> askForCreationConfirmation = new FutureTask<>(() -> {
                Alert creationConfirmation = DialogUtility.createAlert(
                        Alert.AlertType.CONFIRMATION, message.toString(), ButtonType.YES, ButtonType.NO);
                return creationConfirmation.showAndWait();
            });
            Platform.runLater(() -> askForCreationConfirmation.run());
            try {
                Optional<ButtonType> resultButton = askForCreationConfirmation.get();
                if (resultButton.isPresent() && resultButton.get() == ButtonType.YES) {
                    for (Tables table : missingTables) {
                        try {
                            Pair<String, SupportedDatabases> profileInfo = getNameAndTypeOfDatabase();
                            execUpdate(table.generateQuery(
                                    Queries.CREATE_TABLE, profileInfo.getValue(), profileInfo.getKey()));
                        } catch (SQLException ex) {
                            throw new SchemeCreationException("Could not create table " + table.getRealTableName(), ex);
                        }
                    }
                    tablesCreated = true;
                }
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return !tablesAreMissing || tablesCreated;
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
    public Optional<Map<Tables, List<Columns<?>>>> getMissingColumns() {
        Map<Tables, List<Columns<?>>> missingColumns = new HashMap<>();
        for (Tables table : Tables.values()) {
            table.getMissingColumns(this).ifPresent(mc -> missingColumns.put(table, mc));
        }
        if (missingColumns.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(missingColumns);
        }
    }

    /**
     * Returns a list of all member accessible with this connection. The list contains all columns returned by
     * {@link Tables#getAllColumns()} called on {@link Tables#MEMBER}.
     *
     * @return The list with the member.
     */
    public Set<Member> getAllMember() {
        try {
            return MemberGenerator.generateMemberList(
                    execQuery(Tables.MEMBER.generateSearchQuery(this, Tables.MEMBER.getAllColumns()).get()));
        } catch (SQLException ex) {
            throw new Error("Hardcoded SQL-Code invalid", ex);
        }
    }

    /**
     * Queries the nickname table of the specified connection.
     *
     * @return A map from first names to nicknames.
     */
    public Map<String, String> getAllNicknames() {
        try {
            List<List<String>> queriedNicknames
                    = execQuery(Tables.NICKNAMES.generateSearchQuery(this, Tables.NICKNAMES.getAllColumns()).get());

            Map<String, String> mappedNicknames = new HashMap<>();
            queriedNicknames.parallelStream()
                    .skip(1) //Skip headings
                    .forEach(row -> mappedNicknames.put(row.get(0), row.get(1)));
            return mappedNicknames;
        } catch (SQLException ex) {
            throw new Error("Hardcoded SQL-Code invalid", ex);
        }
    }

    /**
     * Checks whether the given table of the configured database contains a specific column. You should NEVER call this
     * function with parameters provided by the user in order to prohibit SQL INJECTION. This method does not check
     * whether the scheme of the given table contains the given column. NOTE: For this function to work for sure the
     * table should have at least one row. When having no rows the database may return nothing not even the headings.
     *
     * @param table The name of the table to search for the column.
     * @param column The column name to search for.
     * @return {@code true} only if the given table contains the given column.
     */
    public boolean columnExists(Tables table, Columns<?> column) {
        synchronized (EXISTING_COLUMNS_CACHE) {
            if (!EXISTING_COLUMNS_CACHE.containsKey(table)) {
                Pair<String, SupportedDatabases> profileInfo = getNameAndTypeOfDatabase();
                try {
                    List<List<String>> result = execQuery(
                            table.generateQuery(Queries.GET_COLUMN_NAMES, profileInfo.getValue(), profileInfo.getKey()));
                    List<String> listOfColumns = result
                            .stream()
                            .map(list -> list.get(0))
                            .collect(Collectors.toList());
                    //NOTE Don´t use putIfAbsent(...) since it is lacking lazy evaluation for the second argument.
                    EXISTING_COLUMNS_CACHE.put(table, listOfColumns.subList(1, listOfColumns.size()));
                } catch (SQLException ex) {
                    Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //TODO Think about ignoring small/capital letters in column names
            return EXISTING_COLUMNS_CACHE.get(table).stream()
                    .filter(c -> c.equalsIgnoreCase(column.getRealColumnName()))
                    .findAny()
                    .isPresent();
        }
    }
}
