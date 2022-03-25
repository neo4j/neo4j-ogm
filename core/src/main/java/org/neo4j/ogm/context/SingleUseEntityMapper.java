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
package org.neo4j.ogm.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.ObjectAnnotations;
import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.metadata.reflect.EntityFactory;
import org.neo4j.ogm.metadata.reflect.GenericUtils;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.session.EntityInstantiator;
import org.neo4j.ogm.support.ClassUtils;
import org.neo4j.ogm.support.CollectionUtils;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;
import org.neo4j.ogm.typeconversion.MapCompositeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple graph-to-entity mapper suitable for ad-hoc, one-off mappings.  This doesn't interact with a
 * mapping context or mandate graph IDs on the target types and is not designed for use in the OGM session.
 *
 * @author Adam George
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class SingleUseEntityMapper {

    private static final Logger logger = LoggerFactory.getLogger(SingleUseEntityMapper.class);

    private final EntityFactory entityFactory;
    private final MetaData metadata;

    /**
     * Compatibility constructor for SDN 5.0 and 5.1
     *
     * @param mappingMetaData The {@link MetaData} to use for performing mappings
     * @param entityFactory   The entity factory to use.
     */
    public SingleUseEntityMapper(MetaData mappingMetaData, @SuppressWarnings("unused") EntityFactory entityFactory) {
        this.metadata = mappingMetaData;
        this.entityFactory = new EntityFactory(mappingMetaData);
    }

    /**
     * Constructs a new {@link SingleUseEntityMapper} based on the given mapping {@link MetaData}.
     *
     * @param mappingMetaData    The {@link MetaData} to use for performing mappings
     * @param entityInstantiator The entity factory to use.
     */
    public SingleUseEntityMapper(MetaData mappingMetaData, EntityInstantiator entityInstantiator) {
        this.metadata = mappingMetaData;
        this.entityFactory = new EntityFactory(mappingMetaData, entityInstantiator);
    }

    /**
     * Maps a row-based result onto a new instance of the specified type.
     *
     * @param <T>         The class of object to return
     * @param type        The {@link Class} denoting the type of object to create
     * @param columnNames The names of the columns in each row of the result
     * @param rowModel    The {@link org.neo4j.ogm.model.RowModel} containing the data to map
     * @return A new instance of {@code T} populated with the data in the specified row model
     */
    public <T> T map(Class<T> type, String[] columnNames, RowModel rowModel) {
        Map<String, Object> properties = new HashMap<>();
        for (int i = 0; i < rowModel.getValues().length; i++) {
            properties.put(columnNames[i], rowModel.getValues()[i]);
        }

        T entity = this.entityFactory.newObject(type, properties);
        setPropertiesOnEntity(entity, properties);
        return entity;
    }

    public <T> T map(Class<T> type, Map<String, Object> row) {
        T entity = this.entityFactory.newObject(type, row);
        setPropertiesOnEntity(entity, row);
        return entity;
    }

    private void setPropertiesOnEntity(Object entity, Map<String, Object> propertyMap) {
        ClassInfo classInfo = resolveClassInfoFor(entity.getClass());
        for (Entry<String, Object> propertyMapEntry : propertyMap.entrySet()) {
            writeProperty(classInfo, entity, propertyMapEntry);
        }
    }

    private ClassInfo resolveClassInfoFor(Class<?> type) {
        ClassInfo classInfo = this.metadata.classInfo(type.getName());
        if (classInfo != null) {
            return classInfo;
        }
        throw new MappingException("Error mapping to ad-hoc " + type +
            ".  At present, only @Result types that are discovered by the domain entity package scanning can be mapped.");
    }

    private void writeProperty(ClassInfo classInfo, Object instance, Map.Entry<String, Object> property) {

        String key = property.getKey();
        FieldInfo writer = classInfo.getFieldInfo(key);

        if (writer == null) { // Check relationships
            FieldInfo fieldInfo = classInfo.relationshipFieldByName(key);
            if (fieldInfo != null) {
                writer = fieldInfo;
            }
        }

        boolean isComposite = false;
        if (writer == null) { // Check property maps
            Optional<FieldInfo> optionalMatchingComposite = findMatchingCompositeField(classInfo, key);
            if (optionalMatchingComposite.isPresent()) {
                writer = optionalMatchingComposite.get();
                isComposite = true;
            }
        }

        if (writer == null) {
            logger.warn("Unable to find property: {} on class: {} for writing", key, classInfo.name());
        } else {
            // That's what we're gonna write too
            Class<?> effectiveFieldType = writer.type();

            // This takes attribute and composite converters into consideration.
            Class<?> elementType = writer.convertedType();
            if (elementType == null) {
                // If it is not a converted type, we retrieve the element type (not the field type, which maybe a collection)
                elementType = DescriptorMappings.getType(writer.getTypeDescriptor());
            }

            Predicate<Class<?>> isCollectionLike = c -> c != null && (c.isArray() || Iterable.class.isAssignableFrom(c));
            boolean targetIsCollection = isCollectionLike.test(effectiveFieldType);

            Object value = property.getValue();

            // In case we have not been able to determine a collection type from the the field
            // but the field is generic and we received something collection like we treat
            // the field as a collection anyway.
            if (!targetIsCollection && GenericUtils.isGenericField(writer.getField())
                && value != null && isCollectionLike.test(value.getClass())) {
                targetIsCollection = true;
            }

            if (metadata.classInfo(elementType) != null) {
                value = mapKnownEntityType(elementType, key, value, targetIsCollection);
            } else if (isComposite) {

                value = getAndMergeExistingCompositeValue(instance, key, writer, value);
            }

            // merge iterable / arrays and co-erce to the correct attribute type
            if (targetIsCollection) {
                if (value == null) {
                    value = Collections.emptyList();
                } else if (value.getClass().isArray()) {
                    value = Arrays.asList((Object[]) value);
                }

                if (effectiveFieldType.isArray()) {
                    value = EntityAccessManager.merge(effectiveFieldType, value, new Object[] {}, elementType);
                } else {
                    value = EntityAccessManager.merge(effectiveFieldType, value, Collections.emptyList(), elementType);
                }
            }
            writer.write(instance, value);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object getAndMergeExistingCompositeValue(Object instance, String key, FieldInfo writer, Object value) {

        // Don't use writer.readProperty() here as that would convert
        // the property into graph format again, leaving us with prefixed values
        Map current = (Map) writer.read(instance);
        Map mergedValues = new HashMap(current == null ? Collections.emptyMap() : current);
        if (value instanceof Map) {
            mergedValues.putAll((Map) value);
        } else {
            // We must strip the prefix here
            mergedValues.put(
                key.replaceAll(
                    Pattern.quote(((MapCompositeConverter) writer.getCompositeConverter()).getPropertyLookup()), ""),
                value
            );
        }
        value = mergedValues;
        return value;
    }

    private Optional<FieldInfo> findMatchingCompositeField(ClassInfo classInfo, String key) {
        return classInfo.fieldsInfo().fields().stream().filter(f -> {
            ObjectAnnotations annotations = f.getAnnotations();
            if (!annotations.has(Properties.class)) {
                return false;
            }

            CompositeAttributeConverter<?> compositeConverter = f.getCompositeConverter();
            if (!(compositeConverter instanceof MapCompositeConverter)) {
                return false;
            }

            return key.startsWith(((MapCompositeConverter) compositeConverter).getPropertyLookup());
        }).findFirst();
    }

    /**
     * If the element type is a known class, it will be mapped, either into a single value or into a collection of things.
     * If the element type is unknown or cannot be mapped to a single property, the original value is returned.
     *
     * @param elementType  The target type, must not be null
     * @param property     The name of the property
     * @param value        The value (can be null)
     * @param asCollection whether to create a collection or not
     * @return The mapped value
     */
    @SuppressWarnings("unchecked")
    Object mapKnownEntityType(Class<?> elementType, String property, Object value, boolean asCollection) {

        if (asCollection && isMappedCollection(value, elementType)) {
            return flatten(value);
        }

        List<Object> nestedObjects = new ArrayList<>();

        for (Object nestedPropertyMap : CollectionUtils.iterableOf(value)) {
            if (nestedPropertyMap instanceof Map) {
                // Recursively map maps
                nestedObjects.add(map(elementType, (Map<String, Object>) nestedPropertyMap));
            } else if (elementType.isInstance(nestedPropertyMap) || ClassUtils.isEnum(elementType)) {
                // Add fitting types and enums directly
                nestedObjects.add(nestedPropertyMap);
            } else {
                logger.warn("Cannot map {} to a nested result object for property {}", nestedPropertyMap, property);
            }
        }

        if (asCollection) {
            return nestedObjects;
        } else if (nestedObjects.size() > 1) {
            logger.warn(
                "Cannot map property {} from result set: The result contains more than one entry for the property.",
                property);
            // Returning the original value here is done on purpose to not change in edge cases in SDN
            // for which we don't have tests yet. Edge cases can be weird queries with weird query result classes
            // that fit together non the less.
            return value;
        } else {
            return nestedObjects.isEmpty() ? null : nestedObjects.get(0);
        }
    }

    private static List<Object> flatten(Object value) {

        if (value instanceof Collection) {
            List<Object> result = new ArrayList<>();
            for (Object object : ((Collection<?>) value)) {
                result.addAll(flatten(object));
            }
            return result;
        } else {
            return Collections.singletonList(value);
        }
    }

    private static boolean isMappedCollection(Object resultObject, Class<?> elementType) {

        Predicate<Object> isElementType = elementType::isInstance;
        return resultObject instanceof Collection && ((Collection<?>) resultObject)
            .stream().allMatch(isElementType.or(v -> isMappedCollection(v, elementType)));
    }
}
