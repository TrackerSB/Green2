package bayern.steinbrecher.gruen2.serialLetters;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.databaseconnector.DatabaseConnection;
import bayern.steinbrecher.gruen2.databaseconnector.DatabaseConnectorModel;
import bayern.steinbrecher.gruen2.databaseconnector.ssh.DatabaseConnectorSSH;
import bayern.steinbrecher.gruen2.databaseconnector.standard.DatabaseConnectorStandard;
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
public class DataForSerialLetters extends Application {

    /**
     * true = SQL over SSH, false = SQL over JDBC.
     */
    public final boolean connectOverSsh;

    public DataForSerialLetters(boolean connectOverSsh) {
        this.connectOverSsh = connectOverSsh;
    }

    /**
     * Die start-Methode. Einstiegspunkt jeder JavaFX-Applikation.
     *
     * @param primaryStage Die initiale Stage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseConnectorModel databaseConnector;
        if (connectOverSsh) {
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

    private LinkedList<String[]> readMember(DatabaseConnection dbc) {
        LinkedList<String[]> result = null;
        try {
            result = dbc.execQuery(
                    "SELECT Vorname, Nachname, Strasse, Hausnummer, PLZ, Ort, "
                    + "istMaennlich "
                    + "FROM Mitglieder "
                    + "WHERE AusgetretenSeit='0000-00-00'");
        } catch (SQLException ex) {
            Logger.getLogger(DataForSerialLetters.class.getName())
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
            Logger.getLogger(DataForSerialLetters.class.getName())
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
            Logger.getLogger(DataForSerialLetters.class.getName())
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
