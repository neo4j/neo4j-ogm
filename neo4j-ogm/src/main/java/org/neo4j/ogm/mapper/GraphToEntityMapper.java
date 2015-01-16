package org.neo4j.ogm.mapper;

import org.neo4j.ogm.model.GraphModel;

import java.util.Collection;

/**
 * Specification for an object-graph mapper, which can map {@link org.neo4j.ogm.model.GraphModel}s onto arbitrary Java objects.
 *
 * @param <G> The Graph implementation
 */
public interface GraphToEntityMapper<G extends GraphModel> {

    /**
     * Maps the data representation in the given {@link org.neo4j.ogm.model.GraphModel} onto an instance of <code>T</code>.
     *
     * @param graphModel The {@link org.neo4j.ogm.model.GraphModel} model containing the data to map onto the object
     * @return An object of type <code>T</code> containing relevant data extracted from the given graph model
     */
    <T> Collection<T> map(Class<T> type, G graphModel);

}
