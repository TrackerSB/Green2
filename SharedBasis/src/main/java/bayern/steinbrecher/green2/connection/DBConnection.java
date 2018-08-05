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

import bayern.steinbrecher.green2.connection.scheme.ColumnPattern;
import bayern.steinbrecher.green2.connection.scheme.Queries;
import bayern.steinbrecher.green2.connection.scheme.RegexColumnPattern;
import bayern.steinbrecher.green2.connection.scheme.SimpleColumnPattern;
import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import bayern.steinbrecher.green2.connection.scheme.Tables;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.utility.DialogUtility;
import bayern.steinbrecher.green2.utility.PopulatingMap;
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

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    /**
     * Caches the names of all really existing columns on every supported table in {@link Tables}. The cache is
     * refreshed whenever the currently loaded profile changes.
     */
    private final Map<Tables<?, ?>, List<Pair<String, Class<?>>>> columnsCache = new PopulatingMap<>(table -> {
        if (table == null) {
            throw new IllegalArgumentException("Can not generate query controls for table null.");
        }
        List<Pair<String, Class<?>>> entry;
        Pair<String, SupportedDatabases> profileInfo = getNameAndTypeOfDatabase();
        try {
            List<List<String>> result = execQuery(table.generateQuery(Queries.GET_COLUMN_NAMES_AND_TYPES,
                    profileInfo.getValue(), profileInfo.getKey()));
            entry = result.stream()
                    .skip(1) //Skip headings
                    .map(list -> {
                        return profileInfo.getValue()
                                .getType(list.get(1))
                                .map(ct -> new Pair<String, Class<?>>(list.get(0), ct));
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
    private final List<String> tablesCache = new ArrayList<>();
    //TODO Any solution to decouple Profile from DBConnection?
    private final ObjectProperty<Optional<Pair<String, SupportedDatabases>>> nameAndTypeOfDbmsCache
            = new SimpleObjectProperty<>(Optional.empty());

    /**
     * Creates a connection using the currently loaded profile.
     */
    public DBConnection() {
        EnvironmentHandler.loadedProfileProperty().addListener(change -> {
            columnsCache.clear();
            tablesCache.clear();
            nameAndTypeOfDbmsCache.set(Optional.empty());
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

    private Pair<String, SupportedDatabases> getNameAndTypeOfDatabase() {
        synchronized (nameAndTypeOfDbmsCache) {
            if (!nameAndTypeOfDbmsCache.get().isPresent()) {
                Profile profile = EnvironmentHandler.getProfile();
                nameAndTypeOfDbmsCache.set(Optional.of(
                        new Pair<>(profile.get(ProfileSettings.DATABASE_NAME), profile.get(ProfileSettings.DBMS))));
            }
        }
        return nameAndTypeOfDbmsCache.get().get();
    }

    /**
     * Checks whether the given table exists. It DOES NOT check whether it has all needed columns and is configured
     * right.
     *
     * @param table The table to search for.
     * @return {@code true} only if the given table exist.
     */
    public boolean tableExists(Tables<?, ?> table) {
        synchronized (tablesCache) {
            if (tablesCache.isEmpty()) {
                Pair<String, SupportedDatabases> profileInfo = getNameAndTypeOfDatabase();
                List<List<String>> result;
                try {
                    result = execQuery(
                            table.generateQuery(Queries.GET_TABLE_NAMES, profileInfo.getValue(), profileInfo.getKey()));
                    tablesCache.addAll(result.stream()
                            //Skip column name
                            .skip(1)
                            .map(list -> list.get(0).toLowerCase(Locale.ROOT))
                            .collect(Collectors.toList()));
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
        return tablesCache.contains(table.getRealTableName().toLowerCase(Locale.ROOT));
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

    //TODO Is this method still needed?
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
     * @see #generateSearchQuery(bayern.steinbrecher.green2.connection.scheme.Tables, java.util.Collection)
     */
    //FIXME The method does not tell which columns and condtions were excluded.
    public Optional<String> generateSearchQuery(Tables<?, ?> table, Collection<String> columnsToSelect,
            Collection<String> conditions) {
        throwIfInvalid();
        List<String> existingColumns;
        List<String> notExistingColumns = new ArrayList<>();
        if (columnsToSelect.isEmpty()) {
            existingColumns = columnsCache.get(table).stream()
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

        Optional<String> searchQuery;
        if (existingColumns.isEmpty()) {
            LOGGER.log(Level.WARNING, "Generating search query without selecting any existing column.");
            searchQuery = Optional.empty();
        } else {
            SupportedDatabases dbms = getNameAndTypeOfDatabase().getValue();
            StringBuilder sqlString = new StringBuilder("SELECT ")
                    .append(existingColumns.stream()
                            .map(dbms::quoteIdentifier)
                            .collect(Collectors.joining(", "))
                            + " FROM " + dbms.quoteIdentifier(table.getRealTableName()));
            /*
             * NOTE condtions.stream() would work on empty condtions list too, but this if spares the computation of
             * nonExistingColumnsPattern.
             */
            if (!conditions.isEmpty()) {
                List<Pattern> notExistingColumnsPattern = notExistingColumns.stream()
                        //Used regex: (?:^|.*\W)columnName(?:\W.*|$)
                        //Tested at: http://www.regexplanet.com/advanced/java/index.html
                        .map(cn -> Pattern.compile("(?:^|.*\\\\W)" + cn + "(?:\\\\W.*|$)", Pattern.CASE_INSENSITIVE))
                        .collect(Collectors.toList());
                String conditionString = conditions.stream()
                        .filter(c -> notExistingColumnsPattern.stream().noneMatch(p -> p.matcher(c).matches()))
                        .collect(Collectors.joining(" AND "));
                if (!conditionString.isEmpty()) {
                    sqlString.append(" WHERE ")
                            .append(conditionString);
                }
            }
            searchQuery = Optional.of(sqlString.toString());
        }
        return searchQuery;
    }

    /**
     * Returns a {@link String} containing a statement with SELECT and FROM but without WHERE, with no semicolon and no
     * trailing space at the end.The select statement contains only columns existing and associated with {@code table}.
     *
     * @param <U> The type of an entry of the table.
     * @param table The table to select from.
     * @param patternsToSelect The columns to select when they exist. If it is empty all available columns are queried.
     * @return The statement selecting all existing columns of {@code patternsToSelect}. Returns
     * {@link Optional#empty()} if {@code patternsToSelect} contains no column which exists in the scheme accessible
     * through {@code connection}.
     * @see #generateSearchQuery(bayern.steinbrecher.green2.connection.scheme.Tables, java.util.Collection,
     * java.util.Collection)
     */
    public <U> Optional<String> generateSearchQuery(Tables<?, U> table,
            Collection<ColumnPattern<?, U>> patternsToSelect) {
        List<String> columnNamesToSelect = patternsToSelect.stream()
                .flatMap(pattern -> {
                    Set<String> columnNames;
                    if (pattern instanceof SimpleColumnPattern) {
                        columnNames = Set.of(((SimpleColumnPattern<?, U>) pattern).getRealColumnName());
                    } else if (pattern instanceof RegexColumnPattern) {
                        columnNames = getAllColumns(table)
                                .stream()
                                .map(Pair::getKey)
                                .filter(existingColumn -> pattern.matches(existingColumn))
                                .collect(Collectors.toSet());
                    } else {
                        LOGGER.log(Level.WARNING, "Can''t handle column patterns of type {0}.",
                                pattern == null ? "null" : pattern.getClass());
                        columnNames = Set.of();
                    }
                    return columnNames.stream();
                })
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
    public Optional<Map<Tables<?, ?>, List<SimpleColumnPattern<?, ?>>>> getMissingColumns() {
        Map<Tables<?, ?>, List<SimpleColumnPattern<?, ?>>> missingColumns = new HashMap<>();
        for (Tables<?, ?> table : Tables.values()) {
            List<SimpleColumnPattern<?, ?>> currentMissingColumns = table.getRequiredColumns()
                    .stream()
                    .filter(scp -> !columnExists(table, scp))
                    .collect(Collectors.toList());
            if (!currentMissingColumns.isEmpty()) {
                missingColumns.put(table, currentMissingColumns);
            }
        }
        return Optional.ofNullable(missingColumns.isEmpty() ? null : missingColumns);
    }

    /**
     * Creates a {@link String} out of the result of {@link #getMissingColumns()}.
     *
     * @param missingColumns The required columns missing.
     * @return
     */
    private static String generateMissingColumnsString(
            Optional<Map<Tables<?, ?>, List<SimpleColumnPattern<?, ?>>>> missingColumns) {
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
            return Tables.MEMBER.generateRepresentations(
                    execQuery(generateSearchQuery(Tables.MEMBER, Tables.MEMBER.getAllColumns()).get()));
        } catch (SQLException ex) {
            throw new Error("Hardcoded SQL-Code invalid", ex); //NOPMD - Indicates bug in hardcoded SQL.
        }
    }

    /**
     * Queries the nickname table of the specified connection.
     *
     * @return A map from first names to nicknames.
     */
    public Map<String, String> getAllNicknames() {
        try {
            return Tables.NICKNAMES.generateRepresentations(
                    execQuery(generateSearchQuery(Tables.NICKNAMES, Tables.NICKNAMES.getAllColumns()).get()));
        } catch (SQLException ex) {
            throw new Error("Hardcoded SQL-Code invalid", ex); //NOPMD - Indicates bug in hardcoded SQL.
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
    public boolean columnExists(Tables<?, ?> table, String columnName) {
        return columnsCache.get(table).stream()
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
     * @param column The column for whose {@link SimpleColumnPattern#getRealColumnName()} to search for.
     * @return {@code true} only if the given table contains the given column.
     */
    public boolean columnExists(Tables<?, ?> table, SimpleColumnPattern<?, ?> column) {
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
    public List<Pair<String, Class<?>>> getAllColumns(Tables<?, ?> table) {
        return columnsCache.get(table);
    }

    /**
     * Returns all
     *
     * @return
     */
    public Set<Tables<?, ?>> getAllTables() {
        return columnsCache.keySet();
    }
}
