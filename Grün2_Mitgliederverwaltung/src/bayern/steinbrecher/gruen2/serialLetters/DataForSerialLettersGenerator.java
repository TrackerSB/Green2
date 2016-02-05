package bayern.steinbrecher.gruen2.serialLetters;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.member.Address;
import bayern.steinbrecher.gruen2.member.Member;
import bayern.steinbrecher.gruen2.member.Person;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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

    public static void generateAddressData(List<Member> member,
            Map<String, String> nicknames) {
        List<String> addresses = appendAddresses(member, nicknames);
        createOutputCsvFile(createOutput(member, addresses));
    }

    /**
     * Creates a list with addresses for the given member.
     *
     * @param member The member to create an address for.
     * @param nicknames The map containing the nicknames.
     *
     * @return A list with apropriate addresses.
     */
    private static List<String> appendAddresses(
            List<Member> member, Map<String, String> nicknames) {

        List<String> addresses = new ArrayList<>(member.size());
        member.stream().forEach(m -> {
            String address = m.getPerson().isIsMale() ? "Lieber " : "Liebe ";
            address += nicknames.getOrDefault(
                    m.getPerson().getPrename(), m.getPerson().getPrename());
            addresses.add(address);
        });
        assert addresses.size() == member.size();

        return addresses;
    }

    /**
     * Creates output representing {@code resultTable}. The order is not
     * guaranteed apart from the first row. The first row will still be the
     * first row.
     *
     * @param member The table to output. First dimension row; second column.
     * @param addresses The list containing the apropriate addresses for the
     * member.
     * @return A String representing the output.
     */
    private static String createOutput(List<Member> member,
            List<String> addresses) {
        StringBuilder output = new StringBuilder(
                "Vorname;Nachname;Strasse;Hausnummer;PLZ;Ort;Anrede");
        for (int i = 0; i < member.size(); i++) {
            Person currentPerson = member.get(i).getPerson();
            Address currentAddress = member.get(i).getHome();
            output.append(currentPerson.getPrename()).append(';')
                    .append(currentPerson.getLastname()).append(';')
                    .append(currentAddress.getStreet()).append(';')
                    .append(currentAddress.getHousenumber()).append(';')
                    .append(currentAddress.getPostcode()).append(';')
                    .append(currentAddress.getPlace()).append(';')
                    .append(addresses.get(i)).append('\n');
        }

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
