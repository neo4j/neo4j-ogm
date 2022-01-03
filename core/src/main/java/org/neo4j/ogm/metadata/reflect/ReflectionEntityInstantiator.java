/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.metadata.reflect;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.EntityInstantiator;

/**
 * Simple instantiator that uses the no-arg constructor, without using property values.
 */
public class ReflectionEntityInstantiator implements EntityInstantiator {

    public ReflectionEntityInstantiator(MetaData metadata) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInstance(Class<T> clazz, Map<String, Object> propertyValues) {
        try {
            Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
        } catch (SecurityException | IllegalArgumentException | ReflectiveOperationException e) {
            throw new MappingException("Unable to find default constructor to instantiate " + clazz, e);
        }
    }
}
