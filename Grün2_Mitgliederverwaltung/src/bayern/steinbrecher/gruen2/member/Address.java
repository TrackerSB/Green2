package bayern.steinbrecher.gruen2.member;

/**
 * Represents a postal address.
 *
 * @author Stefan Huber
 */
public class Address {

    private final String street, housenumber, postcode, place;

    public Address(String street, String housenumber, String postcode, String place) {
        this.street = street;
        this.housenumber = housenumber;
        this.postcode = postcode;
        this.place = place;
    }

    public String getStreet() {
        return street;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getPlace() {
        return place;
    }
}
