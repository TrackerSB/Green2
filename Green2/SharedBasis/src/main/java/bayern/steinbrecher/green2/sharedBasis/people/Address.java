package bayern.steinbrecher.green2.sharedBasis.people;

import io.soabase.recordbuilder.core.RecordBuilder;

/**
 * Represents a postal address. Since it has a variety of fields it is designed to be constructed by chaining calls to
 * setter instead of a constructor.
 *
 * @author Stefan Huber
 */
@RecordBuilder
public record Address(
        String street,
        String houseNumber,
        String postcode,
        String place
) implements AddressBuilder.With {
}
