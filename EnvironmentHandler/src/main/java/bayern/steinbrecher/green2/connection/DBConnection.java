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
import java.util.List;
import java.util.Map;
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

    static {
        DATABASE.bind(Bindings.createObjectBinding(() -> {
            Profile profile = EnvironmentHandler.getProfile();
            return profile == null ? null : profile.getOrDefault(ConfigKey.DBMS, null);
        }, EnvironmentHandler.loadedProfileProperty(), EnvironmentHandler.getProfile().getProperty(ConfigKey.DBMS)));
    }

    static {
        Map<Query, String> mysql = new HashMap<>(Query.values().length);
        mysql.put(Query.CREATE_MEMBER_TABLE, "CREATE TABLE Mitglieder ("
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
        mysql.put(Query.CREATE_NICKNAMES_TABLE, "CREATE TABLE Spitznamen ("
                + "Name VARCHAR(255) PRIMARY KEY,"
                + "Spitzname VARCHAR(255) NOT NULL);");
//FIXME Remove 0000-00-00 legacy check
        mysql.put(Query.QUERY_ALL_MEMBER, "SELECT " + Tables.MEMBER.getAllColumns()
                + " FROM Mitglieder "
                + "WHERE AusgetretenSeit='0000-00-00' OR AusgetretenSeit IS NULL;");
        mysql.put(Query.QUERY_ALL_NICKNAMES, "SELECT " + Tables.NICKNAMES.getAllColumns() + " FROM Spitznamen;");
        mysql.put(Query.TABLES_EXIST_TEST, "SELECT 1 FROM Mitglieder, Spitznamen;");
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
     * Returns a list of all member accessible with {@code dbc}. The list contains all labels hold in
     * {@link DBConnection#COLUMN_LABELS_MEMBER}.
     *
     * @return The list with the member.
     */
    public List<Member> getAllMember() {
        try {
            return MemberGenerator.generateMemberList(execQuery(getQuery(Query.QUERY_ALL_MEMBER)));
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
            List<List<String>> queriedNicknames = execQuery(getQuery(Query.QUERY_ALL_NICKNAMES));

            Map<String, String> mappedNicknames = new HashMap<>();
            queriedNicknames.parallelStream()
                    .skip(1)
                    .forEach(row -> mappedNicknames.put(row.get(0), row.get(1)));
            return mappedNicknames;
        } catch (SQLException ex) {
            throw new Error("Hardcoded SQL-Code invalid", ex);
        }
    }

    /**
     * Checks whether the given table of the configured database contains a specific column. You should NEVER call this
     * function with parameters provided by the user in order to prohibit SQL INJECTION.
     *
     * @param table The name of the table to search for the column.
     * @param column The column name to search for.
     * @return {@code true} only if the given table contains the given column.
     */
    public boolean columnExists(Tables table, Columns column) {
        try {
            List<String> headings = execQuery("SELECT * FROM " + table.getRealTableName() + " WHERE 0;").get(0);
            return headings.stream()
                    .map(String::toLowerCase)
                    .anyMatch(s -> s.equalsIgnoreCase(column.getRealColumnName()));
            //FIXME Try not to use SQLException for controlling the flow.
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private String getQuery(Query query) {
        if (DATABASE.getValue() == null) {
            throw new IllegalStateException("The type of the database is not set.");
        } else {
            return QUERIES.get(DATABASE.getValue()).get(query);
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
         * A query for returning all informations about member.
         */
        QUERY_ALL_MEMBER(false),
        /**
         * A query for returning all informations about nicknames.
         */
        QUERY_ALL_NICKNAMES(false),
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
        MEMBER("Mitglieder", Columns.MEMBERSHIPNUMBER, Columns.IS_ACTIVE, Columns.PRENAME, Columns.LASTNAME,
                Columns.TITLE, Columns.IS_MALE, Columns.BIRTHDAY, Columns.STREET, Columns.HOUSENUMBER,
                Columns.CITY_CODE, Columns.CITY, Columns.IS_CONTRIBUTIONFREE, Columns.IBAN, Columns.BIC,
                Columns.ACCOUNTHOLDER_PRENAME, Columns.ACCOUNTHOLDER_LASTNAME, Columns.MANDAT_SIGNED,
                Columns.CONTRIBUTION),
        NICKNAMES("Spitznamen", Columns.NAME, Columns.NICKNAME);

        private final List<Columns> columns;
        private final String allColumns;
        private final String realTableName;
        private final Map<Columns, Boolean> requiredMap;

        private Tables(String realTableName, Map<Columns, Boolean> requiredMap, Columns... columns) {
            this.realTableName = realTableName;
            this.requiredMap = requiredMap;
            this.columns = Arrays.asList(columns);
            allColumns = this.columns.stream()
                    .map(Columns::getRealColumnName)
                    .collect(Collectors.joining(","));
        }

        private Tables(String realTableName, Columns... columns) {
            this(realTableName, null, columns);
        }

        /**
         * Checks whether the scheme of this table contains the given column. NOTE: This does not confirm that the table
         * contains this column. It only states that the scheme can have such a column.
         *
         * @param column The column to check.
         * @return {@code true} only if this table contains {@code column}.
         */
        public boolean contains(Columns column) {
            return columns.contains(column);
        }

        /**
         * Returns a {@link String} listing all columns of this table separated by a column.
         *
         * @return A {@link String} listing all columns of this table separated by a column.
         */
        public String getAllColumns() {
            return allColumns;
        }

        /**
         * Returns the name of the table in the database scheme.
         *
         * @return The name of the table in the database scheme.
         */
        public String getRealTableName() {
            return realTableName;
        }
    }

    /**
     * This enum lists all columns which a table can have.
     */
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
