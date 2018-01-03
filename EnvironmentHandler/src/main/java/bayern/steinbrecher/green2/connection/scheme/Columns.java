/*
 * Copyright (C) 2018 Steinbrecher
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

    public static final Columns<Integer> MEMBERSHIPNUMBER = new IntegerColumn("Mitgliedsnummer");
    public static final Columns<Boolean> IS_ACTIVE = new BooleanColumn("IstAktiv");
    public static final Columns<String> PRENAME = new StringColumn("Vorname");
    public static final Columns<String> LASTNAME = new StringColumn("Nachname");
    public static final Columns<String> TITLE = new StringColumn("Titel");
    public static final Columns<Boolean> IS_MALE = new BooleanColumn("IstMaennlich");
    public static final Columns<LocalDate> BIRTHDAY = new LocalDateColumn("Geburtstag");
    public static final Columns<String> STREET = new StringColumn("Strasse");
    public static final Columns<String> HOUSENUMBER = new StringColumn("Hausnummer");
    public static final Columns<String> CITY_CODE = new StringColumn("PLZ");
    public static final Columns<String> CITY = new StringColumn("Ort");
    public static final Columns<Boolean> IS_CONTRIBUTIONFREE = new BooleanColumn("IstBeitragsfrei");
    public static final Columns<String> IBAN = new StringColumn("Iban");
    public static final Columns<String> BIC = new StringColumn("Bic");
    public static final Columns<String> ACCOUNTHOLDER_PRENAME = new StringColumn("KontoinhaberVorname");
    public static final Columns<String> ACCOUNTHOLDER_LASTNAME = new StringColumn("KontoinhaberNachname");
    public static final Columns<LocalDate> MANDAT_SIGNED = new LocalDateColumn("MandatErstellt");
    public static final Columns<Double> CONTRIBUTION = new DoubleColumn("Beitrag");
    public static final Columns<String> NAME = new StringColumn("Name");
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

    private static class StringColumn extends Columns<String> {

        public StringColumn(String realColumnName, String defaultValue) {
            setRealColumnName(realColumnName);
            setDefaultValue(defaultValue);
        }

        public StringColumn(String realColumnName) {
            this(realColumnName, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String parse(String value) {
            return value;
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

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer parse(String value) {
            return Integer.parseInt(value);
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

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean parse(String value) {
            return value.equalsIgnoreCase("1");
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

        /**
         * {@inheritDoc}
         */
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
    }

    private static class DoubleColumn extends Columns<Double> {

        public DoubleColumn(String realColumnName, Double defaultValue) {
            setRealColumnName(realColumnName);
            setDefaultValue(defaultValue);
        }

        public DoubleColumn(String realColumnName) {
            this(realColumnName, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double parse(String value) {
            return Double.parseDouble(value);
        }
    }
}
