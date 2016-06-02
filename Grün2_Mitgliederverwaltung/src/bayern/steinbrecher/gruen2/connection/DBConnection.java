package bayern.steinbrecher.gruen2.connection;

import bayern.steinbrecher.gruen2.menu.Menu;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a database connection.
 *
 * @author Stefan Huber
 */
public abstract class DBConnection implements AutoCloseable {

    /**
     * A list containing all names of needed columns for queries in the member
     * table.
     */
    public static final List<String> COLUMN_LABELS_MEMBER = new ArrayList<>(
            Arrays.asList("mitgliedsnummer", "vorname", "nachname", "titel",
                    "istmaennlich", "istaktiv", "geburtstag", "strasse",
                    "hausnummer", "plz", "ort", "istbeitragsfrei", "iban",
                    "bic", "kontoinhabervorname", "kontoinhabernachname",
                    "mandaterstellt"));
    private static final String ALL_COLUMN_LABELS_MEMBER;
    private static final String EXIST_TEST
            = "SELECT COUNT(*) FROM Mitglieder, Spitznamen;";
    private static final String CREATE_MITGLIEDER = "CREATE TABLE Mitglieder ("
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
            + "AusgetretenSeit DATE NOT NULL DEFAULT '0000-00-00',"
            + "IBAN VARCHAR(255) NOT NULL,"
            + "BIC VARCHAR(255) NOT NULL,"
            + "MandatErstellt DATE NOT NULL,"
            + "KontoinhaberVorname VARCHAR(255) NOT NULL,"
            + "KontoinhaberNachname VARCHAR(255) NOT NULL,"
            + "istBeitragsfrei BOOLEAN NOT NULL DEFAULT '0',"
            + "Beitrag FLOAT NOT NULL);";
    private static final String CREATE_SPITZNAMEN = "CREATE TABLE Spitznamen ("
            + "Name VARCHAR(255) PRIMARY KEY,"
            + "Spitzname VARCHAR(255) NOT NULL);";
    private static final String QUERY_ALL_MEMBER;
    private static final String QUERY_ALL_NICKNAMES
            = "SELECT * FROM Spitznamen;";

    static {
        String all = COLUMN_LABELS_MEMBER.stream()
                .map(s -> s + ",")
                .reduce("", String::concat);
        ALL_COLUMN_LABELS_MEMBER = all.substring(0, all.length() - 1);
        QUERY_ALL_MEMBER
                = "SELECT " + ALL_COLUMN_LABELS_MEMBER
                + " FROM Mitglieder "
                + "WHERE AusgetretenSeit='0000-00-00';";
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
     * @return Table containing the results AND the headings of each column.
     * First dimension rows; second columns.
     * @throws SQLException Thrown if the sql code is invalid.
     */
    public abstract List<List<String>> execQuery(String sqlCode)
            throws SQLException;

    /**
     * Executes a command like INSERT INTO, UPDATE or CREATE.
     *
     * @param sqlCode The sql code to execute.
     * @throws SQLException Thrown if the sql code is invalid.
     */
    public abstract void execUpdate(String sqlCode) throws SQLException;

    public boolean tablesExist() {
        try {
            execQuery(EXIST_TEST);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void createTables() {
        try {
            execUpdate(CREATE_MITGLIEDER);
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        try {
            execUpdate(CREATE_SPITZNAMEN);
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public List<List<String>> getAllMember() {
        try {
            return execQuery(QUERY_ALL_MEMBER);
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new Error("Hardcoded SQL-Code invalid");
        }
    }

    /**
     * Queries the nickname table of the specified connection.
     *
     * @param dbc The connection to query through.
     * @return A map from prenames to nicknames.
     */
    public Map<String, String> getAllNicknames() {
        try {
            List<List<String>> queriedNicknames
                    = execQuery(QUERY_ALL_NICKNAMES);
            int nameIndex = queriedNicknames.get(0).indexOf("Name");
            int nicknameIndex = queriedNicknames.get(0).indexOf("Spitzname");

            Map<String, String> mappedNicknames = new HashMap<>();
            queriedNicknames.parallelStream().skip(1).forEach(row -> {
                mappedNicknames.put(row.get(nameIndex), row.get(nicknameIndex));
            });
            return mappedNicknames;
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new Error("Hardcoded SQL-Code invalid");
        }
    }

    /**
     * Checks whether the given table of the configured database contains a
     * specific column.
     *
     * @param table The name of the table to search for the column.
     * @param column The column name to search for.
     * @return {@code true} only if the given table contains the given column.
     */
    public boolean checkColumn(String table, String column) {
        //FIXME Try to find other solution not using SQLException.
        try {
            execQuery("SELECT " + column + " FROM " + table + ";");
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}
