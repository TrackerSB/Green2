/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.generator;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.data.Profile;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.people.Person;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Generates files containing the relevant birthdays of a given year and writes them to a file.
 *
 * @author Stefan Huber
 */
public class BirthdayGenerator {

    /**
     * Sorts according to a members birthday (year descending, month ascending, day ascending) and then according to
     * members name.
     */
    public static final Comparator<Member> SORTING = Comparator.comparing(
            (Member m) -> m.getPerson().getBirthday().getYear()).reversed()
            .thenComparing(m -> m.getPerson().getBirthday().getMonth())
            .thenComparing(m -> m.getPerson().getBirthday().getDayOfMonth())
            .thenComparing(m -> m.getPerson().getName());
    /**
     * Represents a function creating a {@link String} out of a {@link Member} containing the first name, last name and
     * the birthday of the member. Values are separated by semicolon and the {@link String} is closed by a linebreak.
     */
    private static final Function<Member, String> PRINT_LINE = m -> {
        Person p = m.getPerson();
        return p.getFirstName() + ';' + p.getLastName() + ';' + p.getBirthday() + '\n';
    };

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

        boolean distinguishActivePassive = member.parallelStream().anyMatch(m -> m.isActive().isPresent());

        StringBuilder output = new StringBuilder("Geburtstage " + year);
        List<Member> currentActive = new ArrayList<>();
        List<Member> currentPassive = new ArrayList<>();
        List<Member> currentNeither = new ArrayList<>();
        int currentAge = year - member.get(0).getPerson().getBirthday().getYear();
        for (Member m : member) {
            if (year - m.getPerson().getBirthday().getYear() != currentAge) {
                appendMember(
                        output, currentAge, currentActive, currentPassive, currentNeither, distinguishActivePassive);
                currentActive.clear();
                currentPassive.clear();
                currentAge = year - m.getPerson().getBirthday().getYear();
            }
            Optional<Boolean> active = m.isActive();
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
                    output.append("Aktiv:\n")
                            .append("Vorname;Nachname;Geburtstag\n")
                            .append(currentAgeActive.stream().map(PRINT_LINE).reduce("", String::concat));
                }
                if (!currentAgePassive.isEmpty()) {
                    output.append("Passiv:\n")
                            .append("Vorname;Nachname;Geburtstag\n")
                            .append(currentAgePassive.stream().map(PRINT_LINE).reduce("", String::concat));
                }
                if (!currentAgeNeither.isEmpty()) {
                    output.append("Unbekannt:\n")
                            .append("Vorname;Nachname;Geburtstag\n")
                            .append(currentAgeNeither.stream().map(PRINT_LINE).reduce("", String::concat));
                }
            } else {
                output.append("Passiv:\n")
                        .append("Vorname;Nachname;Geburtstag\n")
                        .append(currentAgeActive.stream().map(PRINT_LINE).reduce("", String::concat))
                        .append(currentAgePassive.stream().map(PRINT_LINE).reduce("", String::concat))
                        .append(currentAgeNeither.stream().map(PRINT_LINE).reduce("", String::concat));
            }
        }
    }

    /**
     * Checks whether the given member fits the configured birthday criteria.
     *
     * @param m The member to check.
     * @param year The year to calculate his age at.
     * @return {@code true} only if {@code m} fits the configured criteria.
     * @see Profile#getAgeFunction()
     */
    public static boolean getsNotified(Member m, int year) {
        LocalDate birthday = m.getPerson().getBirthday();
        if (birthday == null) {
            return false;
        } else {
            int age = year - birthday.getYear();
            return EnvironmentHandler.getProfile().getAgeFunction().apply(age);
        }
    }
}