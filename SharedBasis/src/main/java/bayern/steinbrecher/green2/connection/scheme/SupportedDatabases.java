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
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
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
            HashBiMap.create(Map.of(
                    Keywords.DEFAULT, "DEFAULT",
                    Keywords.NOT_NULL, "NOT NULL",
                    Keywords.PRIMARY_KEY, "PRIMARY KEY"
            )),
            HashBiMap.create(Map.of(
                    Boolean.class, new SQLTypeKeyword("TINYINT", 1), //BOOLEAN is an alias for TINYINT(1)
                    Double.class, new SQLTypeKeyword("FLOAT"),
                    Integer.class, new SQLTypeKeyword("INT"), //INTEGER is an alias for INT
                    LocalDate.class, new SQLTypeKeyword("DATE"),
                    String.class, new SQLTypeKeyword("VARCHAR", 255)
            )),
            /*
             * NOTE To make a query work with MessageFormat placeholder like {0} which have to be quoted with single
             * quotes in the resulting String have to be quoted by doubled single quotes.
             */
            Map.of(Queries.CREATE_TABLE, "CREATE TABLE {0} ({1});",
                    Queries.GET_COLUMN_NAMES_AND_TYPES, "SELECT `column_name`, `data_type` "
                    + "FROM `information_schema`.`columns` "
                    + "WHERE `table_schema`=''{0}'' AND `table_name`=''{1}'';",
                    Queries.GET_TABLE_NAMES, "SELECT `table_name` FROM `information_schema`.`tables` "
                    + "WHERE `table_schema`=''{0}'';"
            ),
            //TODO Think about whether to "SET SQL_MODE=ANSI_QUOTES;"
            '`');

    private final String displayName;
    private final int defaultPort;
    private final BiMap<Keywords, String> keywordRepresentations;
    private final BiMap<Class<?>, SQLTypeKeyword> types;
    private final Map<Queries, String> queryTemplates;
    private final char identifierQuoteSymbol;

    /**
     * Creates an enum representing a supported dbms.
     *
     * @param displayName The name to display when shown to the user.
     * @param defaultPort The default port to use when no other specified.
     * @param keywordRepresentations The mapping of the keywords to the database specific keywords. NOTE Only use
     * resolved alias otherwise the mapping from a SQL type keyword to a class may not work since
     * {@code information_schema} stores only resolved alias.
     * @param types The mapping of the types to the database specific types.
     * @param queryTemplates The templates for all queries in {@link Queries#values()}. NOTE: This parameter may be
     * removed in future versions since {@code information_schema} is standardized.
     * @param identifierQuoteSymbol The symbol to use for quoting columns, tables,...
     */
    SupportedDatabases(String displayName, int defaultPort, BiMap<Keywords, String> keywordRepresentations,
            BiMap<Class<?>, SQLTypeKeyword> types, Map<Queries, String> queryTemplates, char identifierQuoteSymbol) {
        this.displayName = displayName;
        this.defaultPort = defaultPort;
        this.keywordRepresentations = keywordRepresentations;
        this.types = types;
        this.queryTemplates = queryTemplates;
        this.identifierQuoteSymbol = identifierQuoteSymbol;

        String missingKeywords = keywordRepresentations.keySet().stream()
                .filter(keyword -> !keywordRepresentations.containsKey(keyword))
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
     * Returns a line which can be used in a CREATE statement appropriate for this type of database.
     *
     * @param column The column for which a line should be created which can be used in CREATE statements.
     * @return A list of the appropriate SQL keywords for the given ones.
     */
    public String generateCreateLine(SimpleColumnPattern<?, ?> column) {
        String realColumnName = column.getRealColumnName();
        return column.getKeywords().stream()
                .map(keyword -> {
                    if (this.keywordRepresentations.containsKey(keyword)) {
                        StringBuilder keywordString = new StringBuilder(this.keywordRepresentations.get(keyword));
                        if (keyword == Keywords.DEFAULT) {
                            keywordString.append(' ')
                                    .append(column.getDefaultValueSql());
                        }
                        return keywordString;
                    } else {
                        Logger.getLogger(SupportedDatabases.class.getName())
                                .log(Level.WARNING, "Keyword {0} is not defined by {1}", new Object[]{keyword, this});
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" ", realColumnName + " " + getType(realColumnName), ""));
    }

    /**
     * Returns the appropriate SQL keyword for the given keyword representation.
     *
     * @param keyword The keyword to get a database specific keyword for.
     * @return The database specific keyword.
     */
    public String getKeyword(Keywords keyword) {
        if (keywordRepresentations.containsKey(keyword)) {
            return keywordRepresentations.get(keyword);
        } else {
            throw new NoSuchElementException(
                    "For the database " + displayName + " no SQL keyword for keyword " + keyword + " is defined.");
        }
    }

    /**
     * Returns the keyword representing the given database specific keyword.
     *
     * @param sqlKeyword The SQL keyword to get a {@link Keywords} from.
     * @return The representing keyword. {@link Optional#empty()} only if this database does not associate a keyword for
     * the given SQL keyword.
     */
    public Optional<Keywords> getKeyword(String sqlKeyword) {
        Optional<Keywords> keyword = Optional.ofNullable(keywordRepresentations.inverse().get(sqlKeyword));
        if (!keyword.isPresent()) {
            Logger.getLogger(SupportedDatabases.class.getName())
                    .log(Level.WARNING, "The database {0} does not define a keyword for {1}.",
                            new Object[]{displayName, sqlKeyword});
        }
        return keyword;
    }

    /**
     * Returns the appropriate SQL type keyword for the given column.
     *
     * @param <T> The type of the values hold by {@code column}.
     * @param column The column to get the type for.
     * @return The SQL type representing the type of {@code column}.
     */
    public <T> String getType(ColumnPattern<T, ?> column) {
        Class<T> type = column.getParser().getType();
        if (types.containsKey(type)) {
            return types.get(type).getSqlTypeKeyword();
        } else {
            throw new NoSuchElementException(
                    "For the database " + displayName + " no SQL type for type " + type + " is defined.");
        }
    }

    /**
     * Returns the class used for representing values of the given SQL type.
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
            throw new NoSuchElementException(
                    "For the database " + displayName + " the query " + query + " is not defined.");
        }
    }

    /**
     * Returns the given identifier quoted with the database specific quote symbol. It also escapes occurrences of the
     * quote symbol within the identifier. NOTE: It is not checked whether the column, table,... described by the
     * identifier exists somewhere.
     *
     * @param identifier The identifier to quote. If an identifier {@code first_part.second_part} contains a dot it is
     * quoted like (e.g. quoted with double quotes) {@code "first_part"."second_part"}.
     * @return The quoted identifier.
     */
    public String quoteIdentifier(String identifier) {
        return Arrays.stream(identifier.split("\\."))
                .map(
                        i -> identifierQuoteSymbol
                        + i.replaceAll(String.valueOf(identifierQuoteSymbol), "\\" + identifierQuoteSymbol)
                        + identifierQuoteSymbol
                )
                .collect(Collectors.joining("."));
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

        private final String keyword; //NOPMD - It is access over getSqlTypeKeyword()
        private final String parameter; //NOPMD - It is access over getSqlTypeKeyword()

        /**
         * Creates a new {@link SQLTypeKeyword}.
         *
         * @param keyword The keyword is always saved and handled in uppercase. This keyword must represent the type
         * saved in {@code information_schema.columns}. Be careful with aliases.
         * @param parameter Additional parameters related to the keyword. These are ignored concerning
         * {@link Object#equals(java.lang.Object)}, {@link Comparable#compareTo(java.lang.Object)}, etc.
         */
        SQLTypeKeyword(String keyword, Object... parameter) {
            this.keyword = keyword.toUpperCase(Locale.ROOT);
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
            boolean areEqual;
            if (other instanceof SQLTypeKeyword) {
                areEqual = keyword.equalsIgnoreCase(((SQLTypeKeyword) other).keyword);
            } else {
                areEqual = false;
            }
            return areEqual;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return keyword.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(SQLTypeKeyword other) {
            return keyword.compareToIgnoreCase(other.keyword);
        }

        /**
         * Returns the SQL type keyword in upper case and appends a comma separated list of parameters in braces.
         *
         * @return The SQL type keyword in upper case and appends a comma separated list of parameters in braces.
         */
        public String getSqlTypeKeyword() {
            return keyword + parameter;
        }
    }
}
