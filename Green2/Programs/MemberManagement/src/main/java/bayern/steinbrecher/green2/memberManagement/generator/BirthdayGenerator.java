package bayern.steinbrecher.green2.memberManagement.generator;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.Profile;
import bayern.steinbrecher.green2.sharedBasis.people.Member;
import bayern.steinbrecher.green2.sharedBasis.people.Person;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Generates files containing the relevant birthdays of a given year and writes them to a file.
 *
 * @author Stefan Huber
 */
public final class BirthdayGenerator {

    /**
     * Sorts according to a members birthday (year descending, month ascending, day ascending) and then according to
     * members name.
     */
    public static final Comparator<Member> SORTING = Comparator.comparing(
            (Member m) -> m.person().birthday().getYear()).reversed()
            .thenComparing(m -> m.person().birthday().getMonth())
            .thenComparing(m -> m.person().birthday().getDayOfMonth())
            .thenComparing(m -> m.person().name());

    /**
     * Prohibit construction of an object.
     */
    private BirthdayGenerator() {
        throw new UnsupportedOperationException("Construction of an object not allowed.");
    }

    /**
     * Creates a {@link String} representing content for a CSV-file containing a grouped list of the given member and
     * their age they had or will have in year {@code year}.
     *
     * @param member The member to print.
     * @param year The year in which the age of the given member has to be calculated.
     * @return A string representing the hole content of a CSV file.
     */
    public static String createGroupedOutput(List<Member> member, int year) {
        if (member.isEmpty()) {
            throw new IllegalArgumentException("Can't create output when member is empty.");
        }

        member.sort(SORTING);

        boolean distinguishActivePassive = member.parallelStream().anyMatch(m -> m.active().isPresent());

        StringBuilder output = new StringBuilder("Geburtstage ")
                .append(year);
        List<Member> currentActive = new ArrayList<>();
        List<Member> currentPassive = new ArrayList<>();
        List<Member> currentNeither = new ArrayList<>();
        int currentAge = year - member.get(0).person().birthday().getYear();
        for (Member m : member) {
            if (year - m.person().birthday().getYear() != currentAge) {
                appendMember(
                        output, currentAge, currentActive, currentPassive, currentNeither, distinguishActivePassive);
                currentActive.clear();
                currentPassive.clear();
                currentAge = year - m.person().birthday().getYear();
            }
            Optional<Boolean> active = m.active();
            if (active.isPresent()) {
                if (active.get()) {
                    currentActive.add(m);
                } else {
                    currentPassive.add(m);
                }
            } else {
                currentNeither.add(m);
            }
        }

        appendMember(output, currentAge, currentActive, currentPassive, currentNeither, distinguishActivePassive);

        return output.toString();
    }

    private static StringBuilder getMemberLines(List<Member> member) {
        return member.stream()
                .map(m -> {
                    Person person = m.person();
                    return person.firstname() + ';' + person.lastname() + ';' + person.birthday() + '\n';
                })
                .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append);
    }

    /**
     * Appends all member of {@code currentAgeActive} and {@code currentAgePassiv} to {@code output} if the lists are
     * not empty. The order of every list remains unchanged. The order of the lists printed is:
     * <ul>
     * <li>{@code currentAgeActive}</li>
     * <li>{@code currentAgePassive}</li>
     * <li>{@code currentAgeNeither}</li>
     * </ul>
     *
     * @param output The {@link StringBuilder} to append the output to.
     * @param currentAge The age of the member in the given lists.
     * @param currentAgeActive The list of active member which are {@code currentAge} years old.
     * @param currentAgePassive The list of passive member which are {@code currentAge} years old.
     * @param currentAgeNeither The list of member whose status (active/passive) is unknown.
     * @param distinguishActivePassive {@code true} if the given lists should not be merged.
     */
    private static void appendMember(StringBuilder output, int currentAge, List<Member> currentAgeActive,
            List<Member> currentAgePassive, List<Member> currentAgeNeither, boolean distinguishActivePassive) {
        if (!currentAgeActive.isEmpty() || !currentAgePassive.isEmpty() || !currentAgeNeither.isEmpty()) {
            output.append("\n\n").append(currentAge).append("ter Geburtstag\n");

            if (distinguishActivePassive) {
                if (!currentAgeActive.isEmpty()) {
                    output.append("Aktiv:\nVorname;Nachname;Geburtstag\n")
                            .append(getMemberLines(currentAgeActive));
                }
                if (!currentAgePassive.isEmpty()) {
                    output.append("Passiv:\nVorname;Nachname;Geburtstag\n")
                            .append(getMemberLines(currentAgePassive));
                }
                if (!currentAgeNeither.isEmpty()) {
                    output.append("Unbekannt:\nVorname;Nachname;Geburtstag\n")
                            .append(getMemberLines(currentAgeNeither));
                }
            } else {
                output.append("Vorname;Nachname;Geburtstag\n")
                        .append(getMemberLines(currentAgeActive))
                        .append(getMemberLines(currentAgePassive))
                        .append(getMemberLines(currentAgeNeither));
            }
        }
    }

    /**
     * Checks whether the given member fits the configured birthday criteria.
     *
     * @param member The member to check.
     * @param year The year to calculate his age at.
     * @return {@code true} only if {@code m} fits the configured criteria.
     * @see Profile#getAgeFunction()
     */
    public static boolean getsNotified(Member member, int year) {
        LocalDate birthday = member.person().birthday();
        return birthday != null
                && EnvironmentHandler.getProfile()
                        .getAgeFunction()
                        .apply(year - birthday.getYear());
    }
}
