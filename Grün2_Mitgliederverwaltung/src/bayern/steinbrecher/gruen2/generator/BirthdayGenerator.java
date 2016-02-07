package bayern.steinbrecher.gruen2.generator;

import bayern.steinbrecher.gruen2.member.Member;
import bayern.steinbrecher.gruen2.member.Person;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Generates files containing the relevant birthdays of a given year and writes
 * them to a file.
 *
 * @author Stefan Huber
 */
public class BirthdayGenerator {

    public static final Comparator<Member> SORTING = Comparator.comparing(
            (Member m) -> m.getPerson().getBirthday().getYear()).reversed()
            .thenComparing(m -> m.getPerson().getBirthday().getMonth())
            .thenComparing(m -> m.getPerson().getBirthday().getDayOfMonth())
            .thenComparing(m -> m.getPerson().getName());
    private static final Function<Member, String> PRINT_LINE = m -> {
        Person p = m.getPerson();
        return new StringBuilder().append(p.getPrename()).append(';')
                .append(p.getLastname()).append(';')
                .append(p.getBirthday()).append('\n').toString();
    };

    private BirthdayGenerator() {
        throw new UnsupportedOperationException(
                "Construction of an object not allowed.");
    }

    public static String createGroupedOutput(List<Member> member, int year) {
        assert !member.isEmpty();
        member.sort(SORTING);

        StringBuilder output = new StringBuilder(
                "Geburtstage " + year);
        List<Member> currentActive = new ArrayList<>();
        List<Member> currentPassive = new ArrayList<>();
        int currentAge = year
                - member.get(0).getPerson().getBirthday().getYear();
        for (Member m : member) {
            if (year - m.getPerson().getBirthday().getYear() != currentAge) {
                tryAppendMember(output, currentAge,
                        currentActive, currentPassive);
                currentActive.clear();
                currentPassive.clear();
                currentAge = year - m.getPerson().getBirthday().getYear();
            }
            if (m.isActive()) {
                currentActive.add(m);
            } else {
                currentPassive.add(m);
            }
        }

        tryAppendMember(output, currentAge, currentActive, currentPassive);

        return output.toString();
    }

    private static void tryAppendMember(StringBuilder output, int currentAge,
            List<Member> currentAgeActive, List<Member> currentAgePassive) {
        if (!currentAgeActive.isEmpty() || !currentAgePassive.isEmpty()) {
            output.append("\n\n").append(currentAge).append("ter Geburtstag\n");
            if (!currentAgeActive.isEmpty()) {
                output.append("Aktiv:\n")
                        .append("Vorname;Nachname;Geburtstag\n")
                        .append(currentAgeActive.stream().map(PRINT_LINE)
                                .reduce(String::concat).get());
            }
            if (!currentAgePassive.isEmpty()) {
                output.append("Passiv:\n")
                        .append("Vorname;Nachname;Geburtstag\n")
                        .append(currentAgePassive.stream().map(PRINT_LINE)
                                .reduce(String::concat).get());
            }
        }
    }
}
