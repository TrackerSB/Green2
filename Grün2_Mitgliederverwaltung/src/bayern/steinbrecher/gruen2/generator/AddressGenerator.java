package bayern.steinbrecher.gruen2.generator;

import bayern.steinbrecher.gruen2.member.Address;
import bayern.steinbrecher.gruen2.member.Member;
import bayern.steinbrecher.gruen2.member.Person;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a generator for generating a table of member with their names,
 * addresses and their saluttaion. The salutation uses given nicknames instead
 * of the real name.
 *
 * @author Stefan Huber
 */
public class AddressGenerator {

    /**
     * Prohibit construction of an object.
     */
    private AddressGenerator() {
        throw new UnsupportedOperationException(
                "Construction of an object not supported");
    }

    /**
     * Generates a {@code String} representing a CSV-file of member with their
     * addresses and their salutation. It also contains column labels and you
     * can import it into Word for serial letters.
     *
     * @param member
     * @param nicknames
     * @return
     */
    public static String generateAddressData(List<Member> member,
            Map<String, String> nicknames) {
        List<String> salutations = createSalutations(member, nicknames);
        return createOutput(member, salutations);
    }

    /**
     * Creates a list with salutations for the given member in the same order as
     * the member.
     *
     * @param member The member to create an salutations for.
     * @param nicknames The map containing the nicknames used for the
     * salutations.
     *
     * @return A list with apropriate salutations.
     */
    private static List<String> createSalutations(
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
     * Creates output representing {@code member}. The order is not guaranteed.
     * But the first row will contain column labels (german).
     *
     * @param member The list of member to create output for.
     * @param addresses The list containing the apropriate salutations for the
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
