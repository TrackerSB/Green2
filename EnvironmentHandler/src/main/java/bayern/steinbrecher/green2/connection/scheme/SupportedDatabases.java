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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
            HashBiMap.create(Map.of(
                    Boolean.class, new SQLTypeKeyword("TINYINT", 1), //BOOLEAN is an alias for TINYIT(1)
                    Double.class, new SQLTypeKeyword("FLOAT"),
                    Integer.class, new SQLTypeKeyword("INT"), //INTEGER is an alias for INT
                    LocalDate.class, new SQLTypeKeyword("DATE"),
                    String.class, new SQLTypeKeyword("VARCHAR", 255)
            )),
            Map.of(Queries.CREATE_TABLE, "CREATE TABLE {0} ({1});",
                    Queries.GET_COLUMN_NAMES_AND_TYPES, "SELECT column_name, data_type FROM information_schema.columns "
                    + "WHERE table_schema=\"{0}\" AND table_name=\"{1}\";",
                    Queries.GET_TABLE_NAMES, "SELECT table_name FROM information_schema.tables "
                    + "WHERE table_schema=\"{0}\";"
            ),
            '`');

    private final String displayName;
    private final int defaultPort;
    private final Map<Keywords, String> keywords;
    private final BiMap<Class<?>, SQLTypeKeyword> types;
    private final Map<Queries, String> queryTemplates;
    private final char columnQuoteSymbol;

    /**
     * Creates an enum representing a supported dbms.
     *
     * @param displayName The name to display when shown to the user.
     * @param defaultPort The default port to use when no other specified.
     * @param keywords The mapping of the keywords to the database specific keywords.
     * @param types The mapping of the types to the database specific types.
     * @param queryTemplates The templates for all queries in {@link Queries#values()}. NOTE: This parameter may be
     * removed in future versions since {@code information_schema} is standardized.
     */
    private SupportedDatabases(String displayName, int defaultPort, Map<Keywords, String> keywords,
            BiMap<Class<?>, SQLTypeKeyword> types, Map<Queries, String> queryTemplates, char columnQuoteSymbol) {
        this.displayName = displayName;
        this.defaultPort = defaultPort;
        this.keywords = keywords;
        this.types = types;
        this.queryTemplates = queryTemplates;
        this.columnQuoteSymbol = columnQuoteSymbol;

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
            return types.get(type).getSqlTypeKeyword();
        } else {
            throw new Error("For the database " + displayName + " no SQL type for type " + type + " is defined.");
        }
    }

    /**
     * Returns the appropriate SQL type for the given class.
     *
     * @param sqlType The type to get a class for.
     * @return An {@link Optional} containing the {@link Class} representing the appropriate SQL type. Returns
     * {@link Optional#empty()} if and only if for {@code sqlType} no class is defined.
     * @since 2u14
     */
    public Optional<Class<?>> getType(String sqlType) {
        Optional<Class<?>> type = Optional.ofNullable(types.inverse().get(new SQLTypeKeyword(sqlType)));
        if (!type.isPresent()) {
            Logger.getLogger(SupportedDatabases.class.getName())
                    .log(Level.WARNING, "The database {0} does not define a class for SQL type {1}.",
                            new Object[]{displayName, sqlType});
        }
        return type;
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
     * Returns the given column name quoted with the database specific quote symbol. It also escapes occurrences of the
     * quote symbol within the column name. NOTE: It is not checked whether the column exists somewhere.
     *
     * @param columnName The column name to quote.
     * @return The quoted column name.
     */
    public String quoteColumnName(String columnName) {
        return columnQuoteSymbol + columnName.replaceAll(String.valueOf(columnQuoteSymbol), "\\" + columnQuoteSymbol)
                + columnQuoteSymbol;
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
     * Represents a wrapper for a {@link String} which ignores at every point the case of characters of the wrapped
     * keyword. This includes {@link Object#equals(java.lang.Object)}, {@link Comparable#compareTo(java.lang.Object)},
     * etc. NOTE: Specified parameters are ignored.
     */
    private static class SQLTypeKeyword implements Comparable<SQLTypeKeyword> {

        private final String sqlTypeKeyword;
        private final String parameter;

        /**
         * Creates a new {@link SQLTypeKeyword}.
         *
         * @param sqlTypeKeyword The keyword is always saved and handled in uppercase. This keyword must represent the
         * type saved in {@code information_schema.columns}. Be careful with aliases.
         * @param parameter Additional parameters related to the keyword. These are ignored concerning
         * {@link Object#equals(java.lang.Object)}, {@link Comparable#compareTo(java.lang.Object)}, etc.
         */
        public SQLTypeKeyword(String sqlTypeKeyword, Object... parameter) {
            this.sqlTypeKeyword = sqlTypeKeyword.toUpperCase(Locale.ROOT);
            this.parameter = parameter.length > 0
                    ? Arrays.stream(parameter)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", ", "(", ")"))
                    : "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            if (other instanceof SQLTypeKeyword) {
                return sqlTypeKeyword.equalsIgnoreCase(((SQLTypeKeyword) other).sqlTypeKeyword);
            } else {
                return false;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return sqlTypeKeyword.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(SQLTypeKeyword other) {
            return sqlTypeKeyword.compareToIgnoreCase(other.sqlTypeKeyword);
        }

        /**
         * Returns the SQL type keyword in upper case and appends a comma separated list of parameters in braces.
         *
         * @return The SQL type keyword in upper case and appends a comma separated list of parameters in braces.
         */
        public String getSqlTypeKeyword() {
            return sqlTypeKeyword + parameter;
        }
    }
}
