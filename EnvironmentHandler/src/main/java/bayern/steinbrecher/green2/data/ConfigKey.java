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
package bayern.steinbrecher.green2.data;

import bayern.steinbrecher.green2.connection.DBConnection;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Represents all options allowed to configure in Green2.
 *
 * @author Stefan Huber
 */
public enum ConfigKey {

    /**
     * Indicating whether to use SSH or not. Write "Yes" to use SSH. ("Ja" is also accepted because of legacy, but
     * should not be used.)
     */
    USE_SSH(Boolean.class) {
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> boolean isValid(T value) {
            return (getValueClass().isInstance(value)) && value != null;
        }
    },
    /**
     * The host for connecting over SSH.
     */
    SSH_HOST(String.class) {
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> boolean isValid(T value) {
            return (getValueClass().isInstance(value)) && value != null;
        }
    },
    /**
     * The host for connecting to the database.
     */
    DATABASE_HOST(String.class) {
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> boolean isValid(T value) {
            //FIXME Need to wait until Java 9 arrives
            return (getValueClass().isInstance(value)) && value != null;
        }
    },
    /**
     * The name of the database to connect to.
     */
    DATABASE_NAME(String.class) {
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> boolean isValid(T value) {
            //FIXME Need to wait until Java 9 arrives
            return (getValueClass().isInstance(value)) && value != null;
        }
    },
    /**
     * Indicates whether the generated SEPA is UTF-8 or "UTF-8 with BOM".
     */
    SEPA_USE_BOM(Boolean.class) {
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> boolean isValid(T value) {
            //FIXME Need to wait until Java 9 arrives
            return (getValueClass().isInstance(value)) && value != null;
        }
    },
    /**
     * The expression to indicate which people get birthday notifications. Like =50,=60,=70,=75,&gt;=80
     */
    BIRTHDAY_EXPRESSION(String.class) {
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> boolean isValid(T value) {
            //FIXME Need to wait until Java 9 arrives
            return (getValueClass().isInstance(value)) && BIRTHDAY_PATTERN.matcher((String) value).matches();
        }
    },
    /**
     * The charset used by the response of the ssh connection.
     */
    SSH_CHARSET(Charset.class) {
        /**
         * Checks whether the given value represents a supported {@link Charset}.
         *
         * @param value The value to check.
         * @return {@code true} only if {@code value} represents a supported {@link Charset}.
         */
        @Override
        public <T> boolean isValid(T value) {
            //FIXME Need to wait until Java 9 arrives
            //FIXME Second operand of && is redundant. Charset.forName(..) already checks support...
            return (getValueClass().isInstance(value)) && Charset.isSupported(((Charset) value).name());
        }
    },
    DBMS(Enum.class) {
        @Override
        public <T> boolean isValid(T value) {
            //FIXME Need to wait until Java 9 arrives. Then finally I can use this enum directly.
            return (getValueClass().isInstance(value)) && Arrays.asList(DBConnection.SupportedDatabase.values())
                    .contains((DBConnection.SupportedDatabase) value);
        }
    };

    /**
     * The regex to check against the value containing the birthday expression.
     */
    public static final Pattern BIRTHDAY_PATTERN
            = Pattern.compile(" *((>=?)|(<=?)|=)[1-9]\\d*(, *((>=?)|(<=?)|=)[1-9]\\d*)* *");
    //FIXME Move pattern into enum constant allowed?
    private final Class<?> valueClass;

    ConfigKey(Class<?> valueClass) {
        this.valueClass = valueClass;
    }

    /**
     * Returns the class of the value this enum constant represents.
     *
     * @return The class of the value this enum constant represents.
     */
    @Deprecated
    public Class<?> getValueClass() {
        //FIXME Need to wait until Java 9 arrives
        return valueClass;
    }

    /**
     * Checks whether the given value is valid for this ConfigKey.
     *
     * @param value The value to check.
     * @param <T> The type of the value.
     * @return {@code true} only if this value is valid for this ConfigKey.
     */
    public abstract <T> boolean isValid(T value);

    /**
     * Returns a String representation of value according to the type of the value the ConfigKey holds.
     *
     * @param value The value to convert.
     * @param <T> The type of the value the ConfigKey holds.
     * @return The String representation.
     */
    public <T> String getStringFromValue(T value) {
        //FIXME Need to wait until Java 9 arrives
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "true" : "false";
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Charset) {
            return ((Charset) value).name();
        } else if (value instanceof DBConnection.SupportedDatabase) {
            return ((DBConnection.SupportedDatabase) value).name();
        } else {
            throw new UnsupportedOperationException(value.getClass().getSimpleName() + " is not supported.");
        }
    }

    /**
     * Returns a value of the type this ConfigKey holds converting it from {@code value}. NOTE: It does NOT imply that
     * the valid is a valid value to be used as value of a ConfigKey.
     *
     * @param value The String representation to convert.
     * @param <T> The type of the value the ConfigKey holds.
     * @return The converted value.
     */
    public <T> T getValueFromString(String value) {
        //FIXME Need to wait until Java 9 arrives
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (Boolean.class.isAssignableFrom(valueClass)) {
            //FIXME "yes" legacy check
            return (T) valueClass.cast(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes"));
        } else if (String.class.isAssignableFrom(valueClass)) {
            return (T) valueClass.cast(value);
        } else if (Charset.class.isAssignableFrom(valueClass)) {
            return (T) valueClass.cast(Charset.forName(value));
        } else if (Enum.class.isAssignableFrom(valueClass)) {
            //FIXME Need to wait until Java 9 arrives
            try {
                return (T) DBConnection.SupportedDatabase.valueOf(value);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ConfigKey.class.getName())
                        .log(Level.WARNING, "Could not find SupportedDatabase {0}", value);
                return null;
            }
        } else {
            throw new UnsupportedOperationException(valueClass.getSimpleName() + " is not supported.");
        }
    }
}
