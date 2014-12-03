package org.neo4j.ogm.entityaccess;

import java.util.Collection;

import org.neo4j.ogm.metadata.info.ClassInfo;

/**
 * Implements the logic to determine how entities should be accessed in both reading and writing scenarios.
 */
public interface ObjectAccessStrategy {

    PropertyWriter getPropertyWriter(ClassInfo classInfo, String propertyName);

    RelationalWriter getRelationalWriter(ClassInfo classInfo, String relationshipType, Object parameter);

    RelationalWriter getIterableWriter(ClassInfo classInfo, Class<?> parameterType);

    PropertyReader getPropertyReader(ClassInfo classInfo, String propertyName);

    RelationalReader getRelationalReader(ClassInfo classInfo, String relationshipType);

    PropertyReader getIdentityPropertyReader(ClassInfo classInfo);

    Collection<RelationalReader> getRelationalReaders(ClassInfo classInfo);

    Collection<PropertyReader> getPropertyReaders(ClassInfo classInfo);

}
