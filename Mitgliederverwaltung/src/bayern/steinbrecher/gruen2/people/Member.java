/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.people;

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
     * Constructes a new member.
     *
     * @param membershipnumber The mandat number.
     * @param person The person itself.
     * @param home The homelocation.
     * @param accountHolder The owner of the account to book off the
     * contribution.
     * @param isActive {@code true} only if this member is an active one.
     * @param isContributionfree {@code true} only if this member does not have
     * to pay contribution.
     */
    public Member(int membershipnumber, Person person, Address home,
            AccountHolder accountHolder, boolean isActive,
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
     * Returns the mandat number.
     *
     * @return The mandat number.
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
        if (comp == this) {
            return true;
        }
        return comp instanceof Member && this.membershipnumber == ((Member) comp).membershipnumber;
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
