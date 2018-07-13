/*
 * Copyright (C) 2018 Stefan Huber
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
package bayern.steinbrecher.green2.generator;

import bayern.steinbrecher.green2.people.Address;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.people.Person;
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
                m -> (m.getPerson().isMale() ? "Lieber " : "Liebe ")
                + nicknames.getOrDefault(m.getPerson().getPrename(), m.getPerson().getPrename())));
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
                            Person person = entry.getKey().getPerson();
                            Address address = entry.getKey().getHome();
                            return new StringJoiner(";")
                                    .add(person.getPrename())
                                    .add(person.getLastname())
                                    .add(address.getStreet())
                                    .add(address.getHouseNumber())
                                    .add(address.getPostcode())
                                    .add(address.getPlace())
                                    .add(String.valueOf(person.getBirthday()))
                                    .add(entry.getValue())
                                    .toString();
                        })
                        .collect(Collectors.joining("\n")))
                .toString();
    }
}
