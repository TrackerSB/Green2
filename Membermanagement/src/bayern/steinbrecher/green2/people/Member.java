/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.people;

import java.text.Collator;
import java.util.Locale;

/**
 * Represents member of a Trachtenverein.
 *
 * @author Stefan Huber
 */
public class Member implements Comparable<Member> {

    private static final Collator COLLATOR
            = Collator.getInstance(Locale.GERMAN);
    private final int membershipnumber;
    private final Person person;
    private final Address home;
    private final AccountHolder accountHolder;
    private final boolean active, contributionfree;

    /**
     * Constructs a new member.
     *
     * @param membershipnumber The mandate number.
     * @param person The person itself.
     * @param home The homelocation.
     * @param accountHolder The owner of the account to book off the contribution.
     * @param isActive {@code true} only if this member is an active one.
     * @param isContributionfree {@code true} only if this member does not have to pay contribution.
     */
    public Member(int membershipnumber, Person person, Address home, AccountHolder accountHolder, boolean isActive,
            boolean isContributionfree) {
        COLLATOR.setStrength(Collator.SECONDARY);
        this.membershipnumber = membershipnumber;
        this.person = person;
        this.home = home;
        this.accountHolder = accountHolder;
        this.active = isActive;
        this.contributionfree = isContributionfree;
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
     * @return The account holder.
     */
    public AccountHolder getAccountHolder() {
        return accountHolder;
    }

    /**
     * Checks whether this member is an active one.
     *
     * @return {@code true} only if this member is active.
     */
    public boolean isActive() {
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
