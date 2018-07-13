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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a class meant for building immutable objects like {@link Member}, {@link Address} or {@link Person}.
 * <ul>
 * <li>NOTE The current implementation does not forbid to reuse this class for other non-people classes. Subclasses
 * should have only getter methods for nested builder. These getter methods have to return a non-null builder and no
 * {@link java.util.Optional}.</li>
 * <li>NOTE Subclasses should have two constructors:
 * <ol>
 * <li>A default constructor</li>
 * <li>2. A constructor accepting a {@link T} whose value are used as initial values for this builder.</li>
 * </ol>
 * </li>
 * <li>NOTE Subclasses should be final.</li>
 * </ul>
 *
 * @author Stefan Huber
 * @param <T> The people class to build.
 */
public abstract class PeopleBuilder<T> {

    private final T toBuild;

    /**
     * Creates a {@link PeopleBuilder}.
     *
     * @param toBuild The initial object to build on.
     */
    public PeopleBuilder(T toBuild) {
        this.toBuild = toBuild;
    }

    /**
     * Returns all fields which were not set at least once using this builder.
     *
     * @return A {@link List} of all fields which were not set at least once using this builder.
     */
    protected final List<Field> getUnsetFields() {
        //TODO Implement a check to make sure all fields of {@link T} are initialized.
        return new ArrayList<>();
    }

    /**
     * Checks whether all fields needed for constructing a {@link T} object are set.
     *
     * @return {@code true} only if a {@link T} object can be constructed.
     */
    public final boolean isAllSet() {
        return getUnsetFields().isEmpty();
    }

    /**
     * Generates a {@link T} object. It is guaranteed that no field of the resulting object contains {@code null}
     * anywhere.
     *
     * @return A {@link T} object.
     * @throws IllegalStateException Only if not all attributes needed for constructing a {@link T} object are set.
     * @see #isAllSet()
     */
    public final T generate() {
        if (isAllSet()) {
            T toBuildCopy = toBuild;
            return toBuildCopy;
        } else {
            String unsetFieldsMessage = getUnsetFields()
                    .stream()
                    .map(field -> field.toGenericString())
                    .collect(Collectors.joining(
                            "\n",
                            "Can not generate since the following fields are not set via " + this.getClass() + ":\n",
                            "")
                    );
            throw new IllegalStateException(unsetFieldsMessage);
        }
    }

    /**
     * Returns the object of type {@link T} which is to be built.
     *
     * @return The object of type {@link T} which is to be built.
     */
    protected T getToBuild() {
        return toBuild;
    }
}
