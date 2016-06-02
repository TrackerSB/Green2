package bayern.steinbrecher.gruen2.connection;

import bayern.steinbrecher.gruen2.menu.Menu;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a database connection.
 *
 * @author Stefan Huber
 */
public abstract class DBConnection implements AutoCloseable {

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
            execQuery(
                    "SELECT COUNT(*) FROM Mitglieder, Spitznamen;");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void createTables() {
        try {
            execUpdate("CREATE TABLE Mitglieder ("
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
                    + "Beitrag FLOAT NOT NULL);");
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        try {
            execUpdate("CREATE TABLE Spitznamen ("
                    + "Name VARCHAR(255) PRIMARY KEY,"
                    + "Spitzname VARCHAR(255) NOT NULL);");
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName())
                    .log(Level.SEVERE, null, ex);
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
