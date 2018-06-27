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

/**
 * Represents a postal address. Since it has a variety of fields it is designed to be constructed by chaining calls to
 * setter instead of a constructor.
 *
 * @author Stefan Huber
 */
public class Address {

    private String street, houseNumber, postcode, place;

    /**
     * Returns the street name.
     *
     * @return The street name.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets a new street name.
     *
     * @param street The new street name.
     * @return This {@link Address} which can be used for chaining calls to setter.
     */
    public Address setStreet(String street) {
        this.street = street;
        return this;
    }

    /**
     * Returns the houseNumber.
     *
     * @return The houseNumber.
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * Sets a new house number.
     *
     * @param houseNumber The new house number.
     * @return This {@link Address} which can be used for chaining calls to setter.
     */
    public Address setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
        return this;
    }

    /**
     * Returns the postal code.
     *
     * @return The postal code.
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Changes the postcode.
     *
     * @param postcode The new postcode.
     * @return This {@link Address} which can be used for chaining calls to setter.
     */
    public Address setPostcode(String postcode) {
        this.postcode = postcode;
        return this;
    }

    /**
     * Returns the village/town.
     *
     * @return The village/town.
     */
    public String getPlace() {
        return place;
    }

    /**
     * Changes the place of the postcode.
     *
     * @param place The new place of the postcode.
     * @return This {@link Address} which can be used for chaining calls to setter.
     */
    public Address setPlace(String place) {
        this.place = place;
        return this;
    }
}
