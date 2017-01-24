/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
