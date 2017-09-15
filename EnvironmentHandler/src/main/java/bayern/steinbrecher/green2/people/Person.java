/* 
 * Copyright (C) 2017 Stefan Huber
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
     * Constructs a new person.
     *
     * @param prename The first name of the person.
     * @param lastName The last name of the person.
     * @param title The title of the person if any.
     * @param birthday The birthday date of the person.
     * @param male {@code true} only if the person is male.
     */
    public Person(String prename, String lastName, String title, LocalDate birthday, boolean male) {
        this.prename = prename;
        this.lastname = lastName;
        this.title = title;
        this.birthday = birthday;
        this.male = male;
    }

    /**
     * Returns the first name.
     *
     * @return The first name.
     */
    public String getPrename() {
        return prename;
    }

    /**
     * Returns the last name.
     *
     * @return The last name.
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Returns last name and first name space separated.
     *
     * @return last name and first name space separated.
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
