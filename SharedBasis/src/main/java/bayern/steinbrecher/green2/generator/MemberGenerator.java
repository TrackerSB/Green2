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

import bayern.steinbrecher.green2.connection.DBConnection;
import bayern.steinbrecher.green2.connection.scheme.Columns;
import bayern.steinbrecher.green2.connection.scheme.Tables;
import bayern.steinbrecher.green2.people.AccountHolder;
import bayern.steinbrecher.green2.people.Address;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.people.Person;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Converts String results of database into member.
 *
 * @author Stefan Huber
 */
public final class MemberGenerator {

    /**
     * Prohibit construction.
     */
    private MemberGenerator() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
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
            Map<Columns<?>, Integer> columnMapping, Columns<T> column) {
        if (columnMapping.containsKey(column)) {
            String cellValue = row.get(columnMapping.get(column));
            Optional<T> value;
            if (cellValue == null) {
                value = Optional.empty();
            } else {
                value = column.parse(cellValue);
            }
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    /*
     * Picks the correct column and parses it to the specified type.
     */
    private static <T> T pickAndConvert(List<String> row, Map<Columns<?>, Integer> columnMapping, Tables table,
            Columns<T> column) {
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
    public static Set<Member> generateMemberList(List<List<String>> queryResult) {
        Map<Columns<?>, Integer> columnMapping
                = DBConnection.generateColumnMapping(Tables.MEMBER, queryResult.get(0));

        return queryResult.parallelStream()
                //Skip columnnames
                .skip(1)
                .map(row -> {
                    //Read attributes
                    LocalDate birthday = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.BIRTHDAY);
                    LocalDate mandatsigned = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.MANDAT_SIGNED);
                    Boolean male = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.IS_MALE);
                    Boolean isActive = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.IS_ACTIVE);
                    Boolean isContributionfree = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.IS_CONTRIBUTIONFREE);
                    Integer membershipnumber = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.MEMBERSHIPNUMBER);
                    String prename = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.PRENAME);
                    String lastname = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.LASTNAME);
                    String title = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.TITLE);
                    String iban = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.IBAN);
                    String bic = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.BIC);
                    String accountholderPrename = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.ACCOUNTHOLDER_PRENAME);
                    if (accountholderPrename == null || accountholderPrename.isEmpty()) {
                        accountholderPrename = prename;
                    }
                    String accountholderLastname = MemberGenerator.pickAndConvert(row, columnMapping,
                            Tables.MEMBER, Columns.ACCOUNTHOLDER_LASTNAME);
                    if (accountholderLastname == null || accountholderLastname.isEmpty()) {
                        accountholderLastname = lastname;
                    }
                    Double contribution = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.CONTRIBUTION);
                    String street = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.STREET);
                    String housenumber = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.HOUSENUMBER);
                    String cityCode = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.CITY_CODE);
                    String city = MemberGenerator.pickAndConvert(
                            row, columnMapping, Tables.MEMBER, Columns.CITY);

                    //Connect attributes
                    Person p = new Person(prename, lastname, title, birthday, male);
                    //FIXME mandateChanged has not to be always false
                    AccountHolder ah = new AccountHolder(iban, bic, mandatsigned, false, accountholderPrename,
                            accountholderLastname, title, birthday, male);
                    Address ad = new Address(street, housenumber, cityCode, city);
                    return new Member(membershipnumber, p, ad, ah, isActive, isContributionfree, contribution);
                })
                .collect(Collectors.toSet());
    }
}
