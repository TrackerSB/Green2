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
package bayern.steinbrecher.green2.utility;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Represents a {@link HashMap} which creates an empty entry fro a key whenever it is accessed over
 * {@link HashMap#get(java.lang.Object)}.
 *
 * @author Stefan Huber
 * @since 2u14
 * @param <K> The type of the key values.
 * @param <V> The type of values.
 */
public class DefaultMap<K, V> extends HashMap<K, V> {

    private final Function<K, V> entrySupplier;

    /**
     * Creates a {@link DefaultMap} which generates empty entries when accessing them using the passed {@link Function}.
     *
     * @param entrySupplier The supplier for generating new empty entries. Its input is the key to generate an empty
     * entry for.
     */
    public DefaultMap(Function<K, V> entrySupplier) {
        this.entrySupplier = entrySupplier;
    }

    /**
     * Returns the value associated with the given key or generates an empty entry, associates it with the key and
     * returns it. This method returns {@code null} if and only if the {@link Function} for generating empty entries
     * returns {@code null}.
     *
     * @param key The key to get an associated value for.
     * @return The value assocated with the given key.
     * @throws ClassCastException Thrown only if no value is associated with the key and the key is not of type
     * {@link K}.
     * @see #getEntrySupplier()
     * @see HashMap#get(java.lang.Object)
     * @see HashMap#containsKey(java.lang.Object)
     */
    @Override
    @SuppressWarnings({"element-type-mismatch", "unchecked"})
    public V get(Object key) {
        if (!containsKey(key)) {
            K keyK = (K) key;
            put(keyK, entrySupplier.apply(keyK));
        }
        return super.get(key);
    }

    /**
     * Returns the {@link Function} used for generating new empty entries.
     *
     * @return The {@link Function} used for generating new empty entries. Its input is the key to generate an empty
     * entry for.I
     */
    public Function<K, V> getEntrySupplier() {
        return entrySupplier;
    }
}
