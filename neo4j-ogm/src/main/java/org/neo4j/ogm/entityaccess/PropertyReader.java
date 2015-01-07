package org.neo4j.ogm.entityaccess;

/**
 * Simple interface through which a particular property of a given object can be read.
 */
public interface PropertyReader {

    /**
     * Retrieves the property name as it would be written to the node or relationship in the graph database.
     *
     * @return The name of the property to write to the graph database property container
     */
    String propertyName();

    Object read(Object instance);



}
