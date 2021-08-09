package bayern.steinbrecher.green2.sharedBasis.data;

import bayern.steinbrecher.dbConnector.scheme.ColumnParser;
import bayern.steinbrecher.dbConnector.scheme.RegexColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.SimpleColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import bayern.steinbrecher.green2.sharedBasis.people.AddressBuilder;
import bayern.steinbrecher.green2.sharedBasis.people.Member;
import bayern.steinbrecher.green2.sharedBasis.people.MemberBuilder;
import bayern.steinbrecher.green2.sharedBasis.people.PersonBuilder;
import bayern.steinbrecher.sepaxmlgenerator.AccountHolderBuilder;
import bayern.steinbrecher.sepaxmlgenerator.BIC;
import bayern.steinbrecher.sepaxmlgenerator.DirectDebitMandateBuilder;
import bayern.steinbrecher.sepaxmlgenerator.IBAN;
import javafx.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class Tables {
    public static final TableScheme<Set<Member>, MemberTableEntryBuilder> MEMBER = new TableScheme<>(
            "Mitglieder",
            List.of(
                    new SimpleColumnPattern<>("Mitgliedsnummer",
                            ColumnParser.INTEGER_COLUMN_PARSER,
                            (mb, n) -> mb.changeMandate(DirectDebitMandateBuilder::id, Integer.toString(n))),
                    new SimpleColumnPattern<>("Vorname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, f) -> mb.changePerson(PersonBuilder::firstname, f)),
                    new SimpleColumnPattern<>("Nachname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, l) -> mb.changePerson(PersonBuilder::lastname, l)),
                    new SimpleColumnPattern<>("Titel",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, t) -> mb.changePerson(PersonBuilder::title, t)),
                    new SimpleColumnPattern<>("IstMaennlich",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (mb, m) -> mb.changePerson(PersonBuilder::male, m)),
                    new SimpleColumnPattern<>("Geburtstag",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (mb, b) -> mb.changePerson(PersonBuilder::birthday, b)),
                    new SimpleColumnPattern<>("MitgliedSeit",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (mb, ms) -> mb.changeMember(MemberBuilder::memberSince, ms)),
                    new SimpleColumnPattern<>("Strasse",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, s) -> mb.changeHome(AddressBuilder::street, s)),
                    new SimpleColumnPattern<>("Hausnummer",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, h) -> mb.changeHome(AddressBuilder::houseNumber, h)),
                    new SimpleColumnPattern<>("PLZ",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, p) -> mb.changeHome(AddressBuilder::postcode, p)),
                    new SimpleColumnPattern<>("Ort",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, p) -> mb.changeHome(AddressBuilder::place, p)),
                    new SimpleColumnPattern<>("IstBeitragsfrei",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (mb, cf) -> mb.changeMember(MemberBuilder::contributionfree, cf),
                            Optional.of(Optional.of(false)), false, false),
                    new SimpleColumnPattern<>("Iban",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, i) -> mb.changeOwner(AccountHolderBuilder::iban, new IBAN(i))),
                    new SimpleColumnPattern<>("Bic",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, b) -> mb.changeOwner(AccountHolderBuilder::bic, new BIC(b))),
                    new SimpleColumnPattern<>("KontoinhaberVorname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, f) -> mb.changeOwner(AccountHolderBuilder::firstname, f)),
                    new SimpleColumnPattern<>("KontoinhaberNachname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (mb, l) -> mb.changeOwner(AccountHolderBuilder::lastname, l)),
                    new SimpleColumnPattern<>("MandatErstellt",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (mb, ms) -> mb.changeMandate(DirectDebitMandateBuilder::signed, ms))
            ),
            List.of(
                    new SimpleColumnPattern<>("Beitrag",
                            ColumnParser.DOUBLE_COLUMN_PARSER,
                            (mb, c) -> mb.changeMember(MemberBuilder::contribution, Optional.ofNullable(c))),
                    new SimpleColumnPattern<>("IstAktiv",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (mb, a) -> mb.changeMember(MemberBuilder::active, Optional.ofNullable(a))),
                    new SimpleColumnPattern<>("AusgetretenSeit",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (mb, ld) -> mb.changeMember(MemberBuilder::leavingDate, Optional.ofNullable(ld)),
                            Optional.of(Optional.empty()), false, false),
                    new RegexColumnPattern<>("^\\d+MitgliedGeehrt$",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            MemberTableEntryBuilder::addHonoring,
                            cn -> Integer.parseInt(cn.substring(0, cn.length() - "MitgliedGeehrt".length())))
            ),
            MemberTableEntryBuilder::new,
            ms -> ms.map(MemberTableEntryBuilder::build)
                    .collect(Collectors.toSet())
    );

    public static final TableScheme<Map<String, String>, Pair<String, String>> NICKNAMES = new TableScheme<>(
            "Spitznamen",
            List.of(
                    new SimpleColumnPattern<>("Name",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (pair, name) -> new Pair<>(name, pair.getValue()),
                            Optional.empty(), true, false),
                    new SimpleColumnPattern<>("Spitzname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (pair, nickname) -> new Pair<>(pair.getKey(), nickname))
            ),
            List.of(),
            () -> new Pair<>(null, null),
            ns -> ns.collect(Collectors.toMap(Pair::getKey, Pair::getValue))
    );

    public static Collection<TableScheme<?, ?>> SCHEMES = List.of(MEMBER, NICKNAMES);

    /**
     * This class encapsulates the creation of a {@link Member} via {@link MemberBuilder} for the following reasons:
     * <ol>
     *     <li>Use builders for all nested records</li>
     *     <li>
     *         Avoid NPEs since any member of type {@link Object} is initialized with {@code null} even if it is a
     *         record annotated with {@link io.soabase.recordbuilder.core.RecordBuilder} as well
     *     </li>
     * </ol>
     */
    public static class MemberTableEntryBuilder {
        private final MemberBuilder member = MemberBuilder.builder();
        private final DirectDebitMandateBuilder mandate = DirectDebitMandateBuilder.builder();
        private final AccountHolderBuilder owner = AccountHolderBuilder.builder();
        private final AddressBuilder address = AddressBuilder.builder();
        private final PersonBuilder person = PersonBuilder.builder();

        private MemberTableEntryBuilder() {
        }

        /**
         * NOTE Change nested records that are annotated with
         * {@link io.soabase.recordbuilder.core.RecordBuilder} via equivalent setters instead of changing them directly.
         *
         * @see #changeMandate(BiFunction, Object)
         * @see #changeOwner(BiFunction, Object)
         * @see #changeHome(BiFunction, Object)
         * @see #changePerson(BiFunction, Object)
         */
        public <V> MemberTableEntryBuilder changeMember(BiFunction<MemberBuilder, V, MemberBuilder> wither, V value) {
            wither.apply(member, value);
            return this;
        }

        public <V> MemberTableEntryBuilder changeMandate(
                BiFunction<DirectDebitMandateBuilder, V, DirectDebitMandateBuilder> wither, V value) {
            wither.apply(mandate, value);
            return this;
        }

        public <V> MemberTableEntryBuilder changeOwner(
                BiFunction<AccountHolderBuilder, V, AccountHolderBuilder> wither, V value) {
            wither.apply(owner, value);
            return this;
        }

        public <V> MemberTableEntryBuilder changeHome(BiFunction<AddressBuilder, V, AddressBuilder> wither, V value) {
            wither.apply(address, value);
            return this;
        }

        public <V> MemberTableEntryBuilder changePerson(BiFunction<PersonBuilder, V, PersonBuilder> wither, V value) {
            wither.apply(person, value);
            return this;
        }

        public MemberTableEntryBuilder addHonoring(int year, boolean wasHonored) {
            if (member.honorings() == null) {
                member.honorings(new HashMap<>());
            }
            member.honorings()
                    .put(year, wasHonored);
            return this;
        }

        public Member build() {
            member.person(person.build());
            member.home(address.build());
            if (owner.firstname().isBlank()) {
                owner.firstname(person.firstname());
            }
            if (owner.lastname().isBlank()) {
                owner.lastname(person.lastname());
            }
            mandate.owner(owner.build());
            member.mandate(mandate.build());
            return member.build();
        }
    }
}
