package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.Graph;

/**
 * Specification for an object-graph mapper, which can map {@link Graph}s onto arbitrary Java objects.
 *
 * @param <T> The type of object onto which the graph data will be mapped
 */
public interface GraphModelToObjectMapper<T, G extends Graph> {

    /**
     * Maps the data representation in the given {@link Graph} onto an object of type <code>T</code>.
     *
     * @param graphModel The {@link Graph} model containing the data to map onto the object
     * @return An object of type <code>T</code> containing relevant data extracted from the given graph model
     */
    T mapToObject(G graphModel);

}
