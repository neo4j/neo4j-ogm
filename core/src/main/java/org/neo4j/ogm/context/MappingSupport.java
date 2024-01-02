/*
 * Copyright (c) 2002-2024 "Neo4j,"
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

import java.util.Collections;

/**
 * @author Gerrit Meier
 */
public final class MappingSupport {

    public static void writeProperty(ClassInfo classInfo, Object instance, String propertyName, Object value) {

        FieldInfo writer = classInfo.getFieldInfo(propertyName);
        value = convertValue(classInfo, propertyName, value, writer);
        writer.write(instance, value);
    }

    public static Object convertValue(ClassInfo classInfo, String propertyName, Object value, FieldInfo fieldInfo) {
        if (fieldInfo.type().isArray() || Iterable.class.isAssignableFrom(fieldInfo.type())) {
            Class<?> paramType = fieldInfo.type();
            Class<?> elementType = underlyingElementType(classInfo, propertyName);
            if (paramType.isArray()) {
                return EntityAccessManager.merge(paramType, value, new Object[] {}, elementType);
            } else {
                return EntityAccessManager.merge(paramType, value, Collections.emptyList(), elementType);
            }
        }
        return value;
    }

    private static Class<?> underlyingElementType(ClassInfo classInfo, String propertyName) {
        FieldInfo fieldInfo = fieldInfoForPropertyName(propertyName, classInfo);
        Class<?> clazz = null;
        if (fieldInfo != null) {
            clazz = DescriptorMappings.getType(fieldInfo.getTypeDescriptor());
        }
        return clazz;
    }

    private static FieldInfo fieldInfoForPropertyName(String propertyName, ClassInfo classInfo) {
        FieldInfo labelField = classInfo.labelFieldOrNull();
        if (labelField != null && labelField.getName().equalsIgnoreCase(propertyName)) {
            return labelField;
        }
        return classInfo.propertyField(propertyName);
    }
}
