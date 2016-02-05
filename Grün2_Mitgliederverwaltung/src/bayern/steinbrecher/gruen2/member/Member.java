package bayern.steinbrecher.gruen2.member;

/**
 * Jedes Objekt dieser Klasse stellt ein Mitglied des Trachtenvereins samt
 * seinen Kontoinformationen dar.
 *
 * @author Stefan Huber
 */
public class Member implements Comparable<Member> {

    private final int membershipnumber;
    private final Person person;
    private final Address home;
    private final AccountHolder accountHolder;
    private final boolean active, contributionfree;

    public Member(int membershipnumber, Person person, Address home,
            AccountHolder accountHolder, boolean isActive,
            boolean isContributionfree) {
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
        return person.getName().compareTo(compared.person.getName());
    }

    @Override
    public String toString() {
        return membershipnumber + ":\t" + person.getName();
    }
}
