package org.neo4j.ogm.entityaccess;

/**
 * Specialisation of {@link PropertyWriteAccess} that also exposes the relationship type represented by the underlying
 * property.
 */
public interface RelationalWriteAccess extends PropertyWriteAccess {

    String relationshipType();

}
