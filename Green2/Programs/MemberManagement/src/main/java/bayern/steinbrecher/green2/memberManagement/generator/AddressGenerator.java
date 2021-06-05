package bayern.steinbrecher.green2.memberManagement.generator;

import bayern.steinbrecher.green2.sharedBasis.people.Address;
import bayern.steinbrecher.green2.sharedBasis.people.Member;
import bayern.steinbrecher.green2.sharedBasis.people.Person;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Represents a generator for generating a table of member with their names, addresses and their salutation. The
 * salutation uses given nicknames instead of the real name.
 *
 * @author Stefan Huber
 */
public final class AddressGenerator {

    /**
     * Prohibit construction of an object.
     */
    private AddressGenerator() {
        throw new UnsupportedOperationException("Construction of an object not supported");
    }

    /**
     * Generates a {@link String} representing a CSV-file of member with their addresses and their salutation. It also
     * contains column labels and you can import it into Word for serial letters.
     *
     * @param member The member to generate output for.
     * @param nicknames The nicknames to use for addresses.
     * @return The content for the output CSV file.
     */
    public static String generateAddressData(Collection<Member> member, Map<String, String> nicknames) {
        if (member.isEmpty()) {
            throw new IllegalArgumentException("Can't create output when member is empty.");
        }
        Map<Member, String> memberSalutationMapping = createSalutations(member, nicknames);
        return createOutput(memberSalutationMapping);
    }

    /**
     * Creates a list with salutations for the given member in the same order as the member.
     *
     * @param member The member to create an salutations for.
     * @param nicknames The map containing the nicknames used for the salutations.
     * @return A list with appropriate salutations.
     */
    private static Map<Member, String> createSalutations(Collection<Member> member, Map<String, String> nicknames) {
        return member.stream().collect(Collectors.toMap(m -> m,
                m -> (m.person().male() ? "Lieber " : "Liebe ")
                + nicknames.getOrDefault(m.person().firstname(), m.person().firstname())));
    }

    /**
     * Creates output representing {@link Member}. The order is not guaranteed. But the first row will contain column
     * labels (german).
     *
     * @param memberSalutationsMapping The map from the given {@link Member} to their salutations.
     * @return A {@link String} representing the output.
     */
    private static String createOutput(Map<Member, String> memberSalutationsMapping) {
        return new StringBuilder("Vorname;Nachname;Strasse;Hausnummer;PLZ;Ort;Geburtstag;Anrede\n")
                .append(memberSalutationsMapping.entrySet().stream()
                        .map(entry -> {
                            Person person = entry.getKey().person();
                            Address address = entry.getKey().home();
                            return new StringJoiner(";")
                                    .add(person.firstname())
                                    .add(person.lastname())
                                    .add(address.street())
                                    .add(address.houseNumber())
                                    .add(address.postcode())
                                    .add(address.place())
                                    .add(String.valueOf(person.birthday()))
                                    .add(entry.getValue())
                                    .toString();
                        })
                        .collect(Collectors.joining("\n")))
                .toString();
    }
}
