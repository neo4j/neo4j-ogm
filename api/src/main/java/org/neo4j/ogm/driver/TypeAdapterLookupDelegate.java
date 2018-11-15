package org.neo4j.ogm.driver;

import static java.util.Collections.*;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * To be used by a driver to lookup type adapters, both native to mapped and mapped to native. This lookup wraps all
 * returned adapters to make resilient against null values.
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
    public Function<Object, Object> getAdapterFor(Class<?> clazz) {

        Function<Object, Object> adapter = findAdapterFor(clazz).orElseGet(Function::identity);
        return object -> object == null ? null : adapter.apply(object);
    }

    public boolean hasAdapterFor(Class<?> clazz) {
        return findAdapterFor(clazz).isPresent();
    }

    private Optional<Function<Object, Object>> findAdapterFor(Class<?> clazz) {

        if (this.registeredTypeAdapter.containsKey(clazz)) {
            return Optional.of(registeredTypeAdapter.get(clazz));
        } else {
            return registeredTypeAdapter.entrySet()
                .stream().filter(e -> e.getKey().isAssignableFrom(clazz))
                .findFirst()
                .map(Map.Entry::getValue);
        }
    }
}
