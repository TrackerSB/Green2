package bayern.steinbrecher.gruen2.member;

import java.time.LocalDate;

/**
 * Represents a simple person.
 *
 * @author Stefan Huber
 */
public class Person {

    private final String prename, lastname, title;
    private final LocalDate birthday;
    private final boolean isMale;

    public Person(String prename, String lastname, String title,
            LocalDate birthday, boolean isMale) {
        this.prename = prename;
        this.lastname = lastname;
        this.title = title;
        this.birthday = birthday;
        this.isMale = isMale;
    }

    public String getPrename() {
        return prename;
    }

    public String getLastname() {
        return lastname;
    }

    public String getName() {
        return getLastname() + " " + getPrename();
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public boolean isIsMale() {
        return isMale;
    }
}
