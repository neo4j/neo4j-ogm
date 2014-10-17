package org.neo4j.ogm.entityaccess;

/**
 * Factory through which instances of {@link EntityAccess} can be created for particular purposes.
 */
public interface EntityAccessFactory {

    /**
     * Retrieves the {@link EntityAccess} appropriate for writing entity information for the named property.
     *
     * @param propertyName The name of a graph component property or the simple name of a class
     * @return An {@link EntityAccess} by which to write information to the entity belonging to the given property
     */
    EntityAccess forProperty(String propertyName);

    /**
     * Retrieves the {@link EntityAccess} that may be used to access the property on instances of the given type
     * that represents the object's unique ID.
     *
     * @param attributeName The name of the attribute to gain access to
     * @param type The {@link Class} denoting the type from which the ID is to be retrieved
     * @return An {@link EntityAccess} for working with the ID of instances of the given type
     */
    EntityAccess forAttributeOfType(String attributeName, Class<?> type);

    /**
     * Retrieves the {@link EntityAccess} that may be used to access the attribute on instances of the given type
     * that represents the object's unique ID.
     *
     * @param type The {@link Class} denoting the type from which the ID is to be retrieved
     * @return An {@link EntityAccess} for working with the ID of instances of the given type
     */
    EntityAccess forIdAttributeOfType(Class<?> type);

}
