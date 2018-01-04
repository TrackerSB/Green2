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
package bayern.steinbrecher.green2.data;

import bayern.steinbrecher.green2.connection.scheme.SupportedDatabases;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents all options allowed to configure in Green2.
 *
 * @author Stefan Huber
 * @param <T> The type of the setting hold.
 */
public abstract class /*enum*/ ProfileSettings<T> {
    //FIXME Add generic to enum when available (JDK9?)

    /**
     * Indicating whether to use SSH or not. Write "1" or "true" to use SSH.
     */
    public static final ProfileSettings<Boolean> USE_SSH = new BooleanSetting();
    /**
     * The host for connecting over SSH.
     */
    public static final ProfileSettings<String> SSH_HOST = new StringSetting();
    /**
     * The port to use for the ssh connection.
     */
    public static final ProfileSettings<Integer> SSH_PORT = new IntegerSetting();
    /**
     * The host for connecting to the database.
     */
    public static final ProfileSettings<String> DATABASE_HOST = new StringSetting();
    /**
     * The port to use for the database connection.
     */
    public static final ProfileSettings<Integer> DATABASE_PORT = new IntegerSetting();
    /**
     * The name of the database to connect to.
     */
    public static final ProfileSettings<String> DATABASE_NAME = new StringSetting();
    /**
     * Indicates whether the generated SEPA is UTF-8 or "UTF-8 with BOM".
     */
    public static final ProfileSettings<Boolean> SEPA_USE_BOM = new BooleanSetting();
    /**
     * The regex to check against the value containing the birthday expression.
     */
    public static final Pattern BIRTHDAY_PATTERN
            = Pattern.compile(" *((>=?)|(<=?)|=)[1-9]\\d*(, *((>=?)|(<=?)|=)[1-9]\\d*)* *");
    /**
     * The expression to indicate which people get birthday notifications. Like =50,=60,=70,=75,&gt;=80
     */
    public static final ProfileSettings<String> BIRTHDAY_EXPRESSION = new BirthdayFunctionSetting();
    /**
     * The charset used by the response of the ssh connection.
     */
    public static final ProfileSettings<Charset> SSH_CHARSET = new CharsetSetting();
    /**
     * The type of the SQL database. (e.g. MySQL).
     */
    public static final ProfileSettings<SupportedDatabases> DBMS = new SupportedDatabaseSetting();

    /**
     * Contains all values like an enum. NOTE: It will be removed when generic enums are added to Java.
     */
    @Deprecated(forRemoval = true, since = "2u13")
    private static final ProfileSettings<?>[] values = {USE_SSH, SSH_HOST, SSH_PORT, DATABASE_HOST, DATABASE_PORT,
        DATABASE_NAME, SEPA_USE_BOM, BIRTHDAY_EXPRESSION, SSH_CHARSET, DBMS};

    /**
     * Returns the list of all values like an enum. NOTE: It will be removed when generic enums are added to Java.
     *
     * @return The list of available "enums".
     */
    @Deprecated(forRemoval = true, since = "2u13")
    public static ProfileSettings<?>[] values() {
        return values;
    }

    /**
     * Returns the name of the given "enum".
     *
     * @param setting The "enum" to retrieve the name for.
     * @return The name of the given "enum".
     * @see Enum#name()
     */
    @Deprecated(forRemoval = true, since = "2u13")
    public static String name(ProfileSettings<?> setting) {
        return Arrays.stream(ProfileSettings.class.getFields())
                .filter(field -> {
                    try {
                        return field.get(null) == setting;
                    } catch (IllegalAccessException ex) {
                        return false;
                    }
                })
                .findAny()
                .map(Field::getName)
                .orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Returns the {@link ProfileSettings} object which has the name {@code name}. NOTE: It will be removed when Java
     * supports generic enums.
     *
     * @param name The name of the {@link ProfileSettings} object to return.
     * @return The appropriate {@link ProfileSettings} object.
     */
    @Deprecated(forRemoval = true, since = "2u13")
    public static final ProfileSettings<?> valueOf(String name) {
        Optional<Field> possibleEnum = Arrays.stream(ProfileSettings.class.getFields())
                .filter(field -> {
                    return ProfileSettings.class.isAssignableFrom(field.getType());
                })
                .filter(field -> field.getName().equalsIgnoreCase(name))
                .findAny();
        try {
            return (ProfileSettings<?>) possibleEnum.orElseThrow(IllegalArgumentException::new).get(null);
        } catch (IllegalAccessException ex) {
            throw new Error("The reimplementation based on Enum#valueOf(...) failed.", ex);
        }
    }

    /**
     * Checks whether the given value is valid for this ProfileSettings.
     *
     * @param value The value to check.
     * @return {@code true} only if this value is valid for this ProfileSettings.
     */
    public boolean isValid(T value) {
        return value != null;
    }

    /**
     * Parses the given value to an object of the type represented by this {@link ProfileSettings}. It is not checked
     * whether the result is a valid setting.
     *
     * @param value The value to parse;
     * @return The object representing the value of the setting hold by {@code value}.
     * @see #toString(T)
     * @see #isValid(java.lang.Object)
     */
    public abstract T parse(String value);

    /**
     * Returns a {@link String} representation of the given object which can be parsed to reproduce the given object.
     *
     * @param value The value to create a {@link String} for.
     * @return The {@link String} representation for which {@link #parse(java.lang.String)} returns {@code value}.
     */
    public String toString(T value) {
        return value.toString();
    }

    private static class StringSetting extends ProfileSettings<String> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String parse(String value) {
            return value;
        }
    }

    private static class IntegerSetting extends ProfileSettings<Integer> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer parse(String value) {
            return Integer.parseInt(value);
        }
    }

    private static class BooleanSetting extends ProfileSettings<Boolean> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean parse(String value) {
            //FIXME Remove legacy equals 1 check
            return value.equalsIgnoreCase("1") || Boolean.parseBoolean(value);
        }
    }

    private static class BirthdayFunctionSetting extends ProfileSettings<String> {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid(String value) {
            return BIRTHDAY_PATTERN.matcher(value).matches();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String parse(String value) {
            return value;
        }
    }

    private static class CharsetSetting extends ProfileSettings<Charset> {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid(Charset value) {
            return Charset.availableCharsets().containsValue(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Charset parse(String value) {
            return Charset.forName(value);
        }
    }

    private static class SupportedDatabaseSetting extends ProfileSettings<SupportedDatabases> {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid(SupportedDatabases value) {
            return true; //An enum of a class can only be constructed if and only if it already exists.
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SupportedDatabases parse(String value) {
            return SupportedDatabases.valueOf(value);
        }
    }
}
