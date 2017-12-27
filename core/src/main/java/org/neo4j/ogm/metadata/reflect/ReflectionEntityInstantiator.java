/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
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
