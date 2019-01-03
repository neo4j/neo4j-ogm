/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.typeconversion;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.session.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MapCompositeConverter converts Map field into prefixed properties of node or relationship entity.
 * The prefix and delimiter is configurable.
 *
 * @author Frantisek Hartman
 */
public class MapCompositeConverter implements CompositeAttributeConverter<Map<?, ?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapCompositeConverter.class);
    private static final Set<Class> cypherTypes;
    private static final Set<Class> castableTypes;

    static {
        Set<Class> types = new HashSet<>();
        types.add(Boolean.class);
        types.add(Long.class);
        types.add(Double.class);
        types.add(String.class);
        types.add(List.class);
        cypherTypes = Collections.unmodifiableSet(types);

        Set<Class> castable = new HashSet<>();
        castable.add(Short.class);
        castable.add(Integer.class);
        castable.add(Float.class);
        castableTypes = unmodifiableSet(castable);
    }

    private final String prefix;
    private final String delimiter;
    private final boolean allowCast;

    private final ParameterizedType mapFieldType;
    private final String firstPart;

    /**
     * Create MapCompositeConverter
     *
     * @param prefix       prefix that is used for all properties
     * @param delimiter    delimiter that is used between prefix, properties and nested properties
     * @param allowCast    if casting from non Cypher types should be allowed
     * @param mapFieldType type information for the field
     */
    public MapCompositeConverter(String prefix, String delimiter, boolean allowCast, ParameterizedType mapFieldType) {
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.allowCast = allowCast;
        this.mapFieldType = mapFieldType;
        firstPart = prefix + delimiter;
    }

    @Override
    public Map<String, ?> toGraphProperties(Map<?, ?> fieldValue) {
        if (fieldValue == null) {
            return emptyMap();
        }
        Map<String, Object> graphProperties = new HashMap<>(fieldValue.size());
        addMapToProperties(fieldValue, graphProperties, firstPart);
        return graphProperties;
    }

    private void addMapToProperties(Map<?, ?> fieldValue, Map<String, Object> graphProperties, String prefix) {
        for (Map.Entry<?, ?> entry : fieldValue.entrySet()) {
            Object entryValue = entry.getValue();
            if (entryValue instanceof Map) {
                addMapToProperties((Map<?, ?>) entryValue, graphProperties, prefix + entry.getKey() + delimiter);
            } else {
                if (isCypherType(entryValue) ||
                    (allowCast && canCastType(entryValue))) {

                    graphProperties.put(prefix + entry.getKey(), entryValue);
                } else {
                    throw new MappingException("Could not map key=" + prefix + entry.getKey() + ", " +
                        "value=" + entryValue + " (type = " + entryValue.getClass() + ") " +
                        "because it is not a supported type.");
                }
            }
        }
    }

    private boolean canCastType(Object value) {
        return castableTypes.contains(value.getClass());
    }

    private boolean isCypherType(Object entryValue) {
        return cypherTypes.contains(entryValue.getClass()) || List.class.isAssignableFrom(entryValue.getClass());
    }

    @Override
    public Map<?, ?> toEntityAttribute(Map<String, ?> value) {

        Set<? extends Map.Entry<String, ?>> prefixedProperties = value.entrySet()
            .stream()
            .filter(entry -> entry.getKey().startsWith(firstPart))
            .collect(toSet());

        Map<Object, Object> result = new HashMap<>();
        for (Map.Entry<String, ?> entry : prefixedProperties) {
            String propertyKey = entry.getKey().substring(firstPart.length());
            putToMap(result, propertyKey, entry.getValue(), mapFieldType);
        }
        return result;
    }

    private void putToMap(Map<Object, Object> result, String propertyKey, Object value, Type fieldType) {
        if (propertyKey.contains(delimiter)) {
            int delimiterIndex = propertyKey.indexOf(delimiter);
            String key = propertyKey.substring(0, delimiterIndex);

            Object keyInstance = keyInstanceFromString(key, getKeyType(fieldType));
            Map<Object, Object> o = (Map<Object, Object>) result.get(key);
            if (o == null) {
                o = new HashMap<>();
                result.put(keyInstance, o);
            }
            putToMap(o, propertyKey.substring(delimiterIndex + delimiter.length()), value, nestedFieldType(fieldType));
        } else {
            Object keyInstance = keyInstanceFromString(propertyKey, getKeyType(fieldType));

            Type valueType = nestedFieldType(fieldType);
            if (valueType != null) {
                result.put(keyInstance, Utils.coerceTypes((Class) valueType, value));
            } else {
                result.put(keyInstance, value);
            }
        }
    }

    private Class<?> getKeyType(Type fieldType) {
        if (fieldType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        } else {
            return null;
        }
    }

    private Type nestedFieldType(Type keyType) {
        if (keyType instanceof ParameterizedType) {
            return ((ParameterizedType) keyType).getActualTypeArguments()[1];
        } else {
            return null;
        }
    }

    private Object keyInstanceFromString(String propertyKey, Class<?> keyType) {
        if (keyType == null) {
            return propertyKey;
        } else if (keyType.equals(String.class)) {
            return propertyKey;
        } else if (keyType.isEnum()) {
            try {
                return keyType.getDeclaredMethod("valueOf", String.class).invoke(keyType, propertyKey);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Should not happen", e);
            }
        } else {
            throw new UnsupportedOperationException("Only String and Enum allowed to be keys, got " + keyType);
        }
    }
}
