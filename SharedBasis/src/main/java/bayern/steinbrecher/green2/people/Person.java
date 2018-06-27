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

import java.time.LocalDate;

/**
 * Represents a simple person. Since it has a variety of fields it is designed to be constructed by chaining calls to
 * setter instead of a constructor.
 *
 * @author Stefan Huber
 */
public class Person {

    private String prename, lastname, title;
    private LocalDate birthday;
    private boolean male;

    /**
     * Returns the first name.
     *
     * @return The first name.
     */
    public String getPrename() {
        return prename;
    }

    /**
     * Sets a new first name.
     *
     * @param prename The new first name.
     * @return This person which can be used for chaining calls to setter.
     */
    public Person setPrename(String prename) {
        this.prename = prename;
        return this;
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
     * Sets a new last name.
     *
     * @param lastname The new last name.
     * @return This {@link Person} which can be used for chaining calls to setter.
     */
    public Person setLastname(String lastname) {
        this.lastname = lastname;
        return this;
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
     * Sets a new title.
     *
     * @param title The new title.
     * @return This {@link Person} which can be used for chaining calls to setter.
     */
    public Person setTitle(String title) {
        this.title = title;
        return this;
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
     * Sets a new birthday.
     *
     * @param birthday the new birthday.
     * @return This {@link Person} which can be used for chaining calls to setter.
     */
    public Person setBirthday(LocalDate birthday) {
        this.birthday = birthday;
        return this;
    }

    /**
     * Checks whether this person is male.
     *
     * @return {@code true} only if this person is male.
     */
    public boolean isMale() {
        return male;
    }

    /**
     * Sets a new sex for this person.
     *
     * @param male {@code true} if this person is male.
     * @return This {@link Person} which can be used for chaining calls to setter.
     */
    public Person setMale(boolean male) {
        this.male = male;
        return this;
    }
}
