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

import java.text.Collator;
import java.time.LocalDate;
import java.util.Collections;
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
public final class Member implements Comparable<Member> {

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

    private Member() {
        //A completely uninitialized person (Only to be used by the builder.
    }

    /**
     * Creates a full initialized {@link Member}.
     *
     * @param membershipnumber The unique membership number.
     * @param person The {@link Person} representing this {@link Member}.
     * @param home The home address.
     * @param accountHolder The person to book off contributions, costs etc.
     * @param active {@code true} only if this is a active {@link Member}.
     * @param contributionfree {@code true} only if this {@link Member} does not have to pay any contributions.
     * @param contribution The contribution associated.
     * @param memberSince The date when its membership was accepted.
     * @param honorings The honorings for years of membership gotten.
     */
    public Member(int membershipnumber, Person person, Address home, AccountHolder accountHolder,
            Optional<Boolean> active, boolean contributionfree, Optional<Double> contribution, LocalDate memberSince,
            Map<Integer, Boolean> honorings) {
        this.membershipnumber = membershipnumber;
        this.person = person;
        this.home = home;
        this.accountHolder = accountHolder;
        this.active = active;
        this.contributionfree = contributionfree;
        this.contribution = contribution;
        this.memberSince = memberSince;
        this.honorings = honorings;
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
     * Returns the person itself.
     *
     * @return The person representing this member.
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Returns the homelocation.
     *
     * @return The homelocation.
     */
    public Address getHome() {
        return home;
    }

    /**
     * Returns the holder of the account where to book off contribution.
     *
     * @return The account holder as represented in the database.
     * @see #getAccountHolderPrename()
     * @see #getAccountHolderLastname()
     */
    public AccountHolder getAccountHolder() {
        return accountHolder;
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
    public Optional<Boolean> isActive() {
        return active;
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
     * Returns the contribution of this member.
     *
     * @return The contribution of this member or {@link Optional#empty()} if the contribution is unknown. (A reason may
     * be it is not saved by the database.)
     */
    public Optional<Double> getContribution() {
        return contribution;
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
     * Returns the associated honorings of this member.
     *
     * @return The associated honorings of this member. This {@link Map} is read-only.
     */
    public Map<Integer, Boolean> getHonorings() {
        return Collections.unmodifiableMap(honorings);
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

    /**
     * Allows to build a {@link Member} stepwise.
     */
    public static final class Builder extends PeopleBuilder<Member> {

        private final Person.Builder person;
        private final Address.Builder home;
        private final AccountHolder.Builder accountHolder;

        /**
         * Creates a builder with no value of {@link Member} set.
         */
        public Builder() {
            this(new Member());
        }

        /**
         * Creates a builder storing all current values of the given {@link Member}. This may be used to create modified
         * copies.
         *
         * @param member The {@link Member} holding the initial values for this builder.
         */
        public Builder(Member member) {
            super(member);
            person = new Person.Builder(member.getPerson());
            home = new Address.Builder(member.getHome());
            accountHolder = new AccountHolder.Builder(member.getAccountHolder());
        }

        /**
         * Sets the membershipnumer of this member.
         *
         * @param membershipnumber The membershipnumber of this member.
         * @return This member which can be used for chaining calls to setter.
         */
        public Builder setMembershipnumber(int membershipnumber) {
            getToBuild().membershipnumber = membershipnumber;
            return this;
        }

        /**
         * Returns the {@link Person} to set.
         *
         * @return The {@link Person} to set.
         */
        public Person.Builder getPerson() {
            return person;
        }

        /**
         * Returns the builder for the home address.
         *
         * @return The builder for the home address.
         */
        public Address.Builder getHome() {
            return home;
        }

        /**
         * Returns the builder for the {@link AccountHolder} to associate a {@link Member} with.
         *
         * @return The builder for the {@link AccountHolder} to associate a {@link Member} with.
         */
        public AccountHolder.Builder getAccountHolder() {
            return accountHolder;
        }

        /**
         * Sets whether this member is an active or a passive member.
         *
         * @param active {@code true} only if this member is an active member. Pass {@code null} if the database stores
         * no information about this.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setActive(Boolean active) {
            getToBuild().active = Optional.ofNullable(active);
            return this;
        }

        /**
         * Sets whether this member has to pay contributions.
         *
         * @param contributionfree {@code false} only if this member has to pay contributions.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setContributionfree(boolean contributionfree) {
            getToBuild().contributionfree = contributionfree;
            return this;
        }

        /**
         * Sets the contribution this member has to pay.
         *
         * @param contribution The contribution this member has to pay or {@code null} if the database does not provide
         * information about this.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setContribution(Double contribution) {
            getToBuild().contribution = Optional.ofNullable(contribution);
            return this;
        }

        /**
         * Changes the date since when this member is member of the association.
         *
         * @param memberSince The date since when this member is member of the association.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setMemberSince(LocalDate memberSince) {
            getToBuild().memberSince = memberSince;
            return this;
        }

        /**
         * Replaces the current honorings with the given ones.
         *
         * @param honorings The honorings this member to associate with.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setHonorings(Map<Integer, Boolean> honorings) {
            if (honorings.values()
                    .stream()
                    .anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("null is not permitted for values in honorings.");
            }
            getToBuild().honorings = honorings;
            return this;
        }

        /**
         * Sets the given entry to the map of honorings. It replaces a value if the key already exists.
         *
         * @param yearsOfMembership The number of years of membership (key).
         * @param honored {@code true} only if the member was honored for the given number of years of membership
         * (value).
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder putHonoring(int yearsOfMembership, boolean honored) {
            getToBuild().honorings
                    .put(yearsOfMembership, honored);
            return this;
        }
    }
}
