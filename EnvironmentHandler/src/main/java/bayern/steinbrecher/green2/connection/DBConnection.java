/* 
 * Copyright (C) 2017 Stefan Huber
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

import bayern.steinbrecher.green2.data.ProfileSettings;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.generator.MemberGenerator;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.utility.DialogUtility;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Represents a database connection.
 *
 * @author Stefan Huber
 */
public abstract class DBConnection implements AutoCloseable {

    private static final Map<SupportedDatabase, Map<Query, String>> QUERIES = new HashMap<>();
    protected static final Property<SupportedDatabase> DATABASE = new SimpleObjectProperty<>();
    /**
     * Caches existing columns of tables. All column names are lowercase.
     */
    private final Map<Tables, List<String>> EXISTING_HEADINGS_CACHE = new HashMap<>();

    static {
        DATABASE.bind(Bindings.createObjectBinding(() -> {
            Profile profile = EnvironmentHandler.getProfile();
            return profile == null ? null : profile.getOrDefault(ProfileSettings.DBMS, null);
        }, EnvironmentHandler.loadedProfileProperty(), EnvironmentHandler.getProfile().getProperty(ProfileSettings.DBMS)));
    }

    static {
        Map<Query, String> mysql = new HashMap<>(Query.values().length);
        mysql.put(Query.CREATE_MEMBER_TABLE, "CREATE TABLE " + Tables.MEMBER.getRealTableName() + " ("
                + "Mitgliedsnummer INTEGER PRIMARY KEY,"
                + "Titel VARCHAR(255) NOT NULL,"
                + "Vorname VARCHAR(255) NOT NULL,"
                + "Nachname VARCHAR(255) NOT NULL,"
                + "IstAktiv BOOLEAN NOT NULL,"
                + "IstMaennlich BOOLEAN NOT NULL,"
                + "Geburtstag DATE NOT NULL,"
                + "Strasse VARCHAR(255) NOT NULL,"
                + "Hausnummer VARCHAR(255) NOT NULL,"
                + "PLZ VARCHAR(255) NOT NULL,"
                + "Ort VARCHAR(255) NOT NULL,"
                //+ "AusgetretenSeit DATE DEFAULT NULL," //FIXME Remove AusgetretenSeit when the column is banished.
                + "IBAN VARCHAR(255) NOT NULL,"
                + "BIC VARCHAR(255) NOT NULL,"
                + "MandatErstellt DATE NOT NULL,"
                + "KontoinhaberVorname VARCHAR(255) NOT NULL,"
                + "KontoinhaberNachname VARCHAR(255) NOT NULL,"
                + "IstBeitragsfrei BOOLEAN NOT NULL DEFAULT '0',"
                + "Beitrag FLOAT NOT NULL);");
        mysql.put(Query.CREATE_NICKNAMES_TABLE, "CREATE TABLE " + Tables.NICKNAMES.getRealTableName() + " ("
                + "Name VARCHAR(255) PRIMARY KEY,"
                + "Spitzname VARCHAR(255) NOT NULL);");
        mysql.put(Query.TABLE_EXISTS, "SELECT count(*) FROM information_schema.tables "
                + "WHERE table_schema=\"{0}\" AND (table_name=\"{1}\");");
        QUERIES.put(SupportedDatabase.MY_SQL, mysql);
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
    public static Map<Columns, Integer> generateColumnMapping(Tables table, List<String> headings) {
        Map<Columns, Integer> columnsMapping = new HashMap<>();
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

    private String getQuery(Query query) {
        if (DATABASE.getValue() == null) {
            throw new IllegalStateException("The supported databases are not set.");
        } else {
            return QUERIES.get(DATABASE.getValue()).get(query);
        }
    }

    /**
     * Checks whether the given table exists. It DOES NOT check whether it has all needed columns and is configured
     * right.
     *
     * @param table The table to search for.
     * @return {@code true} only if the given table exist.
     */
    public boolean tableExists(Tables table) {
        try {
            Profile profile = EnvironmentHandler.getProfile();
            if (profile == null) {
                throw new IllegalStateException("Can't check. Currently no profile is loaded.");
            } else {
                String databaseName = profile.getOrDefault(ProfileSettings.DATABASE_NAME, "database");
                String query
                        = MessageFormat.format(getQuery(Query.TABLE_EXISTS), databaseName, table.getRealTableName());
                return Integer.parseInt(execQuery(query).get(1).get(0)) >= Tables.values().length;
            }
            //FIXME When permissions to read are missing, also a SQLException is thrown.
        } catch (SQLException ex) {
            return false;
        }
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
                .filter(this::tableExists)
                .collect(Collectors.toList());
        boolean tablesAreMissing = !missingTables.isEmpty();
        boolean tablesCreated = false;
        if (tablesAreMissing) {
            StringJoiner message = new StringJoiner("\n").add(EnvironmentHandler.getResourceValue("tablesMissing"));
            missingTables.forEach(table -> message.add(table.getRealTableName()));
            Alert creationConfirmation = DialogUtility.createAlert(
                    Alert.AlertType.CONFIRMATION, message.toString(), ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> resultButton = creationConfirmation.showAndWait();
            if (resultButton.isPresent() && resultButton.get() == ButtonType.YES) {
                for (Tables table : missingTables) {
                    try {
                        switch (table) {
                            case MEMBER:
                                execUpdate(getQuery(Query.CREATE_MEMBER_TABLE));
                                break;
                            case NICKNAMES:
                                execUpdate(getQuery(Query.CREATE_NICKNAMES_TABLE));
                                break;
                            default:
                                throw new SchemeCreationException(
                                        "There is no sql statement for creating \"" + table.getRealTableName() + "\".");
                        }
                    } catch (SQLException ex) {
                        throw new SchemeCreationException("Could not create one or more database tables", ex);
                    }
                }
                tablesCreated = true;
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
    public Optional<Map<Tables, List<Columns>>> getMissingColumns() {
        Map<Tables, List<Columns>> missingColumns = new HashMap<>();
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
    public List<Member> getAllMember() {
        try {
            return MemberGenerator.generateMemberList(execQuery(
                    Tables.MEMBER.generateQuery(this, Tables.MEMBER.getAllColumnsAsArray()).get()
                            //FIXME Remove that concatination when the column AusgetretenSeit has vanished.
                            .concat(columnExists(Tables.MEMBER, Columns.RESIGN_DATE)
                                    ? " WHERE AusgetretenSeit='0000-00-00' OR AusgetretenSeit IS NULL;" : "")));
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
                    = execQuery(Tables.NICKNAMES.generateQuery(this, Tables.NICKNAMES.getAllColumnsAsArray()).get());

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
    public boolean columnExists(Tables table, Columns column) {
        try {
            synchronized (EXISTING_HEADINGS_CACHE) {
                if (!EXISTING_HEADINGS_CACHE.containsKey(table)) {
                    EXISTING_HEADINGS_CACHE
                            /*
                             * FIXME When the database is empty it may happen that the result contains nothing.
                             * Not even the column names. (See also JavaDoc)
                             */
                            /*
                             * NOTE Don´t use putIfAbsent(...). If you do, execQuery(...) will always be evaluated
                             * because of missing lazy evaluation.
                             */
                            .put(table, execQuery("SELECT * FROM " + table.getRealTableName() + " LIMIT 1;")
                                    .get(0)
                                    .stream()
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toList()));
                }
            }
            return EXISTING_HEADINGS_CACHE.get(table).stream()
                    .anyMatch(s -> s.equalsIgnoreCase(column.getRealColumnName()));
            //FIXME Try not to use SQLException for checking whether the table exists.
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private enum Query {
        /**
         * Checks whether a given table exists.<br />
         * Variables:<br />
         * 0: database name<br />
         * 1: name of the table
         */
        TABLE_EXISTS(true),
        /**
         * Creates the database for member with all columns.
         */
        CREATE_MEMBER_TABLE(false),
        /**
         * Creates the database for nicknames with all columns.
         */
        CREATE_NICKNAMES_TABLE(false);

        private final boolean containsVariables;

        private Query(boolean containsVariables) {
            this.containsVariables = containsVariables;
        }

        /**
         * Checks whether this query contains variables to be used as {@link PreparedStatement}.
         *
         * @return {@code true} only if this query contains variables.
         */
        public boolean containsVariables() {
            return containsVariables;
        }
    }

    /**
     * This enum lists all supported databases like MySQL.
     */
    public enum SupportedDatabase {
        MY_SQL("My SQL", 3306);

        private final String displayName;
        private final int defaultPort;

        private SupportedDatabase(String displayName, int defaultPort) {
            this.displayName = displayName;
            this.defaultPort = defaultPort;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return displayName;
        }

        /**
         * Returns the default port of the dbms.
         *
         * @return The default port of the dbms.
         */
        public int getDefaultPort() {
            return defaultPort;
        }
    }

    /**
     * This enum lists all tables needed for Green2.
     */
    public enum Tables {
        //FIXME JDK9 allows <> with anonymous inner classes.
        MEMBER("Mitglieder", new HashMap<Columns, Boolean>() {
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
                put(Columns.RESIGN_DATE, false); //FIXME Remove AusgetretenSeit at some point in the future.
            }
        }),
        //FIXME JDK9 allows <> with anonymous inner classes.
        NICKNAMES("Spitznamen", new HashMap<Columns, Boolean>() {
            {
                put(Columns.NAME, true);
                put(Columns.NICKNAME, true);
            }
        });

        private final Map<Columns, Boolean> columns;
        private final String realTableName;

        /**
         * Creates a representation of a scheme of a table.
         *
         * @param realTableName The name of the table in a database.
         * @param columns A map containing the columns of this table and whether they are required. {@code true} means
         * required; {@code false} means optional.
         */
        private Tables(String realTableName, Map<Columns, Boolean> columns) {
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
        public boolean contains(Columns column) {
            return columns.containsKey(column);
        }

        /**
         * Checks whether this table exists and has all required columns when using the given connection.
         *
         * @param connection The connection to use for checking.
         * @return {@code true} only if the table accessible using {@code connection} exists and has all required
         * columns.
         */
        public boolean isValid(DBConnection connection) {
            return !getMissingColumns(connection).isPresent();
        }

        /**
         * Returns all required but missing or unaccessible columns of this table using the given connection.
         *
         * @param connection The connection to use.
         * @return {@link Optional#empty()} if no required column is missing or unaccessible. Otherwise an
         * {@link Optional} containing a list of these columns.
         */
        public Optional<List<Columns>> getMissingColumns(DBConnection connection) {
            List<Columns> missingColumns = columns.entrySet().stream()
                    .filter(Entry::getValue)
                    .map(Entry::getKey)
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
         * Returns a {@link String} containing a statement with SELECT and FROM but without WHERE, with no semicolon and
         * no trailing space at the end. The select statement contains only columns existing when using
         * {@code connection}.
         *
         * @param connection The connection to use.
         * @param columnsToSelect The columns to select when they exist.
         * @return The statement selecting all existing columns of {@code columnsToSelect}. Returns
         * {@link Optional#empty()} if {@code columnsToSelect} contains no column which exists in the scheme accessible
         * through {@code connection}.
         */
        public Optional<String> generateQuery(DBConnection connection, Columns... columnsToSelect) {
            throwIfInvalid(connection);
            List<Columns> existingColumns = Arrays.stream(columnsToSelect)
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
        public Set<Columns> getAllColumns() {
            return columns.keySet();
        }

        private Columns[] getAllColumnsAsArray() {
            return columns.keySet().toArray(new Columns[0]);
        }
    }

    /**
     * This enum lists all columns which a table can have.
     */
    //FIXME Waiting for JDK 9 making it generic.
    public enum Columns {
        MEMBERSHIPNUMBER("Mitgliedsnummer"), IS_ACTIVE("IstAktiv"), PRENAME("Vorname"), LASTNAME("Nachname"),
        TITLE("Titel"), IS_MALE("IstMaennlich"), BIRTHDAY("Geburtstag"), STREET("Strasse"), HOUSENUMBER("Hausnummer"),
        CITY_CODE("PLZ"), CITY("Ort"), IS_CONTRIBUTIONFREE("IstBeitragsfrei"), IBAN("Iban"), BIC("Bic"),
        ACCOUNTHOLDER_PRENAME("KontoinhaberVorname"), ACCOUNTHOLDER_LASTNAME("KontoinhaberNachname"),
        MANDAT_SIGNED("MandatErstellt"), CONTRIBUTION("Beitrag"), NAME("Name"), NICKNAME("Spitzname"),
        RESIGN_DATE("AusgetretenSeit"); //FIXME Remove AusgetretenSeit at some point in the future.

        private final String realColumnName;

        private Columns(String realColumnName) {
            this.realColumnName = realColumnName;
        }

        /**
         * Returns the name of this column in the database scheme.
         *
         * @return The name of this column in the database scheme.
         */
        public String getRealColumnName() {
            return realColumnName;
        }
    }
}
