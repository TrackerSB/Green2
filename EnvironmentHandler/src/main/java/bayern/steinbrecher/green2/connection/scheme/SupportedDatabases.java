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

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This enum lists all supported databases like MySQL.
 *
 * @author Stefan Huber
 */
public enum SupportedDatabases {
    /**
     * Represents a MySQL database.
     */
    MY_SQL("My SQL", 3306,
            Map.of(
                    Keywords.DEFAULT, "DEFAULT",
                    Keywords.NOT_NULL, "NOT NULL",
                    Keywords.PRIMARY_KEY, "PRIMARY KEY"
            ),
            Map.of(
                    Boolean.class, "BOOLEAN",
                    Double.class, "FLOAT",
                    Integer.class, "INTEGER",
                    LocalDate.class, "DATE",
                    String.class, "VARCHAR(255)"
            ),
            Map.of(
                    Queries.CREATE_TABLE, "CREATE TABLE {0} ({1});",
                    Queries.GET_COLUMN_NAMES, "SELECT column_name FROM information_schema.columns "
                    + "WHERE table_schema=\"{0}\" AND table_name=\"{1}\";",
                    Queries.GET_TABLE_NAMES, "SELECT table_name FROM information_schema.tables "
                    + "WHERE table_schema=\"{0}\";"
            ));

    private final String displayName;
    private final int defaultPort;
    private final Map<Keywords, String> keywords;
    private final Map<Class<?>, String> types;
    private final Map<Queries, String> queryTemplates;

    /**
     * Creates an enum representing a supported dbms.
     *
     * @param displayName The name to display when shown to the user.
     * @param defaultPort The default port to use when no other specified.
     * @param keywords The mapping of the keywords to the database specific keywords.
     * @param types The mapping of the types to the database specific types.
     * @param createTemplate The database specific template for generating a CREATE statement of a table. Place {0}
     * where the name of the table has to be inserted and {1} where the column separated list of columns to add have to
     * be inserted.
     * @param tableExistsTemplate The database specific template for generating a statement for looking up whether a
     * given table exists. Place {0} where to insert the name of the database and {1} where to place the name of the
     * table.
     */
    private SupportedDatabases(String displayName, int defaultPort, Map<Keywords, String> keywords,
            Map<Class<?>, String> types, Map<Queries, String> queryTemplates) {
        this.displayName = displayName;
        this.defaultPort = defaultPort;
        this.keywords = keywords;
        this.types = types;
        this.queryTemplates = queryTemplates;

        String missingKeywords = keywords.keySet().stream()
                .filter(keyword -> !keywords.containsKey(keyword))
                .map(Keywords::toString)
                .collect(Collectors.joining(", "));
        if (!missingKeywords.isEmpty()) {
            Logger.getLogger(SupportedDatabases.class.getName())
                    .log(Level.WARNING, "The database {0} does not define following keywords:\n",
                            new Object[]{displayName, missingKeywords});
        }

        String missingTemplates = queryTemplates.keySet().stream()
                .filter(query -> !queryTemplates.containsKey(query))
                .map(Queries::toString)
                .collect(Collectors.joining(", "));
        if (!missingTemplates.isEmpty()) {
            Logger.getLogger(SupportedDatabases.class.getName())
                    .log(Level.WARNING, "The database {0} does not define the following query templates:\n",
                            new Object[]{displayName, missingTemplates});
        }
    }

    /**
     * Returns a list of the appropriate SQL keywords for the given ones.Keywords which are not defined by this enum are
     * skipped.
     *
     * @param keywords The keywords to lookup the appropriate SQL keyword for.
     * @param column The column for which the keywords have to be retrieved. NOTE: This parameter is currently only used
     * in the case {@code keywords} contains {@link Keywords#DEFAULT}.
     * @return A list of the appropriate SQL keywords for the given ones.
     */
    public Collection<String> getKeywords(Set<Keywords> keywords, Columns<?> column) {
        return keywords.stream()
                .map(keyword -> {
                    if (this.keywords.containsKey(keyword)) {
                        String keywordString = this.keywords.get(keyword);
                        if (keyword == Keywords.DEFAULT) {
                            assert !keywords.contains(Keywords.NOT_NULL) || column.getDefaultValue() != null :
                                    "The keyword NOT NULL is specified but the value for DEFAULT is null";
                            keywordString += " " + column.getDefaultValueSql();
                        }
                        return keywordString;
                    } else {
                        Logger.getLogger(SupportedDatabases.class.getName())
                                .log(Level.WARNING, "Keyword {0} is not defined by {1}", new Object[]{keyword, this});
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns the appropriate SQL type for the given column.
     *
     * @param <T> The type of the value hold by {@code column}.
     * @param column The column to get the type for
     * @return The SQL type representing the type of {@code column}.
     */
    public <T> String getType(Columns<T> column) {
        Class<T> type = column.getType();
        if (types.containsKey(type)) {
            return types.get(type);
        } else {
            throw new Error("For the database " + displayName + " no SQL type for type " + type + " is defined.");
        }
    }

    /**
     * Returns the template reprsenting the given query.
     *
     * @param query The query to look up a template for.
     * @param params The params to insert into the query. Superfluous params are ignored without warning.
     * @return The template reprsenting the given query.
     */
    public String getTemplate(Queries query, Object... params) {
        if (queryTemplates.containsKey(query)) {
            return MessageFormat.format(queryTemplates.get(query), params);
        } else {
            throw new Error("For the database " + displayName + " the query " + query + " is not defined.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Returns the default port of the dbms.
     *
     * @return The default port of the dbms.
     */
    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * Represents SQL keywords.
     */
    public static enum Keywords {
        /**
         * Keyword: DEFAULT.
         */
        DEFAULT,
        /**
         * Two keywords which are often used together: NOT NULL.
         */
        NOT_NULL,
        /**
         * Keyword: PRIMARY KEY.
         */
        PRIMARY_KEY;
    }

    /**
     * Represents general queries for which according to the underlying database SQL e.g. statements can be created.
     */
    public static enum Queries {
        /**
         * Creates a table with all given columns.
         */
        CREATE_TABLE,
        /**
         * Returns all column names of the given table.<br>
         * Variables:<br>
         * 0: database name<br>
         * 1: name of the table
         */
        GET_COLUMN_NAMES,
        /**
         * Returns all table names of the given database.<br>
         * Variables:<br>
         * 0: database name
         */
        GET_TABLE_NAMES;
    }
}
