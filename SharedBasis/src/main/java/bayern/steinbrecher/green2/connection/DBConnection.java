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
import bayern.steinbrecher.green2.connection.scheme.Queries;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.connection.scheme.Tables;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.generator.MemberGenerator;
import bayern.steinbrecher.green2.generator.NicknameGenerator;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.utility.DialogUtility;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    private static final Map<Tables, List<Pair<String, Class<?>>>> EXISTING_COLUMNS_CACHE = new HashMap<>();
    private static final List<String> TABLES_CACHE = new ArrayList<>();
    //TODO Any solution to decouple Profile from DBConnection?
    private static final ObjectProperty<Optional<Pair<String, SupportedDatabases>>> NAME_AND_TYPE_OF_DATABASE
            = new SimpleObjectProperty<>(Optional.empty());

    static {
        EnvironmentHandler.loadedProfileProperty().addListener(change -> {
            EXISTING_COLUMNS_CACHE.clear();
            TABLES_CACHE.clear();
            NAME_AND_TYPE_OF_DATABASE.set(Optional.empty());
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
        synchronized (NAME_AND_TYPE_OF_DATABASE) {
            if (!NAME_AND_TYPE_OF_DATABASE.get().isPresent()) {
                Profile profile = EnvironmentHandler.getProfile();
                NAME_AND_TYPE_OF_DATABASE.set(Optional.of(
                        new Pair<>(profile.get(ProfileSettings.DATABASE_NAME), profile.get(ProfileSettings.DBMS))));
            }
        }
        return NAME_AND_TYPE_OF_DATABASE.get().get();
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
                            //FIXME Ignore case?
                            .map(list -> list.get(0).toLowerCase())
                            .collect(Collectors.toList()));
                } catch (SQLException ex) {
                    Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //FIXME Ignore case?
        return TABLES_CACHE.contains(table.getRealTableName().toLowerCase(Locale.ROOT));
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
            Optional<ButtonType> resultButton = DialogUtility.showAndWait(DialogUtility.createAlert(
                    Alert.AlertType.CONFIRMATION, message.toString(), ButtonType.YES, ButtonType.NO));
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
        }
        return !tablesAreMissing || tablesCreated;
    }

    private void throwIfInvalid() {
        String missingColumnsString = getMissingColumnsString();
        if (!missingColumnsString.isEmpty()) {
            throw new IllegalStateException("The connection refers tables which are missing required columns:\n"
                    + missingColumnsString);
        }
    }

    /**
     * Returns a {@link String} containing a statement with SELECT and FROM but without WHERE, with no semicolon and no
     * trailing space at the end. The select statement contains only columns existing and associated with {@code table}.
     * The where clause excludes conditions references a column contained in {@code columnsToSelect} which do not exist.
     * NOTE: Other non existing columns are not excluded from the where clause yet.
     *
     * @param table The table to select from.
     * @param columnsToSelect The columns to select when they exist. If it is empty all available columns are queried.
     * @param conditions The conditions every row has to satisfy. NOTE Occurrences of identifiers with conditions are
     * not automatically quoted by this method.
     * @return The statement selecting all existing columns of {@code columnsToSelect} satisfying all
     * {@code conditions}. Returns {@link Optional#empty()} if {@code columnsToSelect} contains no column which exists
     * in the scheme accessible through {@code connection}.
     * @see #generateSearchQuery(bayern.steinbrecher.green2.connection.scheme.Tables, java.util.Collection,
     * java.util.Collection)
     */
    //FIXME The method does not tell which columns and condtions were excluded.
    public Optional<String> generateSearchQuery(Tables table, Collection<String> columnsToSelect,
            Collection<String> conditions) {
        throwIfInvalid();
        List<String> existingColumns;
        List<String> notExistingColumns = new ArrayList<>();
        if (columnsToSelect.isEmpty()) {
            checkExistingColumnsCache(table);
            existingColumns = EXISTING_COLUMNS_CACHE.get(table).stream()
                    .map(Pair::getKey)
                    .collect(Collectors.toList());
        } else {
            existingColumns = new ArrayList<>();
            columnsToSelect.stream().forEach((String cn) -> {
                if (columnExists(table, cn)) {
                    existingColumns.add(cn);
                } else {
                    notExistingColumns.add(cn);
                }
            });
        }
        if (existingColumns.isEmpty()) {
            Logger.getLogger(DBConnection.class.getName())
                    .log(Level.WARNING, "Generating search query without selecting any existing column.");
            return Optional.empty();
        } else {
            String conditionString;
            if (conditions.isEmpty()) {
                conditionString = "";
            } else {
                List<Pattern> notExistingColumnsPattern = notExistingColumns.stream()
                        //Used regex: (?:^|.*\W)columnName(?:\W.*|$)
                        //Tested at: http://www.regexplanet.com/advanced/java/index.html
                        //TODO Should it be case sensitive?
                        .map(cn -> Pattern.compile("(?:^|.*\\\\W)" + cn + "(?:\\\\W.*|$)", Pattern.CASE_INSENSITIVE))
                        .collect(Collectors.toList());
                conditionString = conditions.stream()
                        .filter(c -> notExistingColumnsPattern.stream().noneMatch(p -> p.matcher(c).matches()))
                        .collect(Collectors.joining(" AND "));
            }
            SupportedDatabases dbms = getNameAndTypeOfDatabase().getValue();
            return Optional.of("SELECT " + existingColumns.stream()
                    .map(dbms::quoteIdentifier)
                    .collect(Collectors.joining(", "))
                    + " FROM " + dbms.quoteIdentifier(table.getRealTableName())
                    + (conditionString.isEmpty() ? "" : " WHERE " + conditionString));
        }
    }

    /**
     * Returns a {@link String} containing a statement with SELECT and FROM but without WHERE, with no semicolon and no
     * trailing space at the end. The select statement contains only columns existing and associated with {@code table}.
     *
     * @param table The table to select from.
     * @param columnsToSelect The columns to select when they exist. If it is empty all available columns are queried.
     * @return The statement selecting all existing columns of {@code columnsToSelect}. Returns {@link Optional#empty()}
     * if {@code columnsToSelect} contains no column which exists in the scheme accessible through {@code connection}.
     * @see #generateSearchQuery(bayern.steinbrecher.green2.connection.scheme.Tables, java.util.Collection,
     * java.util.Collection)
     */
    public Optional<String> generateSearchQuery(Tables table, Collection<Columns<?>> columnsToSelect) {
        List<String> columnNamesToSelect = columnsToSelect.stream()
                .map(Columns::getRealColumnName)
                .collect(Collectors.toList());
        return generateSearchQuery(table, columnNamesToSelect, new ArrayList<>());
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
            List<Columns<?>> currentMissingColumns = table.getAllColumns().stream()
                    .filter(c -> !table.isOptional(c))
                    .filter(c -> !columnExists(table, c))
                    .collect(Collectors.toList());
            if (!currentMissingColumns.isEmpty()) {
                missingColumns.put(table, currentMissingColumns);
            }
        }
        if (missingColumns.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(missingColumns);
        }
    }

    /**
     * Creates a {@link String} out of the result of {@link #getMissingColumns()}.
     *
     * @param missingColumns The required columns missing.
     * @return
     */
    private static String generateMissingColumnsString(Optional<Map<Tables, List<Columns<?>>>> missingColumns) {
        if (missingColumns.isPresent()) {
            return missingColumns.get().entrySet().parallelStream()
                    .map(entry -> entry.getKey().getRealTableName() + ":\n"
                    + entry.getValue().stream()
                            .map(Columns::getRealColumnName)
                            .sorted()
                            .collect(Collectors.joining(", ")))
                    .collect(Collectors.joining("\n"));
        } else {
            return "";
        }
    }

    /**
     * Generates a {@link String} listing all required but missing columns of all tables.
     *
     * @return A {@link String} listing all required but missing columns of all tables.
     * @see #getMissingColumns()
     */
    public String getMissingColumnsString() {
        return generateMissingColumnsString(getMissingColumns());
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
                    execQuery(generateSearchQuery(Tables.MEMBER, Tables.MEMBER.getAllColumns()).get()));
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
            return NicknameGenerator.generateNicknames(
                    execQuery(generateSearchQuery(Tables.NICKNAMES, Tables.NICKNAMES.getAllColumns()).get()));
        } catch (SQLException ex) {
            throw new Error("Hardcoded SQL-Code invalid", ex);
        }
    }

    //FIXME How to make sure it is called every time HashMap<>#get(...) is called?
    private void checkExistingColumnsCache(Tables table) {
        synchronized (EXISTING_COLUMNS_CACHE) {
            if (!EXISTING_COLUMNS_CACHE.containsKey(table)) {
                Pair<String, SupportedDatabases> profileInfo = getNameAndTypeOfDatabase();
                try {
                    List<List<String>> result = execQuery(table.generateQuery(Queries.GET_COLUMN_NAMES_AND_TYPES,
                            profileInfo.getValue(), profileInfo.getKey()));
                    List<Pair<String, Class<?>>> listOfColumns = result.stream()
                            .skip(1) //Skip headings
                            .map(list -> {
                                return profileInfo.getValue()
                                        .getType(list.get(1))
                                        .map(ct -> new Pair<String, Class<?>>(list.get(0), ct));
                            })
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    /*
                     * NOTE Use "if containsKey(...)" instead of putIfAbsent(...) since it is lacking lazy evaluation
                     * for the second argument.
                     */
                    EXISTING_COLUMNS_CACHE.put(table, listOfColumns);
                } catch (SQLException ex) {
                    Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Checks whether the given table of the configured database contains a specific column and the column is
     * accessible.
     *
     * @param table The name of the table to search for the column.
     * @param columnName The column name to search for.
     * @return {@code true} only if the given table contains the given column and it is accessible.
     */
    public boolean columnExists(Tables table, String columnName) {
        checkExistingColumnsCache(table);
        //TODO Think about ignoring small/capital letters in column names
        return EXISTING_COLUMNS_CACHE.get(table).stream()
                .map(Pair::getKey)
                .filter(c -> c.equalsIgnoreCase(columnName))
                .findAny()
                .isPresent();
    }

    /**
     * Checks whether the given table of the configured database contains a specific column. The column must be
     * associated with {@code table} and has to be accessible.
     *
     * @param table The table to search the column for.
     * @param column The column for whose {@link Columns#getRealColumnName()} to search for.
     * @return {@code true} only if the given table contains the given column.
     */
    public boolean columnExists(Tables table, Columns<?> column) {
        return columnExists(table, column.getRealColumnName());
    }

    /**
     * Returns a {@link List} of all existing columns (not only that ones declared in the scheme) and their
     * <strong>actual</strong> types in the database which may defer from the one declared in the scheme of the table.
     *
     * @param table The table to get the columns for.
     * @return A {@link List} of all existing columns (not only that ones declared in the scheme).
     * @see Tables#getAllColumns()
     */
    public List<Pair<String, Class<?>>> getAllColumns(Tables table) {
        checkExistingColumnsCache(table);
        return EXISTING_COLUMNS_CACHE.get(table);
    }
}
