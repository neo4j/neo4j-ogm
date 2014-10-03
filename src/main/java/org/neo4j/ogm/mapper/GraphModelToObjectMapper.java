package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;

/**
 * Specification for an object-graph mapper, which can map {@link GraphModel}s onto arbitrary Java objects.
 *
 * @param <G> The Graph implementation
 */
public interface GraphModelToObjectMapper<G extends GraphModel> {

    /**
     * Maps the data representation in the given {@link GraphModel} onto an instance of <code>T</code>.
     *
     * @param graphModel The {@link GraphModel} model containing the data to map onto the object
     * @return An object of type <code>T</code> containing relevant data extracted from the given graph model
     */
    Object mapToObject(G graphModel);


}
