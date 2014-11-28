package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;

/**
 * Implements the logic to determine how entities should be accessed in both reading and writing scenarios.
 */
public interface ObjectAccessStrategy {

    // TODO: consider whether we should have getProperty(Write|Read)Access or just return an ObjectAccess that
    // is capable of doing both, as the ObjectAccess contract dictates
    ObjectAccess getPropertyWriteAccess(ClassInfo classInfo, String propertyName);

    ObjectAccess getRelationshipAccess(ClassInfo classInfo, String relationshipType, Object parameter);

    ObjectAccess getIterableAccess(ClassInfo classInfo, Class<?> parameterType);

}
