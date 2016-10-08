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

/**
 * Represents all options allowed to configure in gruen2.conf.
 *
 * @author Stefan Huber
 */
public enum ConfigKey {

    /**
     * Indicating whether to use SSH or not. Write "Ja" to use SSH.
     */
    USE_SSH,
    /**
     * The host for connecting over SSH.
     */
    SSH_HOST,
    /**
     * The host for connecting to the database.
     */
    DATABASE_HOST,
    /**
     * The name of the database to connect to.
     */
    DATABASE_NAME,
    /**
     * The expression to indicate which people get birthday notifications. Like
     * =50,=60,=70,=75,&gt;=80
     */
    BIRTHDAY_EXPRESSION;
}
