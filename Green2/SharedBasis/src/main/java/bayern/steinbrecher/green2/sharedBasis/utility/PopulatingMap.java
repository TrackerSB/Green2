package bayern.steinbrecher.green2.sharedBasis.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a {@link HashMap} where no entry can be put manually. All entries are lazily generated when trying to
 * access them using {@link HashMap#get(java.lang.Object)}.
 *
 * @author Stefan Huber
 * @param <K> The type of the keys.
 * @param <V> The type of the associated valued.
 * @since 2u14
 */
public class PopulatingMap<K, V> extends HashMap<K, V> {

    private final Function<K, V> populator;

    /**
     * Creates a {@link PopulatingMap} whose entries are generated automatically when trying to access them.
     *
     * @param populator The function to use for populating the map.
     */
    public PopulatingMap(Function<K, V> populator) {
        this.populator = populator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"element-type-mismatch", "unchecked"})
    public V get(Object key) {
        /*
         * NOTE Use "if containsKey(...)" instead of putIfAbsent(...) since it is lacking lazy evaluation for the second
         * argument.
         */
        if (!containsKey(key)) {
            K keyK = (K) key;
            super.put(keyK, populator.apply(keyK));
        }
        return super.get(key);
    }

    /**
     * Unsupported operation.
     *
     * @param key ignored.
     * @param defaultValue ignored.
     * @return Throws {@link UnsupportedOperationException} always.
     * @throws UnsupportedOperationException
     */
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        throw new UnsupportedOperationException("This map can not be changed manually.");
    }

    /**
     * Unsupported operation.
     *
     * @param key ignored.
     * @param value ignored.
     * @return Throws {@link UnsupportedOperationException} always.
     * @throws UnsupportedOperationException
     */
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("This map can not be changed manually.");
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("This map can not be changed manually.");
    }

    /**
     * Unsupported operation.
     *
     * @param key ignored.
     * @param value ignored.
     * @return Throws {@link UnsupportedOperationException} always.
     * @throws UnsupportedOperationException
     */
    @Override
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("This map can not be changed manually.");
    }
}
