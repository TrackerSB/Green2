/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package bayern.steinbrecher.gruen2.data;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Represents all options allowed to configure in Grün2.
 *
 * @author Stefan Huber
 */
public enum ConfigKey {

    /**
     * Indicating whether to use SSH or not. Write "Yes" to use SSH. ("Ja" is
     * also accepted because of legacy, but should not be used.)
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
            return (getValueClass().isInstance(value)) && !((String) value).isEmpty();
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
            return (getValueClass().isInstance(value)) && !((String) value).isEmpty();
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
            return (getValueClass().isInstance(value)) && !((String) value).isEmpty();
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
     * The expression to indicate which people get birthday notifications. Like
     * =50,=60,=70,=75,&gt;=80
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
         * Checks whether the given value represents a supported {@code Charset}.
         *
         * @param value The value to check.
         * @return {@code true} only if {@code value} represents a supported {@code Charset}.
         */
        @Override
        public <T> boolean isValid(T value) {
            //FIXME Need to wait until Java 9 arrives
            //FIXME Second operand of && is redundant. Charset.forName(..) already checks support...
            return (getValueClass().isInstance(value)) && Charset.isSupported(((Charset) value).name());
        }
    };

    /**
     * The regex to check against the value containing the birthday expression.
     */
    public static final Pattern BIRTHDAY_PATTERN
            = Pattern.compile("((>=?)|(<=?)|=)[1-9]\\d*(,((>=?)|(<=?)|=)[1-9]\\d*)*");
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
     * @param <T>   The type of the value.
     * @return {@code true} only if this value is valid for this ConfigKey.
     */
    public abstract <T> boolean isValid(T value);

    /**
     * Returns a String representation of value according to the type of the value the ConfigKey holds.
     *
     * @param value The value to convert.
     * @param <T>   The type of the value the ConfigKey holds.
     * @return The String representation.
     */
    public <T> String getStringFromValue(T value) {
        //FIXME Need to wait until Java 9 arrives
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "yes" : "no";
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Charset) {
            return ((Charset) value).name();
        } else {
            throw new UnsupportedOperationException(value.getClass().getSimpleName() + " is not supported.");
        }
    }

    /**
     * Returns a value of the type this ConfigKey holds converting it from {@code value}. NOTE: It does NOT imply that the valid is a valid value to be used as value of a ConfigKey.
     *
     * @param value The String representation to convert.
     * @param <T>   The type of the value the ConfigKey holds.
     * @return The converted value.
     */
    public <T> T getValueFromString(String value) {
        //FIXME Need to wait until Java 9 arrives
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (Boolean.class.isAssignableFrom(valueClass)) {
            return (T) valueClass.cast(value.equalsIgnoreCase("yes"));
        } else if (String.class.isAssignableFrom(valueClass)) {
            return (T) valueClass.cast(value);
        } else if (Charset.class.isAssignableFrom(valueClass)) {
            return (T) valueClass.cast(Charset.forName(value));
        } else {
            throw new UnsupportedOperationException(value.getClass().getSimpleName() + " is not supported.");
        }
    }
}
