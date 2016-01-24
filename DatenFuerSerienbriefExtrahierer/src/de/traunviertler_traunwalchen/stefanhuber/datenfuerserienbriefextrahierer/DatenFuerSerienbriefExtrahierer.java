package de.traunviertler_traunwalchen.stefanhuber.datenfuerserienbriefextrahierer;

import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.DatabaseConnection;
import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.DatabaseConnectorModel;
import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.ssh.DatabaseConnectorSSH;
import de.traunviertler_traunwalchen.stefanhuber.databaseconnector.standard.DatabaseConnectorStandard;
import de.traunviertler_traunwalchen.stefanhuber.dataprovider.DataProvider;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Diese Klasse fragt die Mitglieder und die Spitznamen aus der GTEV-Datenbank
 * ab und erstellt eine Tabelle (CSV-Datei) mit Vorname, Nachname, Adresse und
 * Anrede, die dann f&uuml;r einen Serienbrief in Microsoft Word verwendet
 * werden kann.
 *
 * @author Stefan Huber
 */
public class DatenFuerSerienbriefExtrahierer extends Application {

    /**
     * true = SQL over SSH, false = SQL over JDBC.
     */
    public static final boolean SSH_CONNECTION = true;

    /**
     * Die start-Methode. Einstiegspunkt jeder JavaFX-Applikation.
     *
     * @param primaryStage Die initiale Stage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseConnectorModel databaseConnector;
        if (SSH_CONNECTION) {
            databaseConnector = new DatabaseConnectorSSH();
        } else {
            databaseConnector = new DatabaseConnectorStandard();
        }
        databaseConnector.setCaller(() -> {
            DatabaseConnection databaseConnection
                    = databaseConnector.getConnection();
            //Wenn null, dann wurde das Fenster per "X" geschlossen.
            if (databaseConnection != null) {
                LinkedList<String[]> member
                        = readMember(databaseConnection);
                HashMap<String, String> nicknames
                        = readNicknames(databaseConnection);
                replaceGenderWithAddress(member, nicknames);
                createOutputCsvFile(createOutput(member));
                databaseConnection.close();
            }
            return null;
        });
        databaseConnector.start(primaryStage);
    }

    /**
     * Die main-Methode.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private LinkedList<String[]> readMember(DatabaseConnection dbc) {
        LinkedList<String[]> result = null;
        try {
            result = dbc.execQuery(
                    "SELECT Vorname, Nachname, Strasse, Hausnummer, PLZ, Ort, "
                    + "istMaennlich "
                    + "FROM Mitglieder "
                    + "WHERE AusgetretenSeit='0000-00-00'");
        } catch (SQLException ex) {
            Logger.getLogger(DatenFuerSerienbriefExtrahierer.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private HashMap<String, String> readNicknames(DatabaseConnection dbc) {
        HashMap<String, String> nicknames = new HashMap<>();
        try {
            dbc.execQuery("SELECT * FROM Spitznamen").parallelStream()
                    .forEach(row -> nicknames.put(row[0], row[1]));
        } catch (SQLException ex) {
            Logger.getLogger(DatenFuerSerienbriefExtrahierer.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return nicknames;
    }

    /**
     * Diese Methode konvertiert <code>member</code> mit
     * <code>convertResultSetToArray(...)</code> und ersetzt die Spalte
     * IstMaennlich durch eine passende Anrede ggf mit Spitznamen aus
     * <code>nicknames</code>.
     *
     * @param member Die Liste mit den Datens&auml;tzen der Mitglieder (OHNE
     * Spaltennamen!)
     * @param nicknames Die Liste mit den Datens&auml;tzen der Spitznamen
     * Indizes zuordnet.
     */
    private void replaceGenderWithAddress(LinkedList<String[]> member,
            HashMap<String, String> nicknames) {
        member.parallelStream().forEach(row -> {
            String adress = row[row.length - 1].equals("1") ? "Lieber "
                    : "Liebe ";
            adress += nicknames.getOrDefault(row[0], row[0]);
            row[row.length - 1] = adress;
        });
    }

    private void createOutputCsvFile(String output) {
        //Datei schreiben
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(DataProvider.getSavepath()
                        + "/Serienbriefdaten.csv"), "UTF-8"))) {
            /* Dies dient dazu aus "UTF-8 ohne Bom", "UTF-8 MIT Bom" zu machen,
             * damit Microsoft Excel Sonderzeichen korrekt interpretiert.
             */
            bw.append('\uFEFF')
                    .append(output);
        } catch (IOException ex) {
            Logger.getLogger(DatenFuerSerienbriefExtrahierer.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private String createOutput(LinkedList<String[]> table) {
        StringBuilder output = new StringBuilder();
        output.append("Vorname;Nachname;Strasse;Hausnummer;PLZ;Ort;Anrede\n");
        table.parallelStream().forEach(row -> {
            StringBuilder formattedRow = new StringBuilder();
            for (String field : row) {
                formattedRow.append(field).append(';');
            }
            formattedRow.setCharAt(formattedRow.length() - 1, '\n');
            synchronized (output) {
                output.append(formattedRow);
            }
        });
        return output.toString();
    }
}
