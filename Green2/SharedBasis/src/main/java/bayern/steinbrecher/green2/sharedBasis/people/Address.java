package bayern.steinbrecher.green2.sharedBasis.people;

/**
 * Represents a postal address. Since it has a variety of fields it is designed to be constructed by chaining calls to
 * setter instead of a constructor.
 *
 * @author Stefan Huber
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public final class Address {

    private String street, houseNumber, postcode, place;

    private Address() {
        //A completely uninitialized person (Only to be used by the builder).
    }

    /**
     * Creates a fully initialized {@link Address}.
     *
     * @param street The street name.
     * @param houseNumber The housenumber (which may also contain letters or symbols).
     * @param postcode The postcode of the city.
     * @param place The city the {@link Address} belongs to.
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

    /**
     * Allows to build an {@link Address} stepwise.
     */
    public static final class Builder extends PeopleBuilder<Address> {

        /**
         * Creates an {@link Builder} where no initial value for {@link Address} is set.
         */
        public Builder() {
            this(new Address());
        }

        /**
         * Creates a {@link Builder} whose initial values are taken from the given {@link Address}.
         *
         * @param address The {@link Address} to take initial values from.
         */
        public Builder(Address address) {
            super(address);
        }

        /**
         * Associates a street name of the {@link Address}.
         *
         * @param street A street name to assicate the {@link Address} with.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setStreet(String street) {
            getToBuild().street = street;
            return this;
        }

        /**
         * Associates a housenumber of the {@link Address}.
         *
         * @param houseNumber A housenumber to assicate the {@link Address} with.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setHouseNumber(String houseNumber) {
            getToBuild().houseNumber = houseNumber;
            return this;
        }

        /**
         * Associates a post code of the {@link Address}.
         *
         * @param postcode A post code to assicate the {@link Address} with.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setPostcode(String postcode) {
            getToBuild().postcode = postcode;
            return this;
        }

        /**
         * Associates a city of the {@link Address}.
         *
         * @param place A city to assicate the {@link Address} with.
         * @return This builder which can be used for chaining calls to setter.
         */
        public Builder setPlace(String place) {
            getToBuild().place = place;
            return this;
        }
    }
}
