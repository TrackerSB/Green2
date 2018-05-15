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

/**
 * Represents general queries for which according to the underlying database SQL e.g. statements can be created.
 *
 * @author Stefan Huber
 */
public enum Queries {
    /**
     * Creates a table with all given columns.
     */
    CREATE_TABLE,
    /**
     * Returns all column names (first column) and their types (second column) of the given table.<br>
     * Variables:<br>
     * 0: database name<br>
     * 1: name of the table
     */
    GET_COLUMN_NAMES_AND_TYPES,
    /**
     * Returns all table names of the given database.<br>
     * Variables:<br>
     * 0: database name
     */
    GET_TABLE_NAMES;
}
