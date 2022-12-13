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

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.metadata.reflect.ReflectionEntityInstantiator;
import org.neo4j.ogm.session.Neo4jSession;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Gerrit Meier
 */
public class DtoMapper {

    public static final Set<String> classes = new HashSet<>();

    private final Neo4jSession session;
    private final ReflectionEntityInstantiator instantiator;

    public DtoMapper(Neo4jSession session) {
        this.session = session;
        this.instantiator = new ReflectionEntityInstantiator(session.metaData());
    }

    public <T> T newInstance(Class<T> type, Map<String, Object> properties) {
        T object = instantiator.createInstanceWithConstructorArgs(type, properties);
        setProperties(properties, object);
        return object;
    }

    private void setProperties(Map<String, Object> properties, Object instance) {
        ClassInfo classInfo = session.metaData().classInfo(instance);
        properties.forEach((s, o) -> writeProperty(classInfo, instance, s, o));
    }

    private void writeProperty(ClassInfo classInfo, Object instance, String propertyName, Object value) {

        FieldInfo writer = classInfo.getFieldInfo(propertyName);
        // merge iterable / arrays and co-erce to the correct attribute type
        if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
            Class<?> paramType = writer.type();
            Class<?> elementType = underlyingElementType(classInfo, propertyName);
            if (paramType.isArray()) {
                value = EntityAccessManager.merge(paramType, value, new Object[] {}, elementType);
            } else {
                value = EntityAccessManager.merge(paramType, value, Collections.emptyList(), elementType);
            }
        }
        writer.write(instance, value);
    }

    private Class<?> underlyingElementType(ClassInfo classInfo, String propertyName) {
        FieldInfo fieldInfo = fieldInfoForPropertyName(propertyName, classInfo);
        Class<?> clazz = null;
        if (fieldInfo != null) {
            clazz = DescriptorMappings.getType(fieldInfo.getTypeDescriptor());
        }
        return clazz;
    }

    private FieldInfo fieldInfoForPropertyName(String propertyName, ClassInfo classInfo) {
        FieldInfo labelField = classInfo.labelFieldOrNull();
        if (labelField != null && labelField.getName().equalsIgnoreCase(propertyName)) {
            return labelField;
        }
        return classInfo.propertyField(propertyName);
    }
}
