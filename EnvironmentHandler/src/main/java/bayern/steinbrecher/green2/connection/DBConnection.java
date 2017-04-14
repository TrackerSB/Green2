/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.connection;

import bayern.steinbrecher.green2.data.ConfigKey;
import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.generator.MemberGenerator;
import bayern.steinbrecher.green2.people.Member;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

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
            return profile == null ? null : profile.getOrDefault(ConfigKey.DBMS, null);
        }, EnvironmentHandler.loadedProfileProperty(), EnvironmentHandler.getProfile().getProperty(ConfigKey.DBMS)));
    }

    static {
        Map<Query, String> mysql = new HashMap<>(Query.values().length);
        mysql.put(Query.CREATE_MEMBER_TABLE, "CREATE TABLE " + Tables.MEMBER.getRealTableName() + " ("
                + "Mitgliedsnummer INTEGER PRIMARY KEY,"
                + "Titel VARCHAR(255) NOT NULL,"
                + "Vorname VARCHAR(255) NOT NULL,"
                + "Nachname VARCHAR(255) NOT NULL,"
                + "istAktiv BOOLEAN NOT NULL,"
                + "istMaennlich BOOLEAN NOT NULL,"
                + "Geburtstag DATE NOT NULL,"
                + "Strasse VARCHAR(255) NOT NULL,"
                + "Hausnummer VARCHAR(255) NOT NULL,"
                + "PLZ VARCHAR(255) NOT NULL,"
                + "Ort VARCHAR(255) NOT NULL,"
                + "AusgetretenSeit DATE DEFAULT NULL,"
                + "IBAN VARCHAR(255) NOT NULL,"
                + "BIC VARCHAR(255) NOT NULL,"
                + "MandatErstellt DATE NOT NULL,"
                + "KontoinhaberVorname VARCHAR(255) NOT NULL,"
                + "KontoinhaberNachname VARCHAR(255) NOT NULL,"
                + "istBeitragsfrei BOOLEAN NOT NULL DEFAULT '0',"
                + "Beitrag FLOAT NOT NULL);");
        mysql.put(Query.CREATE_NICKNAMES_TABLE, "CREATE TABLE " + Tables.NICKNAMES.getRealTableName() + " ("
                + "Name VARCHAR(255) PRIMARY KEY,"
                + "Spitzname VARCHAR(255) NOT NULL);");
//FIXME Remove 0000-00-00 legacy check at some point in the future
        mysql.put(Query.TABLES_EXIST_TEST, "SELECT 1 FROM " + Arrays.stream(Tables.values())
                .map(Tables::getRealTableName).collect(Collectors.joining(",")) + ";");
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
            throw new IllegalStateException("The type of the database is not set.");
        } else {
            return QUERIES.get(DATABASE.getValue()).get(query);
        }
    }

    /**
     * Checks whether tables &bdquo;Mitglieder&ldquo; and &bdquo;Spitznamen&ldquo; exist. It DOES NOT check whether they
     * have all needed columns and are configured right.
     *
     * @return {@code true} only if both tables exist.
     */
    public boolean tablesExist() {
        try {
            execQuery(getQuery(Query.TABLES_EXIST_TEST));
            return true;
            //FIXME Avoid using SQLException as control flow.
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Creates table &bdquo;Mitglieder&ldquo; and &bdquo;Spitznamen&ldquo; if they not already exist.
     *
     * @throws SchemeCreationException Thrown, only if there are tables missing in the database and they could not be
     * created.
     */
    public void createTablesIfNeeded() throws SchemeCreationException {
        if (!tablesExist()) {
            try {
                execUpdate(getQuery(Query.CREATE_MEMBER_TABLE));
                execUpdate(getQuery(Query.CREATE_NICKNAMES_TABLE));
            } catch (SQLException ex) {
                throw new SchemeCreationException("Could not create database tables", ex);
            }
        }
    }

    /**
     * Returns a list of all member accessible with this connection. The list contains all columns queried by
     * {@link Query#QUERY_ALL_MEMBER}.
     *
     * @return The list with the member.
     */
    public List<Member> getAllMember() {
        try {
            return MemberGenerator.generateMemberList(execQuery(
                    Tables.MEMBER.generateQuery(this, Tables.MEMBER.getAllColumnsAsArray()).get()
                            //FIXME Remove that concatination when the column AusgetretenSeit has vanished.
                            .concat(" WHERE AusgetretenSeit='0000-00-00' OR AusgetretenSeit IS NULL;")));
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
     * function with parameters provided by the user in order to prohibit SQL INJECTION. NOTE: For this function to work
     * for sure the table should have at least one row. When having no rows the database may return nothing not even the
     * headings.
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
                             * Not even the column names.
                             */
                            /*
                             * NOTE Don´t use putIfAbsent(...). If you do execQuery(...) will always be evaluated
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
         * Checks whether all tables exist.
         */
        TABLES_EXIST_TEST(false),
        /**
         * Creates the database for member with all columns.
         */
        CREATE_MEMBER_TABLE(false),
        /**
         * Creates the database for nicknames with all columns.
         */
        CREATE_NICKNAMES_TABLE(false),
        /**
         * Checks whether the nickname table contains all columns needed. NOTE: It is not neccessary that all columns
         * have to be existent.
         */
        NICKNAME_TABLE_COMPLETE(false),
        /**
         * Checks whether the member table contains all columns needed. NOTE: It is not neccessary that all columns have
         * to be existent.
         */
        MEMBER_TABLE_COMPLETE(false);

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
        MY_SQL("My SQL");

        private final String displayName;

        private SupportedDatabase(String displayName) {
            this.displayName = displayName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * This enum lists all tables needed for Green2.
     */
    public enum Tables {
        MEMBER("Mitglieder", new HashSet<>(Arrays.asList(Columns.MEMBERSHIPNUMBER, Columns.PRENAME, Columns.LASTNAME,
                Columns.TITLE, Columns.IS_MALE, Columns.BIRTHDAY, Columns.STREET, Columns.HOUSENUMBER,
                Columns.CITY_CODE, Columns.CITY, Columns.IS_CONTRIBUTIONFREE, Columns.IBAN, Columns.BIC,
                Columns.ACCOUNTHOLDER_PRENAME, Columns.ACCOUNTHOLDER_LASTNAME, Columns.MANDAT_SIGNED)),
                new HashSet<>(Arrays.asList(Columns.CONTRIBUTION, Columns.IS_ACTIVE))),
        NICKNAMES("Spitznamen", new HashSet<>(Arrays.asList(Columns.NAME, Columns.NICKNAME)), new HashSet<>());

        private final Set<Columns> requiredColumns;
        private final Set<Columns> optionalColumns;
        private final Set<Columns> allColumns;
        private final String realTableName;

        private Tables(String realTableName, Set<Columns> requiredColumns, Set<Columns> optionalColumns) {
            if (requiredColumns.stream().anyMatch(c -> optionalColumns.contains(c))) {
                throw new IllegalArgumentException(
                        "Found a column which is required AND optional in table " + realTableName);
            }
            this.realTableName = realTableName;
            this.requiredColumns = requiredColumns;
            this.optionalColumns = optionalColumns;
            allColumns = new HashSet<>(requiredColumns);
            allColumns.addAll(optionalColumns);
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
            return allColumns.contains(column);
        }

        /**
         * Checks whether this table exists and has all required columns when using the given connection.
         *
         * @param connection The connection to use for checking.
         * @return {@code true} only if the table accessible using {@code connection} exists and has all required
         * columns.
         */
        public boolean isValid(DBConnection connection) {
            return requiredColumns.stream().allMatch(c -> connection.columnExists(this, c));
        }

        /**
         * Checks whether the given column is an optional column of this table.
         *
         * @param column The column to check.
         * @return {@code true} only if this column is an optional column of this table.
         */
        public boolean isOptional(Columns column) {
            if (contains(column)) {
                return optionalColumns.contains(column);
            } else {
                throw new IllegalArgumentException(column + " is no column of " + realTableName);
            }
        }

        /**
         * Returns a {@link String} containing a statement with select and from but without where and also without a
         * semicolon and no trailing space at the end. The select statement contains only columns existing when using
         * {@code connection}.
         *
         * @param connection The connection to use.
         * @param columnsToSelect The columns to select when they exist.
         * @return The statement selecting all existing columns of {@code columnsToSelect}. Returns
         * {@link Optional#empty()} if {@code columnsToSelect} contains no column which exists. NOTE: If
         * {@code columnsToSelect} contains at least one required column this method won´t return
         * {@link Optional#empty()} otherwise the table accessible using {@code connection} is not valid.
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
            return allColumns;
        }

        private Columns[] getAllColumnsAsArray() {
            return allColumns.toArray(new Columns[0]);
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
        MANDAT_SIGNED("MandatErstellt"), CONTRIBUTION("Beitrag"), NAME("Name"), NICKNAME("Spitzname");

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