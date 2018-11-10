package org.neo4j.ogm.driver;

import static java.util.Collections.*;

import java.util.Map;
import java.util.function.Function;

/**
 * To be used by a driver to lookup type adapters, both native to mapped and mapped to native.
 *
 * @author Michael J. Simons
 */
public final class TypeAdapterLookupDelegate {
    private final Map<Class<?>, Function> registeredTypeAdapter;

    public TypeAdapterLookupDelegate(Map<Class<?>, Function> registeredTypeAdapter) {

        this.registeredTypeAdapter = unmodifiableMap(registeredTypeAdapter);
    }

    /**
     * Retrieves an adapter for the specified class. Can be either native or mapped class.
     *
     * @param clazz The class for which an adapter is needed.
     * @return An adapter to convert an object of clazz to native or mapped, identity function if there's no adapter
     */
    public Function<Object, Object> findAdapterFor(Class<?> clazz) {
        // Look for direct match
        if (hasAdapterFor(clazz)) {
            return registeredTypeAdapter.get(clazz);
        }

        return registeredTypeAdapter.entrySet()
            .stream().filter(e -> e.getKey().isAssignableFrom(clazz))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElseGet(Function::identity);
    }

    public boolean hasAdapterFor(Class<?> clazz) {
        return this.registeredTypeAdapter.containsKey(clazz);
    }
}
