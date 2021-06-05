package bayern.steinbrecher.green2.sharedBasis.people;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDate;

/**
 * Represents a simple person. Since it has a variety of fields it is designed to be constructed by chaining calls to
 * setter instead of a constructor.
 *
 * @author Stefan Huber
 */
@RecordBuilder
public record Person(
        String firstname,
        String lastname,
        String title,
        LocalDate birthday,
        boolean male
) implements PersonBuilder.With {
    public String name() {
        return String.format("%s %s", lastname(), firstname());
    }
}
