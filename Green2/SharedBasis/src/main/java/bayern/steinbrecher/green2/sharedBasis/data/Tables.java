package bayern.steinbrecher.green2.sharedBasis.data;

import bayern.steinbrecher.dbConnector.scheme.ColumnParser;
import bayern.steinbrecher.dbConnector.scheme.RegexColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.SimpleColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.TableCreationKeywords;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import bayern.steinbrecher.green2.sharedBasis.people.Member;
import javafx.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class Tables {
    public static final TableScheme<Set<Member>, Member.Builder> MEMBER = new TableScheme<>(
            "Mitglieder",
            List.of(
                    new SimpleColumnPattern<>("Mitgliedsnummer",
                            Set.of(TableCreationKeywords.NOT_NULL, TableCreationKeywords.PRIMARY_KEY),
                            ColumnParser.INTEGER_COLUMN_PARSER,
                            Member.Builder::setMembershipnumber),
                    new SimpleColumnPattern<>("Vorname",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setPrename(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("Nachname",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setLastname(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("Titel",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setTitle(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("IstMaennlich",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setMale(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("Geburtstag",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, value) -> {
                                m.getPerson().setBirthday(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("MitgliedSeit",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.LOCALDATE_COLUMN_PARSER,
                            Member.Builder::setMemberSince),
                    new SimpleColumnPattern<>("Strasse",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getHome().setStreet(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("Hausnummer",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getHome().setHouseNumber(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("PLZ",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getHome().setPostcode(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("Ort",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getHome().setPlace(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("IstBeitragsfrei",
                            Set.of(TableCreationKeywords.NOT_NULL, TableCreationKeywords.DEFAULT),
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            Member.Builder::setContributionfree, Optional.of(Optional.of(false))),
                    new SimpleColumnPattern<>("Iban",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setIban(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("Bic",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setBic(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("KontoinhaberVorname",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setPrename(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("KontoinhaberNachname",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setLastname(value);
                                return m;
                            }),
                    new SimpleColumnPattern<>("MandatErstellt",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, value) -> {
                                m.getAccountHolder().setMandateSigned(value);
                                return m;
                            })
            ),
            List.of(
                    new SimpleColumnPattern<>("Beitrag",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.DOUBLE_COLUMN_PARSER,
                            (builder, contribution) -> builder.setContribution(Optional.ofNullable(contribution))),
                    new SimpleColumnPattern<>("IstAktiv",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (builder, active) -> builder.setActive(Optional.ofNullable(active))),
                    new SimpleColumnPattern<>("AusgetretenSeit",
                            Set.of(TableCreationKeywords.DEFAULT), ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (builder, leavingDate) -> builder.setLeavingDate(Optional.ofNullable(leavingDate)),
                            Optional.of(Optional.empty())),
                    new RegexColumnPattern<>("^\\d+MitgliedGeehrt$",
                            ColumnParser.BOOLEAN_COLUMN_PARSER, Member.Builder::putHonoring,
                            cn -> Integer.parseInt(cn.substring(0, cn.length() - "MitgliedGeehrt".length())))
            ),
            Member.Builder::new,
            ms -> ms.map(Member.Builder::generate)
                    .collect(Collectors.toSet())
    );

    public static final TableScheme<Map<String, String>, Pair<String, String>> NICKNAMES = new TableScheme<>(
            "Spitznamen",
            List.of(
                    new SimpleColumnPattern<>("Name",
                            Set.of(TableCreationKeywords.NOT_NULL, TableCreationKeywords.PRIMARY_KEY),
                            ColumnParser.STRING_COLUMN_PARSER, (pair, name) -> new Pair<>(name, pair.getValue())),
                    new SimpleColumnPattern<>("Spitzname",
                            Set.of(TableCreationKeywords.NOT_NULL), ColumnParser.STRING_COLUMN_PARSER,
                            (pair, nickname) -> new Pair<>(pair.getKey(), nickname))
            ),
            List.of(),
            () -> new Pair<>(null, null),
            ns -> ns.collect(Collectors.toMap(Pair::getKey, Pair::getValue))
    );

    public static Collection<TableScheme<?, ?>> SCHEMES = List.of(MEMBER, NICKNAMES);
}
