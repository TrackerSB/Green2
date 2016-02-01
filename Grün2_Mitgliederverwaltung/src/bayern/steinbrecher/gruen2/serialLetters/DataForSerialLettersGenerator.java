package bayern.steinbrecher.gruen2.serialLetters;

import bayern.steinbrecher.gruen2.connection.DBConnection;
import bayern.steinbrecher.gruen2.data.DataProvider;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Diese Klasse fragt die Mitglieder und die Spitznamen aus der GTEV-Datenbank
 * ab und erstellt eine Tabelle (CSV-Datei) mit Vorname, Nachname, Adresse und
 * Anrede, die dann f&uuml;r einen Serienbrief in Microsoft Word verwendet
 * werden kann.
 *
 * @author Stefan Huber
 */
public class DataForSerialLettersGenerator {

    private DataForSerialLettersGenerator() {
        throw new UnsupportedOperationException(
                "Construction of an object not supported");
    }

    public static void generateAddressData(DBConnection dbc) {
        List<String[]> member = readMember(dbc);
        Map<String, String> nicknames = readNicknames(dbc);
        replaceGenderWithAddress(member, nicknames);
        createOutputCsvFile(createOutput(member));
    }

    private static List<String[]> readMember(DBConnection dbc) {
        List<String[]> result = null;
        try {
            result = dbc.execQuery(
                    "SELECT Vorname, Nachname, Strasse, Hausnummer, PLZ, Ort, "
                    + "istMaennlich "
                    + "FROM Mitglieder "
                    + "WHERE AusgetretenSeit='0000-00-00'");
        } catch (SQLException ex) {
            Logger.getLogger(DataForSerialLettersGenerator.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static Map<String, String> readNicknames(DBConnection dbc) {
        HashMap<String, String> nicknames = new HashMap<>();
        try {
            dbc.execQuery("SELECT * FROM Spitznamen").parallelStream()
                    .forEach(row -> nicknames.put(row[0], row[1]));
        } catch (SQLException ex) {
            Logger.getLogger(DataForSerialLettersGenerator.class.getName())
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
    private static void replaceGenderWithAddress(List<String[]> member,
            Map<String, String> nicknames) {
        member.parallelStream().forEach(row -> {
            String adress = row[row.length - 1].equals("1") ? "Lieber "
                    : "Liebe ";
            adress += nicknames.getOrDefault(row[0], row[0]);
            row[row.length - 1] = adress;
        });
    }

    private static String createOutput(List<String[]> table) {
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

    private static void createOutputCsvFile(String output) {
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
            Logger.getLogger(DataForSerialLettersGenerator.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
