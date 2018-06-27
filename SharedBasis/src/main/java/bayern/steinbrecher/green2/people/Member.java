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
package bayern.steinbrecher.green2.people;

import android.support.annotation.NonNull;
import java.text.Collator;
import java.time.LocalDate;
import java.util.HashMap;
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
public class Member implements Comparable<Member> {

    private static final Collator COLLATOR = Collator.getInstance(Locale.GERMAN);
    private int membershipnumber;
    private Person person;
    private Address home;
    private AccountHolder accountHolder;
    private Optional<Boolean> active;
    private boolean contributionfree;
    private Optional<Double> contribution;
    private LocalDate memberSince;
    private Map<Integer, Boolean> honorings;

    static {
        COLLATOR.setStrength(Collator.SECONDARY);
    }

    /**
     * Returns the mandate number.
     *
     * @return The mandate number.
     */
    public int getMembershipnumber() {
        return membershipnumber;
    }

    /**
     * Sets the membershipnumer of this member.
     *
     * @param membershipnumber The membershipnumber of this member.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setMembershipnumber(int membershipnumber) {
        this.membershipnumber = membershipnumber;
        return this;
    }

    /**
     * Returns the person itself.
     *
     * @return The person representing this member.
     */
    @NonNull
    public Person getPerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }

    /**
     * Sets the person associated with this member.
     *
     * @param person The person holding some of the information about this member.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setPerson(Person person) {
        this.person = person;
        return this;
    }

    /**
     * Returns the homelocation.
     *
     * @return The homelocation.
     */
    @NonNull
    public Address getHome() {
        if (home == null) {
            home = new Address();
        }
        return home;
    }

    /**
     * Sets the address of this member.
     *
     * @param home The address of this member.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setHome(Address home) {
        this.home = home;
        return this;
    }

    /**
     * Returns the holder of the account where to book off contribution.
     *
     * @return The account holder as represented in the database.
     * @see #getAccountHolderPrename()
     * @see #getAccountHolderLastname()
     */
    @NonNull
    public AccountHolder getAccountHolder() {
        if (accountHolder == null) {
            accountHolder = new AccountHolder();
        }
        return accountHolder;
    }

    /**
     * The account holder who pays the contributions of this member.
     *
     * @param accountHolder The account holder who pays the contributions of this member.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setAccountHolder(AccountHolder accountHolder) {
        this.accountHolder = accountHolder;
        return this;
    }

    /**
     * Returns the prename of the account holder associated with this member. In contrast to
     * {@link AccountHolder#getPrename()} this method returns {@link Person#getPrename()} if the account holder has no
     * prename, which may the case if the associated account holder is this member himself.
     *
     * @return The prename of the account holder associated with this member.
     * @see #getPerson()
     */
    public String getAccountHolderPrename() {
        String accountHolderPrename = getAccountHolder().getPrename();
        if (accountHolderPrename.isEmpty()) {
            accountHolderPrename = getPerson().getPrename();
        }
        return accountHolderPrename;
    }

    /**
     * Returns the lastname of the account holder associated with this member. In contrast to
     * {@link AccountHolder#getLastname()} this method returns {@link Person#getLastname()} if the account holder has no
     * lastname, which may the case if the associated account holder is this member himself.
     *
     * @return The lastname of the account holder associated with this member.
     * @see #getPerson()
     */
    public String getAccountHolderLastname() {
        String accountHolderLastname = getAccountHolder().getLastname();
        if (accountHolderLastname.isEmpty()) {
            accountHolderLastname = getPerson().getLastname();
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
     * Checks whether this member is an active one.
     *
     * @return {@code true} only if this member is active. Returns {@link Optional#empty()} if the database does not
     * provide such information.
     */
    @NonNull
    public Optional<Boolean> isActive() {
        if (active == null) {
            active = Optional.empty();
        }
        return active;
    }

    /**
     * Sets whether this member is an active or a passive member.
     *
     * @param active {@code true} only if this member is an active member. Pass {@code null} if the database stores no
     * information about this.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setActive(Boolean active) {
        this.active = Optional.ofNullable(active);
        return this;
    }

    /**
     * Checks whether this member has to pay contribution.
     *
     * @return {@code true} only if this member has NOT to pay contribution.
     */
    public boolean isContributionfree() {
        return contributionfree;
    }

    /**
     * Sets whether this member has to pay contributions.
     *
     * @param contributionfree {@code false} only if this member has to pay contributions.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setContributionfree(boolean contributionfree) {
        this.contributionfree = contributionfree;
        return this;
    }

    /**
     * Returns the contribution of this member.
     *
     * @return The contribution of this member or {@link Optional#empty()} if the contribution is unknown. (A reason may
     * be it is not saved by the database.)
     */
    @NonNull
    public Optional<Double> getContribution() {
        if (contribution == null) {
            contribution = Optional.empty();
        }
        return contribution;
    }

    /**
     * Sets the contribution this member has to pay.
     *
     * @param contribution The contribution this member has to pay or {@code null} if the database does not provide
     * information about this.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setContribution(Double contribution) {
        this.contribution = Optional.ofNullable(contribution);
        return this;
    }

    /**
     * Returns the date since when this member is member of the association.
     *
     * @return The date since when this member is member of the association.
     */
    public LocalDate getMemberSince() {
        return memberSince;
    }

    /**
     * Changes the date since when this member is member of the association.
     *
     * @param memberSince The date since when this member is member of the association.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setMemberSince(LocalDate memberSince) {
        this.memberSince = memberSince;
        return this;
    }

    /**
     * Returns the associated honorings of this member.
     *
     * @return The associated honorings of this member.
     */
    //TODO How to make sure that no value is ever null?
    @NonNull
    public Map<Integer, Boolean> getHonorings() {
        if (honorings == null) {
            honorings = new HashMap<>();
        }
        return honorings;
    }

    /**
     * Replaces the current honorings with the given ones.
     *
     * @param honorings The honorings this member to associate with.
     * @return This member which can be used for chaining calls to setter.
     */
    public Member setHonorings(Map<Integer, Boolean> honorings) {
        if (honorings.values()
                .stream()
                .anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("null is not permitted for values in honorings.");
        }
        this.honorings = honorings;
        return this;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Member compared) {
        return COLLATOR.compare(person.getName(), compared.person.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object comp) {
        return comp == this || comp instanceof Member && this.membershipnumber == ((Member) comp).membershipnumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.membershipnumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return membershipnumber + ":\t" + person.getName();
    }
}
