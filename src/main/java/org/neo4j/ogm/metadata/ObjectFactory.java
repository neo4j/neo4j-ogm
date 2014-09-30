package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.neo4j.EdgeModel;
import org.graphaware.graphmodel.neo4j.NodeModel;

/**
 * Responsible for creating objects that correspond to components of a graph model.  Note that resultant instances will not be
 * hydrated; the {@link ObjectFactory} will only instantiate the corresponding class.
 */
public interface ObjectFactory {

    /**
     * Creates an instance of the class that is mapped to the given {@link NodeModel} according to the configuration that drives
     * this {@link ObjectFactory}.
     *
     * @param node The {@link NodeModel} for which to find the corresponding class and return an instance of it
     * @return A new instance of an object onto which the properties of the given {@link NodeModel} can be mapped
     * @throws MappingException if there is no known class that maps to the given graph component
     */
    <T> T instantiateObjectMappedTo(NodeModel node);

    /**
     * Creates an instance of the class that is mapped to the given {@link EdgeModel} according to the configuration that drives
     * this {@link ObjectFactory}.
     *
     * @param edge The {@link EdgeModel} for which to find the corresponding class and return an instance of it
     * @return A new instance of an object onto which the properties of the given {@link EdgeModel} can be mapped
     * @throws MappingException if there is no known class that maps to the given graph component
     */
    <T> T instantiateObjectMappedTo(EdgeModel edge);

}
