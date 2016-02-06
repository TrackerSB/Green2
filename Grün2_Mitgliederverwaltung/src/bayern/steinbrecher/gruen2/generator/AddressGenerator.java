package bayern.steinbrecher.gruen2.generator;

import bayern.steinbrecher.gruen2.Output;
import bayern.steinbrecher.gruen2.member.Address;
import bayern.steinbrecher.gruen2.member.Member;
import bayern.steinbrecher.gruen2.member.Person;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse fragt die Mitglieder und die Spitznamen aus der GTEV-Datenbank
 * ab und erstellt eine Tabelle (CSV-Datei) mit Vorname, Nachname, Adresse und
 * Anrede, die dann f&uuml;r einen Serienbrief in Microsoft Word verwendet
 * werden kann.
 *
 * @author Stefan Huber
 */
public class AddressGenerator {

    private AddressGenerator() {
        throw new UnsupportedOperationException(
                "Construction of an object not supported");
    }

    public static void generateAddressData(List<Member> member,
            Map<String, String> nicknames, String filename) {
        List<String> addresses = appendAddresses(member, nicknames);
        Output.printContent(createOutput(member, addresses), filename);
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
                "Vorname;Nachname;Strasse;Hausnummer;PLZ;Ort;Anrede\n");
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
}
