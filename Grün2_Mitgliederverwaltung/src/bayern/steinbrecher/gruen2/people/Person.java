package bayern.steinbrecher.gruen2.people;

import java.time.LocalDate;

/**
 * Represents a simple person.
 *
 * @author Stefan Huber
 */
public class Person {

    private final String prename, lastname, title;
    private final LocalDate birthday;
    private final boolean male;

    public Person(String prename, String lastname, String title,
            LocalDate birthday, boolean male) {
        this.prename = prename;
        this.lastname = lastname;
        this.title = title;
        this.birthday = birthday;
        this.male = male;
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

    public boolean isMale() {
        return male;
    }
}
