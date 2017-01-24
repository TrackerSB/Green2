/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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

    /**
     * Constructes a new person.
     *
     * @param prename The prename of the person.
     * @param lastname The lastname of the person.
     * @param title The title of the person if any.
     * @param birthday The birthday date of the person.
     * @param male {@code true} only if the person is male.
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
