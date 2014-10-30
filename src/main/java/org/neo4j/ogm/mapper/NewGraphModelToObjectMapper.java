package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;

/**
 * Specification for an object-graph mapper, which can map {@link org.graphaware.graphmodel.neo4j.GraphModel}s onto arbitrary Java objects.
 *
 * @param <G> The Graph implementation
 */
public interface NewGraphModelToObjectMapper<G extends GraphModel> {

    /**
     * Maps the data representation in the given {@link org.graphaware.graphmodel.neo4j.GraphModel} onto an instance of <code>T</code>.
     *
     * @param graphModel The {@link org.graphaware.graphmodel.neo4j.GraphModel} model containing the data to map onto the object
     * @return An object of type <code>T</code> containing relevant data extracted from the given graph model
     */
    <T> T load(Class<T> type, G graphModel);

}
