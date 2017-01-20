/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.people;

/**
 * Represents a postal address.
 *
 * @author Stefan Huber
 */
public class Address {

    private final String street, housenumber, postcode, place;

    /**
     * Constructes a new Address.
     *
     * @param street The streetname.
     * @param housenumber The housenumber
     * @param postcode The postal code.
     * @param place The village/town.
     */
    public Address(String street, String housenumber, String postcode,
            String place) {
        this.street = street;
        this.housenumber = housenumber;
        this.postcode = postcode;
        this.place = place;
    }

    /**
     * Returns the streetname.
     *
     * @return The streetname.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Returns the housenumber.
     *
     * @return The housenumber.
     */
    public String getHousenumber() {
        return housenumber;
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
