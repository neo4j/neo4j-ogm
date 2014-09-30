package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.Edge;
import org.graphaware.graphmodel.Vertex;

/**
 * Responsible for creating objects that correspond to components of a graph model.  Note that resultant instances will
 * not be hydrated - the {@link ObjectFactory} will only instantiate the corresponding class.
 */
public interface ObjectFactory {

    /**
     * Creates an instance of the class that is mapped to the given {@link Vertex} according to the metadata that drives
     * this {@link ObjectFactory}.
     *
     * @param vertex The {@link Vertex} for which to find the corresponding class and return an instance of it
     * @return A new instance of an object onto which the properties of the given {@link Vertex} can be mapped
     * @throws MappingException if there is no known class that maps to the given graph component
     */
    <T> T instantiateObjectMappedTo(Vertex vertex);

    /**
     * Creates an instance of the class that is mapped to the given {@link Edge} according to the metadata that drives
     * this {@link ObjectFactory}.
     *
     * @param edge The {@link Edge} for which to find the corresponding class and return an instance of it
     * @return A new instance of an object onto which the properties of the given {@link Edge} can be mapped
     * @throws MappingException if there is no known class that maps to the given graph component
     */
    <T> T instantiateObjectMappedTo(Edge edge);

}
