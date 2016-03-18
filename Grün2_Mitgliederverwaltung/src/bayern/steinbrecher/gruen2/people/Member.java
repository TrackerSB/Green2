package bayern.steinbrecher.gruen2.people;

import java.text.Collator;
import java.util.Locale;

/**
 * Jedes Objekt dieser Klasse stellt ein Mitglied des Trachtenvereins samt
 * seinen Kontoinformationen dar.
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

    public int getMembershipnumber() {
        return membershipnumber;
    }

    public Person getPerson() {
        return person;
    }

    public Address getHome() {
        return home;
    }

    public AccountHolder getAccountHolder() {
        return accountHolder;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isContributionfree() {
        return contributionfree;
    }

    @Override
    public int compareTo(Member compared) {
        return COLLATOR.compare(person.getName(), compared.person.getName());
    }

    @Override
    public String toString() {
        return membershipnumber + ":\t" + person.getName();
    }
}
