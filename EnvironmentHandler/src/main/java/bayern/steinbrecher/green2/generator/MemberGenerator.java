/* 
 * Copyright (C) 2017 Stefan Huber
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

import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.people.AccountHolder;
import bayern.steinbrecher.green2.people.Address;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.people.Person;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Converts String results of database into member.
 *
 * @author Stefan Huber
 */
public class MemberGenerator {

    /**
     * Prohibit construction.
     */
    private MemberGenerator() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }

    private static LocalDate parseString(String dateString) {
        LocalDate date = null;
        try {
            if (dateString == null) {
                throw new DateTimeParseException("Can´t parse null", "null", 0);
            } else {
                date = LocalDate.parse(dateString);
            }
        } catch (DateTimeParseException ex) {
            Logger.getLogger(MemberGenerator.class.getName())
                    .log(Level.WARNING, dateString + " is invalid birthdaydate", ex);
        }
        return date;
    }

    /**
     * Returns the appropriate entry of the given row if existing.
     *
     * @param row The row to pick the entry from.
     * @param index The index of the row to pick.
     * @return An {@link Optional} containing the entry. Returns {@link Optional#empty()} if {@code row} does not
     * contain {@code column}. Returns an {@link Optional} of an {@link Optional#empty()} if and and only if {@code row}
     * contains {@code column} but the value is {@code null} or is parsed to {@code null}.
     */
    private static <T> Optional<Optional<T>> getOptionally(List<String> row,
            Map<DBConnection.Columns<?>, Integer> columnMapping, DBConnection.Columns<T> column) {
        if (columnMapping.containsKey(column)) {
            String cell = row.get(columnMapping.get(column));
            T value;
            if (cell == null) {
                value = null;
            } else {
                value = column.parse(cell);
            }
            return Optional.of(Optional.ofNullable(value));
        } else {
            return Optional.empty();
        }
    }

    /*
     * Picks the correct column and parses it to the specified type.
     */
    //FIXME Waiting for JDK 9.
    private static <T> T pickAndConvert(List<String> row, Map<DBConnection.Columns<?>, Integer> columnMapping,
            DBConnection.Tables table, DBConnection.Columns<T> column) {
        Optional<Optional<T>> optionalField = getOptionally(row, columnMapping, column);
        if (optionalField.isPresent()) {
            Optional<T> field = optionalField.get();
            if (field.isPresent()) {
                return field.get();
            } else {
                if (!table.isOptional(column)) {
                    Logger.getLogger(MemberGenerator.class.getName()).log(Level.WARNING,
                            "Column {0} is not optional but contains a null value", column.getRealColumnName());
                }
                return null;
            }
        } else {
            if (table.isOptional(column)) {
                return null;
            } else {
                throw new IllegalStateException(column.getRealColumnName()
                        + " is no optional column but has no mapping which means the column is not accessible.");
            }
        }
    }

    /**
     * Generates a list of member out of {@code queryResult}.
     *
     * @param queryResult The table that hold the member informations. First dimension has to be row; second column.
     * Each row is treated as one member. The first row is skipped since it is assumed it contains the headings of the
     * columns.
     * @return The resulting list of member.
     */
    public static List<Member> generateMemberList(List<List<String>> queryResult) {
        Map<DBConnection.Columns<?>, Integer> columnMapping
                = DBConnection.generateColumnMapping(DBConnection.Tables.MEMBER, queryResult.get(0));

        return queryResult.parallelStream()
                //Skip columnnames
                .skip(1)
                .map(row -> {
                    //Read attributes
                    LocalDate birthday = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.BIRTHDAY);
                    LocalDate mandatsigned = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.MANDAT_SIGNED);
                    Boolean male = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.IS_MALE);
                    Boolean isActive = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.IS_ACTIVE);
                    Boolean isContributionfree = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.IS_CONTRIBUTIONFREE);
                    Integer membershipnumber = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.MEMBERSHIPNUMBER);
                    String prename = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.PRENAME);
                    String lastname = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.LASTNAME);
                    String title = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.TITLE);
                    String iban = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.IBAN);
                    String bic = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.BIC);
                    String accountholderPrename = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.ACCOUNTHOLDER_PRENAME);
                    if (accountholderPrename == null || accountholderPrename.isEmpty()) {
                        accountholderPrename = prename;
                    }
                    String accountholderLastname = MemberGenerator.pickAndConvert(row, columnMapping,
                            DBConnection.Tables.MEMBER, DBConnection.Columns.ACCOUNTHOLDER_LASTNAME);
                    if (accountholderLastname == null || accountholderLastname.isEmpty()) {
                        accountholderLastname = lastname;
                    }
                    Double contribution = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.CONTRIBUTION);
                    String street = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.STREET);
                    String housenumber = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.HOUSENUMBER);
                    String cityCode = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.CITY_CODE);
                    String city = MemberGenerator.pickAndConvert(
                            row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.CITY);

                    //Connect attributes
                    Person p = new Person(prename, lastname, title, birthday, male);
                    //FIXME mandateChanged has not to be always false
                    AccountHolder ah = new AccountHolder(iban, bic, mandatsigned, false, accountholderPrename,
                            accountholderLastname, title, birthday, male);
                    Address ad = new Address(street, housenumber, cityCode, city);
                    return new Member(membershipnumber, p, ad, ah, isActive, isContributionfree, contribution);
                }).collect(Collectors.toList());
    }
}
