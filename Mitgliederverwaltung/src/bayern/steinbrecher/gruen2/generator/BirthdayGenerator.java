/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.generator;

import bayern.steinbrecher.gruen2.data.DataProvider;
import bayern.steinbrecher.gruen2.people.Member;
import bayern.steinbrecher.gruen2.people.Person;
import java.time.LocalDate;
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

    /**
     * Sorts according to a members birthday (year descending, month ascending,
     * day ascending) and then according to members name.
     */
    public static final Comparator<Member> SORTING = Comparator.comparing(
            (Member m) -> m.getPerson().getBirthday().getYear()).reversed()
            .thenComparing(m -> m.getPerson().getBirthday().getMonth())
            .thenComparing(m -> m.getPerson().getBirthday().getDayOfMonth())
            .thenComparing(m -> m.getPerson().getName());
    /**
     * Represents a function creating a {@code String} out of a {@code Member}
     * containing the prename, lastname and the birthday of the member. Values
     * are separated by semicolon and the {@code String} is closed by a
     * linebreak.
     */
    private static final Function<Member, String> PRINT_LINE = m -> {
        Person p = m.getPerson();
        return p.getPrename() + ';' + p.getLastname() + ';'
                + p.getBirthday() + '\n';
    };

    /**
     * Prohibit construction of an object.
     */
    private BirthdayGenerator() {
        throw new UnsupportedOperationException(
                "Construction of an object not allowed.");
    }

    /**
     * Creates a {@code String} representing content for a CSV-file containing a
     * grouped list of the given member and their age they had or will have in
     * year {@code year}.
     *
     * @param member The member to print.
     * @param year The year in which the age of the given member has to be
     * calculated.
     * @return A string representing the hole content of a CSV file.
     */
    public static String createGroupedOutput(List<Member> member, int year) {
        if (member.isEmpty()) {
            throw new IllegalArgumentException(
                    "Can't create output when member is empty.");
        }

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

    /**
     * Appends all member of {@code currentAgeActive} and
     * {@code currentAgePassiv} to {@code output} if the lists are not empty.
     *
     * @param output The {@code StringBuilder} to append the output to.
     * @param currentAge The age of the member in the given lists.
     * @param currentAgeActive The list of active member which are
     * {@code currentAge} years old.
     * @param currentAgePassive The list of passiv member which are
     * {@code currentAge} years old.
     */
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

    /**
     * Checks whether the given member fits the configured birthday criteria.
     *
     * @param m The member to check.
     * @param year The year to calculate his age at.
     * @return {@code true} only if {@code m} fits the configured criteria.
     * @see DataProvider#ageFunction
     */
    public static boolean getsNotified(Member m, int year) {
        LocalDate birthday = m.getPerson().getBirthday();
        if (birthday == null) {
            return false;
        } else {
            int age = year - birthday.getYear();
            return DataProvider.getProfile().getAgeFunction().apply(age);
        }
    }
}
