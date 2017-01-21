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
package bayern.steinbrecher.gruen2.data;

import java.nio.charset.Charset;

/**
 * Represents all options allowed to configure in gruen2.conf.
 *
 * @author Stefan Huber
 */
public enum ConfigKey {

    /**
     * Indicating whether to use SSH or not. Write "Yes" to use SSH. ("Ja" is
     * also accepted because of legacy, but should not be used.)
     */
    USE_SSH(Boolean.class),
    /**
     * The host for connecting over SSH.
     */
    SSH_HOST(String.class),
    /**
     * The host for connecting to the database.
     */
    DATABASE_HOST(String.class),
    /**
     * The name of the database to connect to.
     */
    DATABASE_NAME(String.class),
    /**
     * Indicates whether the generated SEPA is UTF-8 or "UTF-8 with BOM".
     */
    SEPA_USE_BOM(Boolean.class),
    /**
     * The expression to indicate which people get birthday notifications. Like
     * =50,=60,=70,=75,&gt;=80
     */
    BIRTHDAY_EXPRESSION(String.class),
    /**
     * The charset used by the response of the ssh connection.
     */
    SSH_CHARSET(Charset.class);

    private final Class<?> valueClass;

    ConfigKey(Class<?> valueClass) {
        this.valueClass = valueClass;
    }

    /**
     * Returns the class of the value this enum constant represents.
     *
     * @return The class of the value this enum constant represents.
     */
    public Class<?> getValueClass() {
        return valueClass;
    }
}
