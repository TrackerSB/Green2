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

    private static Optional<String> getOptionally(List<String> row, Integer index) {
        if (index == null || index < 0) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(row.get(index));
        }
    }

    /*
     * Picks the correct column and parses it to the specified type.
     */
    //FIXME Waiting for JDK 9.
    private static <T> T pickAndConvert(List<String> row, Map<DBConnection.Columns, Integer> columnMapping,
            DBConnection.Tables table, DBConnection.Columns column) {
        throw new UnsupportedOperationException("It is only supported in JDK9.");
        /*Class<T> typeT = (Class<T>) ((ParameterizedType) DBConnection.Columns.class.getGenericSuperclass())
                .getActualTypeArguments()[0];
        Optional<String> optionalField = getOptionally(row, columnMapping.get(column));
        if (optionalField.isPresent()) {
            T value;
            if (Boolean.class.isAssignableFrom(typeT)) {
                value = (T) (Boolean) optionalField.get().equalsIgnoreCase("1");
            } else if (LocalDate.class.isAssignableFrom(typeT)) {
                value = (T) parseString(optionalField.get());
            } else if (Integer.class.isAssignableFrom(typeT)) {
                value = (T) (Integer) Integer.parseInt(optionalField.get());
            } else if (Double.class.isAssignableFrom(typeT)) {
                value = (T) (Double) Double.parseDouble(optionalField.get());
            } else if (String.class.isAssignableFrom(typeT)) {
                value = (T) optionalField.get();
            } else {
                throw new IllegalArgumentException("Type " + typeT.getSimpleName() + " not supported.");
            }
            return value;
        } else {
            if (table.isOptional(column)) {
                return null;
            } else {
                throw new IllegalStateException(
                        column.getRealColumnName() + " is no optional column but has no mapping.");
            }
        }*/
    }

    /*
     * Picks the correct column and parses it to the specified type.
     */
    //FIXME Waiting for JDK 9.
    @Deprecated
    private static <T> T pickAndConvert(List<String> row, Map<DBConnection.Columns, Integer> columnMapping,
            DBConnection.Tables table, DBConnection.Columns column, Class<T> clazz) {
        /*Class<T> typeT = (Class<T>) ((ParameterizedType) DBConnection.Columns.class.getGenericSuperclass())
                .getActualTypeArguments()[0];*/
        Optional<String> optionalField = getOptionally(row, columnMapping.get(column));
        if (optionalField.isPresent()) {
            T value;
            if (Boolean.class.isAssignableFrom(clazz)) {
                value = (T) (Boolean) optionalField.get().equalsIgnoreCase("1");
            } else if (LocalDate.class.isAssignableFrom(clazz)) {
                value = (T) parseString(optionalField.get());
            } else if (Integer.class.isAssignableFrom(clazz)) {
                value = (T) (Integer) Integer.parseInt(optionalField.get());
            } else if (Double.class.isAssignableFrom(clazz)) {
                value = (T) (Double) Double.parseDouble(optionalField.get());
            } else if (String.class.isAssignableFrom(clazz)) {
                value = (T) optionalField.get();
            } else {
                throw new IllegalArgumentException("Type " + clazz.getSimpleName() + " not supported.");
            }
            return value;
        } else {
            if (table.isOptional(column)) {
                return null;
            } else {
                throw new IllegalStateException(
                        column.getRealColumnName() + " is no optional column but has no mapping.");
            }
        }
    }

    /**
     * Generates a list of member out of {@code queryResult}.
     *
     * @param queryResult The table that hold the member informations. First dimension has to be row; second column.
     * Each row is treated as one member. First row has to contain the headings.
     * @return The resulting list of member.
     */
    public static List<Member> generateMemberList(List<List<String>> queryResult) {
        Map<DBConnection.Columns, Integer> columnMapping
                = DBConnection.generateColumnMapping(DBConnection.Tables.MEMBER, queryResult.get(0));

        return queryResult.parallelStream().skip(1).map(row -> {
            //Read attributes
            //FIXME Waiting for JDK 9
            LocalDate birthday = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.BIRTHDAY, LocalDate.class);
            LocalDate mandatsigned = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.BIRTHDAY, LocalDate.class);
            Boolean male = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.IS_MALE, Boolean.class);
            Boolean isActive = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.IS_ACTIVE, Boolean.class);
            Boolean isContributionfree = MemberGenerator.pickAndConvert(row,
                    columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.IS_CONTRIBUTIONFREE, Boolean.class);
            Integer membershipnumber = MemberGenerator.pickAndConvert(row,
                    columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.MEMBERSHIPNUMBER, Integer.class);
            String prename = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.PRENAME, String.class);
            String lastname = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.LASTNAME, String.class);
            String title = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.TITLE, String.class);
            String iban = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.IBAN, String.class);
            String bic = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.BIC, String.class);
            String accountholderPrename = MemberGenerator.pickAndConvert(row, columnMapping,
                    DBConnection.Tables.MEMBER, DBConnection.Columns.ACCOUNTHOLDER_PRENAME, String.class);
            if (accountholderPrename == null || accountholderPrename.isEmpty()) {
                accountholderPrename = prename;
            }
            String accountholderLastname = MemberGenerator.pickAndConvert(row, columnMapping,
                    DBConnection.Tables.MEMBER, DBConnection.Columns.ACCOUNTHOLDER_LASTNAME, String.class);
            if (accountholderLastname == null || accountholderLastname.isEmpty()) {
                accountholderLastname = lastname;
            }
            Double contribution = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.CONTRIBUTION, Double.class);
            String street = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.STREET, String.class);
            String housenumber = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.HOUSENUMBER, String.class);
            String cityCode = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.CITY_CODE, String.class);
            String city = MemberGenerator.pickAndConvert(
                    row, columnMapping, DBConnection.Tables.MEMBER, DBConnection.Columns.CITY, String.class);

            //Connect attributes
            Person p = new Person(lastname, lastname, title, birthday, male);
            //FIXME mandateChanged has not to be always false
            AccountHolder ah = new AccountHolder(
                    iban, bic, mandatsigned, false, accountholderPrename, accountholderLastname, title, birthday, male);
            Address ad = new Address(street, housenumber, cityCode, city);
            return new Member(membershipnumber, p, ad, ah, isActive, isContributionfree, contribution);
        }).collect(Collectors.toList());
    }
}
