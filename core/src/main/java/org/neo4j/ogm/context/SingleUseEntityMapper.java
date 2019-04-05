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
package org.neo4j.ogm.context;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.metadata.reflect.EntityFactory;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.session.EntityInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple graph-to-entity mapper suitable for ad-hoc, one-off mappings.  This doesn't interact with a
 * mapping context or mandate graph IDs on the target types and is not designed for use in the OGM session.
 *
 * @author Adam George
 * @author Luanne Misquitta
 */
public class SingleUseEntityMapper {

    private static final Logger logger = LoggerFactory.getLogger(SingleUseEntityMapper.class);

    private final EntityFactory entityFactory;
    private final MetaData metadata;

    /**
     * Compatibility constructor for SDN 5.0
     *
     * @param mappingMetaData The {@link MetaData} to use for performing mappings
     * @param entityFactory   The entity factory to use.
     */
    public SingleUseEntityMapper(MetaData mappingMetaData, EntityFactory entityFactory) {
        this.metadata = mappingMetaData;
        this.entityFactory = new EntityFactory(mappingMetaData);
    }

    /**
     * Constructs a new {@link SingleUseEntityMapper} based on the given mapping {@link MetaData}.
     *
     * @param mappingMetaData The {@link MetaData} to use for performing mappings
     * @param entityInstantiator   The entity factory to use.
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
        ClassInfo classInfo = this.metadata.classInfo(type.getSimpleName());
        if (classInfo != null) {
            return classInfo;
        }
        throw new MappingException("Error mapping to ad-hoc " + type +
            ".  At present, only @Result types that are discovered by the domain entity package scanning can be mapped.");
    }

    // TODO: the following is all pretty much identical to GraphEntityMapper so should probably be refactored
    private void writeProperty(ClassInfo classInfo, Object instance, Map.Entry<String, Object> property) {

        FieldInfo writer = classInfo.getFieldInfo(property.getKey());

        if (writer == null) {
            FieldInfo fieldInfo = classInfo.relationshipFieldByName(property.getKey());
            if (fieldInfo != null) {
                writer = fieldInfo;
            }
        }

        if (writer == null && property.getKey().equals(
            "id")) { //When mapping query results to objects that are not domain entities, there's no concept of a GraphID
            FieldInfo idField = classInfo.identityField();
            if (idField != null) {
                writer = idField;
            }
        }

        if (writer != null) {
            Object value = property.getValue();
            if (value != null && value.getClass().isArray()) {
                value = Arrays.asList((Object[]) value);
            }
            if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
                Class elementType = underlyingElementType(classInfo, property.getKey());
                value = writer.type().isArray()
                    ? EntityAccessManager.merge(writer.type(), value, (Object[]) null, elementType)
                    : EntityAccessManager.merge(writer.type(), value, (Collection<?>) null, elementType);
            }
            writer.write(instance, value);
        } else {
            logger.warn("Unable to find property: {} on class: {} for writing", property.getKey(), classInfo.name());
        }
    }

    private Class underlyingElementType(ClassInfo classInfo, String propertyName) {
        FieldInfo fieldInfo = classInfo.propertyField(propertyName);
        if (fieldInfo != null) {
            return DescriptorMappings.getType(fieldInfo.getTypeDescriptor());
        }
        return classInfo.getUnderlyingClass();
    }
}
