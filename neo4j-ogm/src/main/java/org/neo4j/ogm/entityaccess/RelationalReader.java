package org.neo4j.ogm.entityaccess;

/**
 * Specialisation of {@link PropertyReader} that also exposes the relationship type represented by the corresponding
 * object member.
 */
public interface RelationalReader {

    Object read(Object instance);

    String propertyName();

    String relationshipType();

    String relationshipDirection();

}
