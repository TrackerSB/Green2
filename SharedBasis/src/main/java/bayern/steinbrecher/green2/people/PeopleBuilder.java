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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a class meant for building immutable objects like {@link Member}, {@link Address} or {@link Person}.
 * <ul>
 * <li>NOTE The current implementation does not forbid to reuse this class for other non-people classes. Subclasses
 * should have only getter methods for nested builder. These getter methods have to return a non-null builder and no
 * {@link java.util.Optional}.</li>
 * <li>NOTE Subclasses should fulfill:
 * <ol>
 * <li>Declare subclass as {@code final}.</li>
 * <li>Define a default constructor.</li>
 * <li>Define constructor accepting a {@link T} whose value are used as initial values for this builder.</li>
 * <li>Make sure nested builder are initialized as non-null within constructors
 * ({@link #initializeNestedBuilder(java.lang.Object, java.util.function.Supplier, java.util.function.Function)}).</li>
 * <li>Declare nested builder as {@code final}.</li>
 * </ol>
 * </li>
 * </ul>
 *
 * @author Stefan Huber
 * @param <T> The people class to build.
 */
//TODO Is there any way to force behaviour/handling of nested builders?
public class PeopleBuilder<T> {

    private final T toBuild;

    /**
     * Creates a {@link PeopleBuilder}.
     *
     * @param toBuild The initial object to build on.
     */
    protected PeopleBuilder(T toBuild) {
        this.toBuild = Objects.requireNonNull(
                toBuild, "The initial person must not be null. You may use #initializeNestedBuilder(...).");
    }

    /**
     * Returns a {@link PeopleBuilder} for the given person.It the person is {@code null} it returns an empty builder.
     *
     * @param <P> The type of the person to build.
     * @param <B> The type of the builder.
     * @param initialPerson The initial person to use.
     * @param emptyBuilder Constructs an uninitialilzed person.
     * @param initializedBuilder Constructs an initialized person.
     * @return The builder initialized with the given person.
     */
    protected static <P, B extends PeopleBuilder<P>> B initializeNestedBuilder(
            P initialPerson, Supplier<B> emptyBuilder, Function<P, B> initializedBuilder) {
        B builder;
        if (initialPerson == null) {
            builder = emptyBuilder.get();
        } else {
            builder = initializedBuilder.apply(initialPerson);
        }
        return builder;
    }

    /**
     * Generates a {@link T} object. NOTE Make sure to call all nested builder and to return only a copy.
     *
     * @return A {@link T} object.
     */
    public T generate() {
        //TODO Force creation of a copy.
        T toBuildCopy = toBuild;
        return toBuildCopy;
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
