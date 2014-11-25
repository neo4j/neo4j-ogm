package org.neo4j.ogm.mapper;

import org.neo4j.graphmodel.GraphModel;

import java.util.Collection;

/**
 * Specification for an object-graph mapper, which can map {@link org.neo4j.graphmodel.GraphModel}s onto arbitrary Java objects.
 *
 * @param <G> The Graph implementation
 */
public interface GraphToObjectMapper<G extends GraphModel> {

    /**
     * Maps the data representation in the given {@link org.neo4j.graphmodel.GraphModel} onto an instance of <code>T</code>.
     *
     * @param graphModel The {@link org.neo4j.graphmodel.GraphModel} model containing the data to map onto the object
     * @return An object of type <code>T</code> containing relevant data extracted from the given graph model
     */
    <T> T load(Class<T> type, G graphModel);

    <T> Collection<T> loadAll(Class<T> type, G graphModel);

}
