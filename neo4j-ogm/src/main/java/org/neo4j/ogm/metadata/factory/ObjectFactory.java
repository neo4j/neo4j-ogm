package org.neo4j.ogm.metadata.factory;

import org.neo4j.graphmodel.RelationshipModel;
import org.neo4j.graphmodel.NodeModel;

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
     * @throws org.neo4j.ogm.metadata.MappingException if there is no known class that maps to the given graph component
     */
    <T> T newObject(NodeModel node);

    /**
     * Creates an instance of the class that is mapped to the given {@link RelationshipModel} according to the configuration that drives
     * this {@link ObjectFactory}.
     *
     * @param relationship The {@link RelationshipModel} for which to find the corresponding class and return an instance of it
     * @return A new instance of an object onto which the properties of the given {@link RelationshipModel} can be mapped
     * @throws org.neo4j.ogm.metadata.MappingException if there is no known class that maps to the given graph component
     */
    <T> T newObject(RelationshipModel relationship);

}
