package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;

/**
 * Implements the logic to determine how entities should be accessed in both reading and writing scenarios.
 */
public interface ObjectAccessStrategy {

    // TODO: consider whether we should have getProperty(Write|Read)Access or just return an ObjectAccess that
    // is capable of doing both, as the ObjectAccess contract dictates
    PropertyObjectAccess getPropertyWriteAccess(ClassInfo classInfo, String propertyName);

    RelationalObjectAccess getRelationshipAccess(ClassInfo classInfo, String relationshipType, Object parameter);

    RelationalObjectAccess getIterableAccess(ClassInfo classInfo, Class<?> parameterType);

}
