package org.neo4j.ogm.mapper;

/**
 * Specification for an object-model mapper, which can map a model M onto arbitrary Java objects.
 *
 * @param <M> The Model implementation
 * @author Vince Bickers
 */

public interface Mapper<M> {
    /**
     * Maps the data representation in the given Model onto an instance of <code>T</code>.
     *
     * @param type The {@link Class} defining the type to which each entity in the model should be mapped
     * @param model The {@link org.neo4j.ogm.model.GraphModel} model containing the data to map onto the object
     * @return An object of type <code>T</code> containing relevant data extracted from the given graph model
     */
    <T> Iterable<T> map(Class<T> type, M model);
}
