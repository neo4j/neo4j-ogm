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
package org.neo4j.ogm.typeconversion;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.neo4j.ogm.annotation.Properties.NoopTransformation;
import static org.neo4j.ogm.annotation.Properties.Phase;
import static org.neo4j.ogm.support.ClassUtils.isEnum;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.support.ClassUtils;

/**
 * MapCompositeConverter converts Map field into prefixed properties of node or relationship entity.
 * The prefix and delimiter is configurable.
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class MapCompositeConverter implements CompositeAttributeConverter<Map<?, ?>> {

    private static final Set<Class> castableTypes =
        Stream.of(
            Short.class, short.class,
            Integer.class, int.class,
            Float.class, Float.class
        ).collect(collectingAndThen(toSet(), Collections::unmodifiableSet));

    private final String delimiter;
    private final boolean allowCast;

    private final ParameterizedType mapFieldType;
    /**
     * This is the prefix + delimiter, indicating which properties of the database entity participate in populating the map.
     */
    private final String propertyLookup;

    private final Predicate<Class<?>> isSupportedNativeType;

    private BiFunction<Phase, String, String> enumKeysTransformation = new NoopTransformation();

    private final Map<Class<? extends Enum<?>>, EnumStringConverter> converterCache = new ConcurrentHashMap<>();

    /**
     * Create MapCompositeConverter
     *
     * @param prefix                prefix that is used for all properties
     * @param delimiter             delimiter that is used between prefix, properties and nested properties
     * @param allowCast             if casting from non Cypher types should be allowed
     * @param mapFieldType          type information for the field
     * @param isSupportedNativeType Passed on f
     */
    public MapCompositeConverter(String prefix, String delimiter, boolean allowCast, ParameterizedType mapFieldType,
        Predicate<Class<?>> isSupportedNativeType) {

        this.delimiter = delimiter;
        this.allowCast = allowCast;
        this.mapFieldType = mapFieldType;
        this.propertyLookup = prefix + delimiter;
        this.isSupportedNativeType = isSupportedNativeType;
    }

    public void setEnumKeysTransformation(BiFunction<Phase, String, String> enumKeysTransformation) {

        this.enumKeysTransformation = enumKeysTransformation;
    }

    @Override
    public Map<String, ?> toGraphProperties(Map<?, ?> fieldValue) {
        if (fieldValue == null) {
            return emptyMap();
        }
        Map<String, Object> graphProperties = new HashMap<>(fieldValue.size());
        addMapToProperties(fieldValue, graphProperties, propertyLookup);
        return graphProperties;
    }

    /**
     * @param fieldValue      The actual domain value
     * @param graphProperties The properties that will be written to the graph, must be mutable
     * @param entryPrefix     The prefix for the current map. On top level, this will be {@link #propertyLookup}, all nested
     *                        maps will have their name + {@link #delimiter} added to this.
     */
    private void addMapToProperties(Map<?, ?> fieldValue, Map<String, Object> graphProperties, String entryPrefix) {
        for (Map.Entry<?, ?> entry : fieldValue.entrySet()) {
            Object entryValue = entry.getValue();
            String entryKey = entryPrefix + keyInstanceToString(entry.getKey());
            if (entryValue instanceof Map) {
                addMapToProperties((Map<?, ?>) entryValue, graphProperties, entryKey + delimiter);
            } else if (isCypherType(entryValue) || (allowCast && canCastType(entryValue))) {
                graphProperties.put(entryKey, entryValue);
            } else if (ClassUtils.isEnum(entryValue)) {
                @SuppressWarnings("unchecked") // It is checked by the condition above.
                EnumStringConverter enumStringConverter = converterCache.computeIfAbsent((Class<? extends Enum<?>>) entryValue.getClass(), EnumStringConverter::new);
                graphProperties.put(entryKey, enumStringConverter.toGraphProperty((Enum<?>) entryValue));
            } else {
                throw new MappingException("Could not map key=" + entryPrefix + entry.getKey() + ", " +
                    "value=" + entryValue + " (type = " + entryValue.getClass() + ") " +
                    "because it is not a supported type.");
            }
        }
    }

    private boolean canCastType(Object value) {
        return castableTypes.contains(value.getClass());
    }

    private boolean isCypherType(Object entryValue) {
        return entryValue == null || this.isSupportedNativeType.test(entryValue.getClass());
    }

    @Override
    public Map<?, ?> toEntityAttribute(Map<String, ?> value) {

        Set<? extends Map.Entry<String, ?>> prefixedProperties = value.entrySet()
            .stream()
            .filter(entry -> entry.getKey().startsWith(propertyLookup))
            .collect(toSet());

        Map<Object, Object> result = new HashMap<>();
        for (Map.Entry<String, ?> entry : prefixedProperties) {
            String propertyKey = entry.getKey().substring(propertyLookup.length());
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
                if (value instanceof String && ClassUtils.isEnum(valueType)) {
                    @SuppressWarnings("unchecked")
                    EnumStringConverter enumStringConverter = converterCache.computeIfAbsent((Class<? extends Enum<?>>) valueType, EnumStringConverter::new);
                    value = enumStringConverter.toEntityAttribute((String) value);
                } else {
                    value = Utils.coerceTypes((Class<?>) valueType, value);
                }
            }

            result.put(keyInstance, value);
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

    private String keyInstanceToString(Object propertyKey) {
        if (propertyKey == null) {
            throw new UnsupportedOperationException("Null is not a supported property key!");
        }

        if (propertyKey instanceof String) {
            return (String) propertyKey;
        } else if (isEnum(propertyKey)) {
            return enumKeysTransformation.apply(Phase.TO_GRAPH, ((Enum) propertyKey).name());
        }

        throw new UnsupportedOperationException(
            "Only String and Enum allowed to be keys, got " + propertyKey.getClass());

    }

    private Object keyInstanceFromString(String propertyKey, Class<?> keyType) {
        if (keyType == null) {
            return propertyKey;
        } else if (keyType.equals(String.class)) {
            return propertyKey;
        } else if (isEnum(keyType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Enum key = Enum.valueOf(((Class<Enum>) keyType), enumKeysTransformation.apply(
                Phase.TO_ENTITY, propertyKey));
            return key;
        }

        throw new UnsupportedOperationException("Only String and Enum allowed to be keys, got " + keyType);
    }

    public String getPropertyLookup() {
        return propertyLookup;
    }
}
