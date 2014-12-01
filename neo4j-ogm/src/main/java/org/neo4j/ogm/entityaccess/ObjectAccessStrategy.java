package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;

/**
 * Implements the logic to determine how entities should be accessed in both reading and writing scenarios.
 */
public interface ObjectAccessStrategy {

    PropertyWriteAccess getPropertyWriteAccess(ClassInfo classInfo, String propertyName);

    RelationalWriteAccess getRelationshipAccess(ClassInfo classInfo, String relationshipType, Object parameter);

    RelationalWriteAccess getIterableAccess(ClassInfo classInfo, Class<?> parameterType);

}
