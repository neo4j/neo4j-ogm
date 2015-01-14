package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.Collection;

/**
 * Implements the logic to determine how entities should be accessed in both reading and writing scenarios.
 */
public interface EntityAccessStrategy {

    PropertyReader getIdentityPropertyReader(ClassInfo classInfo);

    PropertyReader getPropertyReader(ClassInfo classInfo, String propertyName);
    PropertyWriter getPropertyWriter(ClassInfo classInfo, String propertyName);

    RelationalWriter getRelationalWriter(ClassInfo classInfo, String relationshipType, Object parameter);
    RelationalReader getRelationalReader(ClassInfo classInfo, String relationshipType);

    RelationalWriter getIterableWriter(ClassInfo classInfo, Class<?> parameterType);
    RelationalReader getIterableReader(ClassInfo classInfo, Class<?> parameterType);

    Collection<RelationalReader> getRelationalReaders(ClassInfo classInfo);
    Collection<PropertyReader> getPropertyReaders(ClassInfo classInfo);

    RelationalReader getEndNodeReader(ClassInfo relationshipEntityClassInfo);
    RelationalReader getStartNodeReader(ClassInfo classInfo);
}
