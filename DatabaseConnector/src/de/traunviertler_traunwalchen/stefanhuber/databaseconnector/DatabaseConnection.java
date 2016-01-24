package de.traunviertler_traunwalchen.stefanhuber.databaseconnector;

import java.sql.SQLException;
import java.util.LinkedList;

/**
 * @author Stefan Huber
 */
public interface DatabaseConnection extends AutoCloseable {

    /**
     * Schließt die Verbindung. Diese Methode sollte immer aufgerufen werden, um
     * sicherzustellen, dass alle Prozesse terminieren.
     */
    @Override
    void close();

    /**
     * F&uuml;hrt eine SQL-Abfrage aus und liefert eine Tabelle der Ergebnisse
     *
     * @param sqlCode Der SQL-Code
     * @return Tabelle der Ergebnisse. Erste Dimension = Zeile; zweite Dimension
     * = Spalte
     * @throws SQLException Tritt auf, wenn der SQL-Befehl einen Fehler
     * ausl&ouml;ste.
     */
    LinkedList<String[]> execQuery(String sqlCode) throws SQLException;
}
