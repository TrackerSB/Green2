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

import bayern.steinbrecher.green2.people.Member;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 * This enum lists all tables and their schemes needed.
 *
 * @author Stefan Huber
 * @param <T> The type representing the whole table.
 * @param <U> The type of an entry of the table.
 * @since 2u14
 */
//TODO Wait for generic enums
public final class /* enum */ Tables<T, U> {

    /**
     * Represents the table of members.
     */
    public static final Tables<Set<Member>, Member.Builder> MEMBER = new Tables<>(
            "Mitglieder",
            List.of(
                    //TODO Is there any way to avoid passing the parser explicitely?
                    //TODO Check whether patterns with the same name are rejected
                    new SimpleColumnPattern<Integer, Member.Builder>("Mitgliedsnummer",
                            Set.of(Keywords.NOT_NULL, Keywords.PRIMARY_KEY), ColumnParser.INTEGER_COLUMN_PARSER,
                            Member.Builder::setMembershipnumber),
                    new SimpleColumnPattern<String, Member.Builder>("Vorname",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setPrename(value);
                                return m;
                            }),
                    new SimpleColumnPattern<String, Member.Builder>("Nachname",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setLastname(value);
                                return m;
                            }),
                    new SimpleColumnPattern<String, Member.Builder>("Titel",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setTitle(value);
                                return m;
                            }),
                    new SimpleColumnPattern<Boolean, Member.Builder>("IstMaennlich",
                            Set.of(Keywords.NOT_NULL), ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setMale(value);
                                return m;
                            }),
                    new SimpleColumnPattern<LocalDate, Member.Builder>("Geburtstag",
                            Set.of(Keywords.NOT_NULL), ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setBirthday(value);
                                return m;
                            }),
                    new SimpleColumnPattern<LocalDate, Member.Builder>("MitgliedSeit",
                            Set.of(Keywords.NOT_NULL), ColumnParser.LOCALDATE_COLUMN_PARSER,
                            Member.Builder::setMemberSince),
                    new SimpleColumnPattern<String, Member.Builder>("Strasse",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getHome().setStreet(value);
                                return m;
                            }),
                    new SimpleColumnPattern<String, Member.Builder>("Hausnummer",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getHome().setHouseNumber(value);
                                return m;
                            }),
                    new SimpleColumnPattern<String, Member.Builder>("PLZ",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getHome().setPostcode(value);
                                return m;
                            }),
                    new SimpleColumnPattern<String, Member.Builder>("Ort",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getHome().setPlace(value);
                                return m;
                            }),
                    new SimpleColumnPattern<Boolean, Member.Builder>("IstBeitragsfrei",
                            Set.of(Keywords.NOT_NULL, Keywords.DEFAULT), ColumnParser.BOOLEAN_COLUMN_PARSER,
                            Member.Builder::setContributionfree, Optional.of(Optional.of(false))),
                    new SimpleColumnPattern<String, Member.Builder>("Iban",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setIban(value);
                                return m;
                            }),
                    new SimpleColumnPattern<String, Member.Builder>("Bic",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setBic(value);
                                return m;
                            }),
                    new SimpleColumnPattern<String, Member.Builder>("KontoinhaberVorname",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setPrename(value);
                                return m;
                            }),
                    new SimpleColumnPattern<String, Member.Builder>("KontoinhaberNachname",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setLastname(value);
                                return m;
                            }),
                    new SimpleColumnPattern<LocalDate, Member.Builder>("MandatErstellt",
                            Set.of(Keywords.NOT_NULL), ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setMandateSigned(value);
                                return m;
                            })
            ),
            List.of(
                    new SimpleColumnPattern<Double, Member.Builder>("Beitrag",
                            Set.of(Keywords.NOT_NULL), ColumnParser.DOUBLE_COLUMN_PARSER,
                            (builder, contribution) -> builder.setContribution(Optional.ofNullable(contribution))),
                    new SimpleColumnPattern<Boolean, Member.Builder>("IstAktiv",
                            Set.of(Keywords.NOT_NULL), ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (builder, active) -> builder.setActive(Optional.ofNullable(active))),
                    new RegexColumnPattern<Boolean, Member.Builder, Integer>("^\\d+MitgliedGeehrt$",
                            ColumnParser.BOOLEAN_COLUMN_PARSER, Member.Builder::putHonoring,
                            cn -> Integer.parseInt(cn.substring(0, cn.length() - "MitgliedGeehrt".length())))
            ),
            Member.Builder::new,
            ms -> ms.map(Member.Builder::generate)
                    .collect(Collectors.toSet())
    );
    /**
     * Represents a table mapping names to nicknames.
     */
    public static final Tables<Map<String, String>, Pair<String, String>> NICKNAMES = new Tables<>(
            "Spitznamen",
            List.of(
                    new SimpleColumnPattern<String, Pair<String, String>>("Name",
                            Set.of(Keywords.NOT_NULL, Keywords.PRIMARY_KEY), ColumnParser.STRING_COLUMN_PARSER,
                            (pair, name) -> new Pair<>(name, pair.getValue())),
                    new SimpleColumnPattern<String, Pair<String, String>>("Spitzname",
                            Set.of(Keywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (pair, nickname) -> new Pair<>(pair.getKey(), nickname))
            ),
            List.of(),
            () -> new Pair<>(null, null),
            ns -> ns.collect(Collectors.toMap(Pair::getKey, Pair::getValue))
    );

    private final List<SimpleColumnPattern<?, U>> requiredColumns;
    private final List<ColumnPattern<?, U>> optionalColumns;
    private final String realTableName;
    private transient final Supplier<U> baseEntrySupplier;
    private transient final Function<Stream<U>, T> reducer;

    /**
     * Creates a representation of a scheme of a table. {@code requiredColumns} and {@code optionalColumns} are checked
     * to be free of patterns intersecting each other.
     *
     * @param realTableName The name of the table in a database.
     * @param requiredColumns The required simple column patterns.
     * @param optionalColumns The optional column patterns.
     * @param baseEntrySupplier Returns an empty entry to be populated by the content of a row.
     * @param reducer Combines multiple row representations to a representation for the whole table.
     * @see ColumnPattern#equals(java.lang.Object)
     */
    //TODO Is there any way to force duplication and intersection freedom at compile time?
    private Tables(String realTableName, List<SimpleColumnPattern<?, U>> requiredColumns,
            List<ColumnPattern<?, U>> optionalColumns, Supplier<U> baseEntrySupplier, Function<Stream<U>, T> reducer) {
        Set<ColumnPattern<?, U>> intersection = new HashSet<>(optionalColumns);
        intersection.retainAll(requiredColumns);
        if (!intersection.isEmpty()) {
            throw new AssertionError(
                    "Found a column which is marked as required as well as optional in table " + realTableName);
        }
        this.realTableName = realTableName;
        this.requiredColumns = requiredColumns;
        this.optionalColumns = optionalColumns;
        this.baseEntrySupplier = baseEntrySupplier;
        this.reducer = reducer;
    }

    /**
     * Returns all columns as a {@link Stream}. When streaming the columns anyway this method should be preffered over
     * {@link #getAllColumns()} to spare some computations.
     *
     * @return A concatted {@link Stream} of all associated required and optional columns.
     */
    public Stream<ColumnPattern<?, U>> streamAllColumns() {
        return Stream.concat(getRequiredColumns().stream(), getOptionalColumns().stream());
    }

    /**
     * Checks whether the scheme of this table contains the given column. NOTE: This does not confirm that the this
     * column exists in the real table. It only states that the scheme considers a column of this name.
     *
     * @param columnName The column to check.
     * @return {@code true} only if this table contains {@code column}.
     */
    public boolean contains(String columnName) {
        return streamAllColumns().anyMatch(cp -> cp.matches(columnName));
    }

    /**
     * Checks whether the given column is an optional column of this table.
     *
     * @param columnName The column to check.
     * @return {@code true} only if this column is an optional column of this table.
     */
    public boolean isOptional(String columnName) {
        if (contains(columnName)) {
            return getOptionalColumns()
                    .stream()
                    .anyMatch(cp -> cp.matches(columnName));
        } else {
            throw new IllegalArgumentException(columnName + " is no column of " + realTableName);
        }
    }

    private String generateCreateStatement(SupportedDatabases dbms) {
        String columnList = streamAllColumns()
                .filter(column -> column instanceof SimpleColumnPattern)
                .map(column -> (SimpleColumnPattern) column)
                .map(column -> dbms.generateCreateLine(column))
                .collect(Collectors.joining(", "));
        return dbms.getTemplate(Queries.CREATE_TABLE, getRealTableName(), columnList);
    }

    /**
     * Generates a statement for the given connection independent query.
     *
     * @param query The query to generate a statement for.
     * @param dbms The dbms to create the statement for.
     * @param databaseName The name of the database to use.
     * @return The generated statement.
     */
    //FIXME Think about where to move these generateQuery methods
    public String generateQuery(Queries query, SupportedDatabases dbms, String databaseName) {
        String statement;
        switch (query) {
            case CREATE_TABLE:
                statement = generateCreateStatement(dbms);
                break;
            case GET_COLUMN_NAMES_AND_TYPES:
                statement = dbms.getTemplate(Queries.GET_COLUMN_NAMES_AND_TYPES, databaseName, getRealTableName());
                break;
            case GET_TABLE_NAMES:
                statement = dbms.getTemplate(Queries.GET_TABLE_NAMES, databaseName);
                break;
            default:
                throw new UnsupportedOperationException("The query " + query + " is not implemented, yet.");
        }
        return statement;
    }

    /**
     * Generates a list of objects of {@link T} out of {@code queryResult}.
     *
     * @param queryResult The query result table that hold the member informations. First dimension has to be row;
     * second column. Each row is treated as one object of type {@link T}. The first row must contain the headings of
     * the columns.
     * @return The resulting object of type {@link T} represented.
     */
    public T generateRepresentations(List<List<String>> queryResult) {
        List<String> headings = queryResult.get(0);
        Map<ColumnPattern<?, U>, List<Integer>> patternToColumnMapping = streamAllColumns()
                //TODO Why is Function::identity not acccepted?
                .map(pattern -> {
                    List<Integer> targetIndices = new ArrayList<>();
                    for (int i = 0; i < headings.size(); i++) {
                        if (pattern.matches(headings.get(i))) {
                            targetIndices.add(i);
                        }
                    }
                    return new Pair<>(pattern, targetIndices);
                })
                .filter(pair -> !pair.getValue().isEmpty())
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        //Check duplicate target column indices
        //TODO Can this be replaced by an "assert" which can be disabled at compile time?
        Set<Integer> mappedTargetIndices = new HashSet<>();
        boolean isDuplicateFree = patternToColumnMapping.values()
                .stream()
                .flatMap(List::stream)
                .allMatch(mappedTargetIndices::add);
        if (!isDuplicateFree) {
            throw new AssertionError("Table " + getRealTableName() + " contains intersecting column patterns.");
        }

        return reducer.apply(queryResult.stream()
                .skip(1) //Skip headings
                .map(row -> {
                    U rowRepresentation = baseEntrySupplier.get();
                    for (Map.Entry<ColumnPattern<?, U>, List<Integer>> columnMapping
                            : patternToColumnMapping.entrySet()) {
                        ColumnPattern<?, U> pattern = columnMapping.getKey();
                        List<Integer> targetIndices = columnMapping.getValue();
                        if (targetIndices.size() <= 0) {
                            Logger.getLogger(Tables.class.getName())
                                    .log(Level.WARNING, "Pattern {0} is registered but not associated to any "
                                            + "target index.", pattern.getColumnNamePattern().pattern());
                        } else if (pattern instanceof SimpleColumnPattern<?, ?>) {
                            if (targetIndices.size() > 1) { //NOPMD - Check whether association is ambiguous.
                                Logger.getLogger(Tables.class.getName())
                                        .log(Level.WARNING, "The simple column {0} is associated to more than "
                                                + "1 target index. Only the first index is recognized.",
                                                pattern.getColumnNamePattern().pattern());
                            }
                            SimpleColumnPattern<?, U> simplePattern = (SimpleColumnPattern<?, U>) pattern;
                            rowRepresentation = simplePattern.combine(rowRepresentation, row.get(targetIndices.get(0)));
                        } else if (pattern instanceof RegexColumnPattern<?, ?, ?>) {
                            RegexColumnPattern<?, U, ?> regexPattern = (RegexColumnPattern<?, U, ?>) pattern;
                            for (Integer index : targetIndices) {
                                rowRepresentation
                                        = regexPattern.combine(rowRepresentation, headings.get(index), row.get(index));
                            }
                        } else {
                            Logger.getLogger(Tables.class.getName())
                                    .log(Level.WARNING, "Can't handle patterns of type {0}.",
                                            pattern == null ? null : pattern.getClass());
                        }
                    }
                    return rowRepresentation;
                }));
    }

    /**
     * Returns the name of the table in the database scheme.
     *
     * @return The name of the table in the database scheme.
     */
    public String getRealTableName() {
        return realTableName;
    }

    /**
     * Returns the patterns of all required columns.
     *
     * @return The patterns of all required columns.
     */
    public List<SimpleColumnPattern<?, U>> getRequiredColumns() {
        return requiredColumns;
    }

    /**
     * Returns the patterns of all optional columns.
     *
     * @return The patterns of all optional columns.
     */
    public List<ColumnPattern<?, U>> getOptionalColumns() {
        return optionalColumns;
    }

    /**
     * Returns the {@link Set} containing all columns this table can have according to its scheme. When streaming all
     * columns afterwards {@link #streamAllColumns()} should be preffered for efficiency reasons.
     *
     * @return The {@link Set} containing all columns this table can have according to its scheme.
     * @see #streamAllColumns()
     */
    public Set<ColumnPattern<?, U>> getAllColumns() {
        Set<ColumnPattern<?, U>> allColumns = new HashSet<>(getRequiredColumns());
        allColumns.addAll(getOptionalColumns());
        return allColumns;
    }

    /**
     * Returns all declared tables. NOTE This method is only needed explicitely until generic enums are introduced. At
     * that point enums {@code values()} method is going to take this functionality.
     *
     * @return All declared tables.
     */
    public static Tables<?, ?>[] values() {
        return new Tables<?, ?>[]{MEMBER, NICKNAMES};
    }
}
