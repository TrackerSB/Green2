/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.connection;

import bayern.steinbrecher.gruen2.exception.SchemeCreationException;
import bayern.steinbrecher.gruen2.generator.MemberGenerator;
import bayern.steinbrecher.gruen2.menu.Menu;
import bayern.steinbrecher.gruen2.people.Member;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    private static final String ALL_COLUMN_LABELS_MEMBER
            = COLUMN_LABELS_MEMBER.stream().collect(Collectors.joining(","));
    private static final String EXIST_TEST
            = "SELECT 1 FROM Mitglieder, Spitznamen;";
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
    private static final String QUERY_ALL_MEMBER
            = "SELECT " + ALL_COLUMN_LABELS_MEMBER
            + " FROM Mitglieder "
            + "WHERE AusgetretenSeit='0000-00-00';";
    private static final String QUERY_ALL_NICKNAMES
            = "SELECT Name, Spitzname FROM Spitznamen;";
    private static final String QUERY_ALL_CONTRIBUTIONS
            = "SELECT Mitgliedsnummer, Beitrag FROM Mitglieder;";

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

    /**
     * Checks whether tables &bdquo;Mitglieder&ldquo; and
     * &bdquo;Spitznamen&ldquo; exist. It DOES NOT check whether they have all
     * needed columns and are configured right.
     *
     * @return {@code true} only if both tables exist.
     */
    public boolean tablesExist() {
        try {
            execQuery(EXIST_TEST);
            return true;
            //FIXME Avoid using SQLException as control flow.
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Creates table &bdquo;Mitglieder&ldquo; and &bdquo;Spitznamen&ldquo; if
     * they not already exist.
     */
    public void createTablesIfNeeded() {
        if (!tablesExist()) {
            try {
                execUpdate(CREATE_MITGLIEDER);
                execUpdate(CREATE_SPITZNAMEN);
            } catch (SQLException ex) {
                throw new SchemeCreationException(
                        "Could not create database tables", ex);
            }
        }
    }

    /**
     * Returns a list of all member accessable with {@code dbc}. The list
     * contains all labels hold in {@code COLUMN_LABELS_MEMBER}.
     *
     * @return The list with the member.
     */
    public List<Member> getAllMember() {
        try {
            return MemberGenerator.generateMemberList(
                    execQuery(QUERY_ALL_MEMBER));
        } catch (SQLException ex) {
            throw new Error("Hardcoded SQL-Code invalid", ex);
        }
    }

    /**
     * Queries the nickname table of the specified connection.
     *
     * @return A map from prenames to nicknames.
     */
    public Map<String, String> getAllNicknames() {
        try {
            List<List<String>> queriedNicknames
                    = execQuery(QUERY_ALL_NICKNAMES);

            Map<String, String> mappedNicknames = new HashMap<>();
            queriedNicknames.parallelStream().skip(1).forEach(row -> {
                mappedNicknames.put(row.get(0), row.get(1));
            });
            return mappedNicknames;
        } catch (SQLException ex) {
            throw new Error("Hardcoded SQL-Code invalid", ex);
        }
    }

    /**
     * Checks whether the given table of the configured database contains a
     * specific column. You should NEVER call this function with parameters
     * provided by the user in order to prohibit SQL INJECTION.
     *
     * @param table The name of the table to search for the column.
     * @param column The column name to search for.
     * @return {@code true} only if the given table contains the given column.
     */
    public boolean checkColumn(String table, String column) {
        try {
            execQuery("SELECT " + column + " FROM " + table + ";");
            return true;
            //FIXME Avoid using SQLException as control flow.
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Reads the individual contributions of every member - if specified.
     *
     * @return A Optional containing the individual contributions or
     * {@code Optional.empty()} if inidividual contributions are not specified.
     */
    public Optional<Map<Integer, Double>> readIndividualContributions() {
        if (checkColumn("Mitglieder", "Beitrag")) {
            try {
                List<List<String>> result = execQuery(QUERY_ALL_CONTRIBUTIONS);
                Map<Integer, Double> contributions = new HashMap<>();
                result.parallelStream()
                        .skip(1)
                        .forEach(row -> {
                            contributions.put(Integer.parseInt(row.get(0)),
                                    Double.parseDouble(
                                            row.get(1).replaceAll(",", ".")));
                        });
                return Optional.of(contributions);
            } catch (SQLException ex) {
                Logger.getLogger(Menu.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        return Optional.empty();
    }
}
