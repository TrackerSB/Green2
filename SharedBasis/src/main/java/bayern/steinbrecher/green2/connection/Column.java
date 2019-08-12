/*
 * Copyright (C) 2019 Steinbrecher
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
package bayern.steinbrecher.green2.connection;

import bayern.steinbrecher.green2.connection.scheme.ColumnPattern;
import bayern.steinbrecher.green2.connection.scheme.Tables;

/**
 * Represents a concrete column that exists in an existing database. In contrast {@link ColumnPattern} only represents
 * patterns of columns names in a table scheme defined in {@link Tables}.
 *
 * @author Stefan Huber
 * @param <T> The type of Java objects this column represents.
 * @since 2u14
 */
public class Column<T> {

    private final String name;
    private final Class<T> columnType;

    /**
     * Creates a concrete column that exists in an existing database.
     *
     * @param name The name of the column.
     * @param columnType The class of Java objects this column represents. Since this class represents existing columns
     * this type can only be determined at runtime.
     */
    // NOTE Only DBConnection and its subclasses should be allowed to create such column objects.
    Column(String name, Class<T> columnType) {
        this.name = name;
        this.columnType = columnType;
    }

    /**
     * Returns the name of this column.
     *
     * @return The name of this column.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of Java objects that this column represents.
     *
     * @return The type of Java objects that this column represents.
     */
    public Class<T> getColumnType() {
        return columnType;
    }
}
