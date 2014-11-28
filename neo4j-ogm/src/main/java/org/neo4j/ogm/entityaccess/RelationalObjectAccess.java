package org.neo4j.ogm.entityaccess;

/**
 * Specialisation of {@link PropertyObjectAccess} that also exposes the relationship type represented by the underlying
 * property.
 */
public interface RelationalObjectAccess extends PropertyObjectAccess {

    String relationshipType();

}
