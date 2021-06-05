package bayern.steinbrecher.green2.sharedBasis.people;

import bayern.steinbrecher.sepaxmlgenerator.AccountHolder;
import bayern.steinbrecher.sepaxmlgenerator.DirectDebitMandate;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents member of an association. Since it has a variety of fields it is designed to be constructed by chaining
 * calls to setter instead of a constructor.
 *
 * @author Stefan Huber
 */
@RecordBuilder
public record Member(
        Person person,
        Address home,
        DirectDebitMandate mandate,
        Optional<Boolean> active,
        boolean contributionfree,
        Optional<Double> contribution,
        LocalDate memberSince,
        Optional<LocalDate> leavingDate,
        Map<Integer, Boolean> honorings
) implements Comparable<Member> {

    private static final Collator COLLATOR = Collator.getInstance(Locale.GERMAN);

    static {
        COLLATOR.setStrength(Collator.SECONDARY);
    }

    /**
     * The application reuses the IDs of the associated mandates (unique) as membership number (unique). Thus there
     * should be 1:1 mapping between instances of {@link Member} and {@link DirectDebitMandate}.
     */
    public String membershipnumber() {
        return mandate().id();
    }

    /**
     * Returns the prename of the account holder associated with this member. In contrast to
     * {@link AccountHolder#firstname()} this method returns {@link Person#firstname()} if the account holder has no
     * firstname, which may the case if the associated account holder is this member himself.
     *
     * @return The prename of the account holder associated with this member.
     * @see #person()
     */
    public String getAccountHolderPrename() {
        String accountHolderPrename = mandate().owner().firstname();
        if (accountHolderPrename.isEmpty()) {
            accountHolderPrename = person().firstname();
        }
        return accountHolderPrename;
    }

    /**
     * Returns the lastname of the account holder associated with this member. In contrast to
     * {@link AccountHolder#lastname()} this method returns {@link Person#lastname()} if the account holder has no
     * lastname, which may the case if the associated account holder is this member himself.
     *
     * @return The lastname of the account holder associated with this member.
     * @see #person()
     */
    public String getAccountHolderLastname() {
        String accountHolderLastname = mandate().owner().lastname();
        if (accountHolderLastname.isEmpty()) {
            accountHolderLastname = person().lastname();
        }
        return accountHolderLastname;
    }

    /**
     * Returns lastname and prename of the associated account holder separated with a comma, e.g. "Doe, John".
     *
     * @return Lastname and prename of the associated account holder separated with a comma, e.g. "Doe, John".
     * @see #getAccountHolderLastname()
     * @see #getAccountHolderPrename()
     */
    public String getAccountHolderName() {
        return getAccountHolderLastname() + ", " + getAccountHolderPrename();
    }

    /**
     * Determines whether this member was honored for a specific number of years membership.
     *
     * @param years The number of years of membership to check a honoring for.
     * @return {@code true} only if this member was honored for being for {@code years} a member. Returns
     * {@link Optional#empty()} only if this member is not associated whether it was honored or not.
     */
    public Optional<Boolean> wasHonored(int years) {
        return Optional.ofNullable(honorings.getOrDefault(years, null));
    }

    @Override
    public int compareTo(Member compared) {
        return COLLATOR.compare(person.name(), compared.person.name());
    }

    @Override
    public boolean equals(Object comp) {
        return comp == this || comp instanceof Member && this.membershipnumber() == ((Member) comp).membershipnumber();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.membershipnumber());
    }

    @Override
    public String toString() {
        return membershipnumber() + ":\t" + person.name();
    }
}
