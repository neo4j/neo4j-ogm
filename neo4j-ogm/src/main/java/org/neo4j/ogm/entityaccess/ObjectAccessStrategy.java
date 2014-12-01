package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;

/**
 * Implements the logic to determine how entities should be accessed in both reading and writing scenarios.
 */
public interface ObjectAccessStrategy {

    PropertyWriter getPropertyWriter(ClassInfo classInfo, String propertyName);

    RelationalWriter getRelationalWriter(ClassInfo classInfo, String relationshipType, Object parameter);

    RelationalWriter getIterableWriter(ClassInfo classInfo, Class<?> parameterType);

}
