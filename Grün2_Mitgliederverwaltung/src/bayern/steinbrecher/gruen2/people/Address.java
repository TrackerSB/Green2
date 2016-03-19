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
