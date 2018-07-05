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
public final class Person {

    private String prename, lastname, title;
    private LocalDate birthday;
    private boolean male;

    private Person() {
        //A completely uninitialized person (Only to be used by the builder.
    }

    /**
     * Creates a fully initialized {@link Person}. For a description for the parameters see ther getter.
     *
     * @param prename
     * @param lastname
     * @param title
     * @param birthday
     * @param male
     */
    public Person(String prename, String lastname, String title, LocalDate birthday, boolean male) {
        this.prename = prename;
        this.lastname = lastname;
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

    /**
     * Allows to build a {@link Person} stepwise.
     */
    public static final class Builder extends PeopleBuilder<Person> {

        /**
         * Creates a {@link PersonBuilder} whith no value of {@link Person} set.
         */
        public Builder() {
            this(new Person());
        }

        /**
         * Creates a {@link PersonBuilder} whose initial values are taken from the given {@link Person}.
         *
         * @param person The {@link Person} to take the initial values from.
         */
        public Builder(Person person) {
            super(person);
        }

        /**
         * Sets a new first name.
         *
         * @param prename The new first name.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setPrename(String prename) {
            getToBuild().prename = prename;
            return this;
        }

        /**
         * Sets a new last name.
         *
         * @param lastname The new last name.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setLastname(String lastname) {
            getToBuild().lastname = lastname;
            return this;
        }

        /**
         * Sets a new title.
         *
         * @param title The new title.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setTitle(String title) {
            getToBuild().title = title;
            return this;
        }

        /**
         * Sets a new birthday.
         *
         * @param birthday the new birthday.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setBirthday(LocalDate birthday) {
            getToBuild().birthday = birthday;
            return this;
        }

        /**
         * Sets a new sex for this person.
         *
         * @param male {@code true} if this person is male.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setMale(boolean male) {
            getToBuild().male = male;
            return this;
        }
    }
}
