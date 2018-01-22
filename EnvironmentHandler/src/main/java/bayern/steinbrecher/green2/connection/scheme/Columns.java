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
package bayern.steinbrecher.green2.connection.scheme;

import bayern.steinbrecher.green2.generator.MemberGenerator;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This enum lists all columns which a table can have.
 *
 * @param <T> The type of the value hold.
 * @author Stefan Huber
 */
public abstract class /*enum*/ Columns<T> {

    /**
     * Stores membership numbers which are ment to be unique and therefore can be used e.g. for SEPA Direct Depits.
     */
    public static final Columns<Integer> MEMBERSHIPNUMBER = new IntegerColumn("Mitgliedsnummer");
    /**
     * Stores two values: true: active, false: passive. Can be used for describing people as active/passive members.
     */
    public static final Columns<Boolean> IS_ACTIVE = new BooleanColumn("IstAktiv");
    /**
     * The prename of a person.
     */
    public static final Columns<String> PRENAME = new StringColumn("Vorname");
    /**
     * The lastname of a person.
     */
    public static final Columns<String> LASTNAME = new StringColumn("Nachname");
    /**
     * The title of a person like Dr or B.Sc.
     */
    public static final Columns<String> TITLE = new StringColumn("Titel");
    /**
     * Stores the sex of a person. true: male, false: female.
     */
    public static final Columns<Boolean> IS_MALE = new BooleanColumn("IstMaennlich");
    /**
     * The birthday of a person.
     */
    public static final Columns<LocalDate> BIRTHDAY = new LocalDateColumn("Geburtstag");
    /**
     * The streetname of an address.
     */
    public static final Columns<String> STREET = new StringColumn("Strasse");
    /**
     * The housenumber of an address.
     */
    public static final Columns<String> HOUSENUMBER = new StringColumn("Hausnummer");
    /**
     * The city code of an address.
     */
    public static final Columns<String> CITY_CODE = new StringColumn("PLZ");
    /**
     * The name of the city of the city code of an address.
     *
     * @see #CITY_CODE
     */
    public static final Columns<String> CITY = new StringColumn("Ort");
    /**
     * Stores whether a person has to pay contributions e.g. to an association.
     */
    public static final Columns<Boolean> IS_CONTRIBUTIONFREE = new BooleanColumn("IstBeitragsfrei", false);
    /**
     * The IBAN of a bank account.
     */
    public static final Columns<String> IBAN = new StringColumn("Iban");
    /**
     * The BIC of a bank account.
     */
    public static final Columns<String> BIC = new StringColumn("Bic");
    /**
     * The prename of an owner of a bank account.
     */
    public static final Columns<String> ACCOUNTHOLDER_PRENAME = new StringColumn("KontoinhaberVorname");
    /**
     * The lastname of an owner of a bank account.
     */
    public static final Columns<String> ACCOUNTHOLDER_LASTNAME = new StringColumn("KontoinhaberNachname");
    /**
     * The date when an owner of a bank account allowed to include his/her account in SEPA Direct Debits.
     */
    public static final Columns<LocalDate> MANDAT_SIGNED = new LocalDateColumn("MandatErstellt");
    /**
     * Represents a contribution.
     */
    public static final Columns<Double> CONTRIBUTION = new DoubleColumn("Beitrag");
    /**
     * A general name where not specified whether it is pername, lastname or both.
     *
     * @see #PRENAME
     * @see #LASTNAME
     */
    public static final Columns<String> NAME = new StringColumn("Name");
    /**
     * The nickname of a person.
     */
    public static final Columns<String> NICKNAME = new StringColumn("Spitzname");

    private String realColumnName;
    private T defaultValue;

    /**
     * Returns the name of this column in the database scheme.
     *
     * @return The name of this column in the database scheme.
     */
    public String getRealColumnName() {
        return realColumnName;
    }

    /**
     * Sets the name of this column in the database scheme.
     *
     * @param realColumnName The name of the column in the database scheme.
     */
    protected void setRealColumnName(String realColumnName) {
        this.realColumnName = realColumnName;
    }

    /**
     * Returns the default value to set when creating a table containing this column.
     *
     * @return The default value to set when creating a table containing this column.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the {@link String} representation of the default value suitable for SQL. NOTE: For implementation it can
     * be assumed that the default value is not null since this is handled by {@link #getDefaultValueSql()}. The default
     * implementation just calls {@link String#valueOf(java.lang.Object)}.
     *
     * @return The {@link String} representation of the default value suitable for SQL.
     * @see #getDefaultValue()
     */
    protected String getDefaultValueSqlImpl() {
        return String.valueOf(defaultValue);
    }

    /**
     * Returns the {@link String} representation of the default value suitable for SQL.
     *
     * @return The {@link String} representation of the default value suitable for SQL. If the default value is
     * {@code null} the {@link String} "NULL" (without quotes) is returned.
     * @see #getDefaultValue()
     */
    public final String getDefaultValueSql() {
        String valueSql;
        if (defaultValue == null) {
            valueSql = "NULL";
        } else {
            valueSql = getDefaultValueSqlImpl();
        }
        return valueSql;
    }

    /**
     * Sets the default value to set when creating a table containing this column.
     *
     * @param defaultValue The default value to set when creating a table containing this column. {@code null} indicates
     * to not set a default value.
     */
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Parses the given value to the appropriate type of this column if possible.
     *
     * @param value The value to parse.
     * @return The typed value represented by {@code value}.
     */
    public abstract T parse(String value);

    /**
     * Returns the generic type of the class. This method is needed since type ereasure takes place.
     *
     * @return The generic type of the class.
     */
    public abstract Class<T> getType();

    private static class StringColumn extends Columns<String> {

        public StringColumn(String realColumnName, String defaultValue) {
            setRealColumnName(realColumnName);
            setDefaultValue(defaultValue);
        }

        public StringColumn(String realColumnName) {
            this(realColumnName, null);
        }

        @Override
        protected String getDefaultValueSqlImpl() {
            return "'" + getDefaultValue() + "'";
        }

        @Override
        public String parse(String value) {
            return value;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }

    private static class IntegerColumn extends Columns<Integer> {

        public IntegerColumn(String realColumnName, Integer defaultValue) {
            setRealColumnName(realColumnName);
            setDefaultValue(defaultValue);
        }

        public IntegerColumn(String realColumnName) {
            this(realColumnName, null);
        }

        @Override
        public Integer parse(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public Class<Integer> getType() {
            return Integer.class;
        }
    }

    private static class BooleanColumn extends Columns<Boolean> {

        public BooleanColumn(String realColumnName, Boolean defaultValue) {
            setRealColumnName(realColumnName);
            setDefaultValue(defaultValue);
        }

        public BooleanColumn(String realColumnName) {
            this(realColumnName, null);
        }

        @Override
        protected String getDefaultValueSqlImpl() {
            return getDefaultValue() ? "TRUE" : "FALSE";
        }

        @Override
        public Boolean parse(String value) {
            return value.equalsIgnoreCase("1");
        }

        @Override
        public Class<Boolean> getType() {
            return Boolean.class;
        }
    }

    private static class LocalDateColumn extends Columns<LocalDate> {

        public LocalDateColumn(String realColumnName, LocalDate defaultValue) {
            setRealColumnName(realColumnName);
            setDefaultValue(defaultValue);
        }

        public LocalDateColumn(String realColumnName) {
            this(realColumnName, null);
        }

        @Override
        public LocalDate parse(String value) {
            LocalDate date = null;
            try {
                if (value == null) {
                    throw new DateTimeParseException("Can´t parse null", "null", 0);
                } else {
                    date = LocalDate.parse(value);
                }
            } catch (DateTimeParseException ex) {
                Logger.getLogger(MemberGenerator.class.getName()).log(Level.WARNING, value + " is an invalid date", ex);
            }
            return date;
        }

        @Override
        public Class<LocalDate> getType() {
            return LocalDate.class;
        }
    }

    private static class DoubleColumn extends Columns<Double> {

        public DoubleColumn(String realColumnName, Double defaultValue) {
            setRealColumnName(realColumnName);
            setDefaultValue(defaultValue);
        }

        public DoubleColumn(String realColumnName) {
            this(realColumnName, null);
        }

        @Override
        public Double parse(String value) {
            return Double.parseDouble(value);
        }

        @Override
        public Class<Double> getType() {
            return Double.class;
        }
    }
}
