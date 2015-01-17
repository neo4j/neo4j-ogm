package org.neo4j.ogm.entityaccess;

/**
 * Specialisation of {@link PropertyWriter} that also exposes the relationship type represented by the corresponding
 * object member.
 */
public interface RelationalWriter extends PropertyWriter {

    String relationshipName();

    String relationshipDirection();
}
