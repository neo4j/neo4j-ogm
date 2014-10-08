package org.neo4j.ogm.entityaccess;

public interface EntityAccessFactory {

    /**
     * Retrieves the {@link EntityAccess} appropriate for writing entity information for the named property.
     *
     * @param propertyName The name of a graph component property or the simple name of a class
     * @return An {@link EntityAccess} by which to write information to the entity belonging to the given property
     */
    EntityAccess forProperty(String propertyName);

}
