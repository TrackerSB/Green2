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

/**
 * Represents a postal address.
 *
 * @author Stefan Huber
 */
public class Address {

    private final String street, houseNumber, postcode, place;

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
     * Returns the houseNumber.
     *
     * @return The houseNumber.
     */
    public String getHouseNumber() {
        return houseNumber;
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
     * Returns the village/town.
     *
     * @return The village/town.
     */
    public String getPlace() {
        return place;
    }
}
