/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.core.entityaccess;


import org.neo4j.ogm.core.metadata.ClassInfo;

import java.util.Collection;

/**
 * Implements the logic to determine how entities should be accessed in both reading and writing scenarios.
 *
 * @author Adam George
 * @author Luanne Misquitta
 */
public interface EntityAccessStrategy {

    PropertyReader getIdentityPropertyReader(ClassInfo classInfo);

    PropertyReader getPropertyReader(ClassInfo classInfo, String propertyName);
    PropertyWriter getPropertyWriter(ClassInfo classInfo, String propertyName);

    RelationalWriter getRelationalWriter(ClassInfo classInfo, String relationshipType, String relationshipDirection, Object parameter);
    RelationalReader getRelationalReader(ClassInfo classInfo, String relationshipType, String relationshipDirection); //TODO this method isn't used by anything other than tests?

    RelationalWriter getIterableWriter(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection);
    RelationalReader getIterableReader(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection);

    Collection<RelationalReader> getRelationalReaders(ClassInfo classInfo);
    Collection<PropertyReader> getPropertyReaders(ClassInfo classInfo);

    RelationalReader getEndNodeReader(ClassInfo relationshipEntityClassInfo);
    RelationalReader getStartNodeReader(ClassInfo classInfo);

    RelationalWriter getRelationalEntityWriter(ClassInfo classInfo, Class entityAnnotation);
}
