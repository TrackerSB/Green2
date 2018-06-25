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
 * Represents a postal address.
 *
 * @author Stefan Huber
 */
public class Address {

    private String street, houseNumber, postcode, place;

    /**
     * Constructs a new Address.
     *
     * @param street The street name.
     * @param houseNumber The houseNumber
     * @param postcode The postal code.
     * @param place The village/town.
     */
    public Address(String street, String houseNumber, String postcode, String place) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.postcode = postcode;
        this.place = place;
    }

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
     */
    public void setStreet(String street) {
        this.street = street;
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
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
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
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
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
     */
    public void setPlace(String place) {
        this.place = place;
    }
}
