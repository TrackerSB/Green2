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
import java.util.Locale;
import java.util.Optional;

/**
 * Represents member of a Trachtenverein.
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

    static {
        COLLATOR.setStrength(Collator.SECONDARY);
    }

    /**
     * Constructs a new member.
     *
     * @param membershipnumber The mandate number.
     * @param person The person itself.
     * @param home The homelocation.
     * @param accountHolder The owner of the account to book off the contribution.
     * @param isActive {@code true} only if this member is an active one or {@code null} if the database does not
     * provide such information.
     * @param isContributionfree {@code true} only if this member does not have to pay contribution.
     * @param contribution The contribution this member has to pay. {@code null} indicates that the database has no
     * information about that.
     */
    public Member(int membershipnumber, Person person, Address home, AccountHolder accountHolder, Boolean isActive,
            boolean isContributionfree, Double contribution) {
        this.membershipnumber = membershipnumber;
        this.person = person;
        this.home = home;
        this.accountHolder = accountHolder;
        this.active = Optional.ofNullable(isActive);
        this.contributionfree = isContributionfree;
        this.contribution = Optional.ofNullable(contribution);
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
     */
    public void setMembershipnumber(int membershipnumber) {
        this.membershipnumber = membershipnumber;
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
     * Sets the person associated with this member.
     *
     * @param person The person holding some of the information about this member.
     */
    public void setPerson(Person person) {
        this.person = person;
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
     * Sets the address of this member.
     *
     * @param home The address of this member.
     */
    public void setHome(Address home) {
        this.home = home;
    }

    /**
     * Returns the holder of the account where to book off contribution.
     *
     * @return The account holder.
     */
    public AccountHolder getAccountHolder() {
        return accountHolder;
    }

    /**
     * The account holder who pays the contributions of this member.
     *
     * @param accountHolder The account holder who pays the contributions of this member.
     */
    public void setAccountHolder(AccountHolder accountHolder) {
        this.accountHolder = accountHolder;
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
     * Sets whether this member is an active or a passive member.
     *
     * @param active {@code true} only if this member is an active member.
     */
    public void setActive(Boolean active) {
        this.active = Optional.ofNullable(active);
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
     */
    public void setContributionfree(boolean contributionfree) {
        this.contributionfree = contributionfree;
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
     * Sets the contribution this member has to pay.
     *
     * @param contribution The contribution this member has to pay or {@code null} if the database does not provide
     * information about this.
     */
    public void setContribution(Double contribution) {
        this.contribution = Optional.ofNullable(contribution);
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
        return comp == this || (comp instanceof Member && this.membershipnumber == ((Member) comp).membershipnumber);
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
