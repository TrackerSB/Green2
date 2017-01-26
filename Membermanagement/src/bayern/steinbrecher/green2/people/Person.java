/*
 * Copyright (c) 2017. Stefan Huber
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/.
 */
package bayern.steinbrecher.green2.people;

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

    /**
     * Constructes a new person.
     *
     * @param prename  The prename of the person.
     * @param lastname The lastname of the person.
     * @param title    The title of the person if any.
     * @param birthday The birthday date of the person.
     * @param male     {@code true} only if the person is male.
     */
    public Person(String prename, String lastname, String title,
                  LocalDate birthday, boolean male) {
        this.prename = prename;
        this.lastname = lastname;
        this.title = title;
        this.birthday = birthday;
        this.male = male;
    }

    /**
     * Returns the prename.
     *
     * @return The prename.
     */
    public String getPrename() {
        return prename;
    }

    /**
     * Returns the lastname.
     *
     * @return The lastname.
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Returns lastname and prename spaceseparated.
     *
     * @return Lastname and prename spaceseparated.
     */
    public String getName() {
        return getLastname() + " " + getPrename();
    }

    /**
     * Returns the title if any.
     *
     * @return The title if any.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the birthday.
     *
     * @return The birthday.
     */
    public LocalDate getBirthday() {
        return birthday;
    }

    /**
     * Checks whether this person is male.
     *
     * @return {@code true} only if this person is male.
     */
    public boolean isMale() {
        return male;
    }
}
