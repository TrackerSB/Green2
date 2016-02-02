package bayern.steinbrecher.gruen2.serialLetters;

import bayern.steinbrecher.gruen2.data.DataProvider;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    public static void generateAddressData(List<List<String>> member,
            Map<String, String> nicknames) {
        appendAddresses(member, nicknames);
        createOutputCsvFile(createOutput(member));
    }

    /**
     * Diese Methode fügt die Anreden zu <code>member</code> hinzu.
     *
     * @param member Die Liste mit den Datens&auml;tzen der Mitglieder
     * @param nicknames Die Liste mit den Datens&auml;tzen der Spitznamen.
     */
    private static void appendAddresses(
            List<List<String>> member, Map<String, String> nicknames) {
        int istMaennlichIndex = member.get(0).indexOf("istMaennlich");
        int vornameIndex = member.get(0).indexOf("Vorname");
        member.get(0).add("Anrede");
        member.parallelStream().skip(1).forEach(row -> {
            String address = row.get(istMaennlichIndex).equalsIgnoreCase("1")
                    ? "Lieber " : "Liebe ";
            address += nicknames.getOrDefault(
                    row.get(vornameIndex), row.get(vornameIndex));
            row.add(address);
        });
    }

    private static String createOutput(List<List<String>> resultTable) {
        StringBuilder output = new StringBuilder();
        resultTable.parallelStream().forEach(row -> {
            StringBuilder formattedRow = new StringBuilder();
            row.stream().forEach(field -> {
                formattedRow.append(field).append(';');
            });
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
