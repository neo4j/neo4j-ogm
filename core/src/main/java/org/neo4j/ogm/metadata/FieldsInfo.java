/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.reflect.GenericUtils;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class FieldsInfo {

    private final Map<String, FieldInfo> fields;

    FieldsInfo(ClassInfo classInfo, Class<?> clazz) {
        this.fields = new HashMap<>();

        // Fields influenced by this class are all all declared fields plus
        // all generics fields of possible superclasses that resolve to concrete
        // types through this class.
        List<Field> allFieldsInfluencedByThisClass = new ArrayList<>();
        allFieldsInfluencedByThisClass.addAll(getGenericOrParameterizedFieldsInHierarchyOf(clazz));
        allFieldsInfluencedByThisClass.addAll(Arrays.asList(clazz.getDeclaredFields()));

        for (Field field : allFieldsInfluencedByThisClass) {
            final int modifiers = field.getModifiers();
            if (!(field.isSynthetic() || Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers))) {
                ObjectAnnotations objectAnnotations = ObjectAnnotations.of(field.getDeclaredAnnotations());
                if (!objectAnnotations.has(Transient.class)) {
                    String typeParameterDescriptor = null;
                    final Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        typeParameterDescriptor = GenericUtils.findFieldType(field, clazz).getName();
                    }
                    if (typeParameterDescriptor == null && (genericType instanceof TypeVariable)) {
                        typeParameterDescriptor = field.getType().getTypeName();
                    }
                    fields.put(field.getName(),
                        new FieldInfo(classInfo, field, typeParameterDescriptor, objectAnnotations));
                }
            }
        }
    }

    private static List<Field> getGenericOrParameterizedFieldsInHierarchyOf(Class<?> clazz) {

        Predicate<Field> predicate = GenericUtils::isGenericField;
        predicate = predicate.or(GenericUtils::isParameterizedField);

        List<Field> genericFieldsInHierarchy = new ArrayList<>();
        Class<?> currentClass = clazz.getSuperclass();
        while(currentClass != null) {
            Stream.of(currentClass.getDeclaredFields())
                .filter(predicate)
                .forEach(genericFieldsInHierarchy::add);
            currentClass = currentClass.getSuperclass();
        }

        return genericFieldsInHierarchy;
    }

    public Collection<FieldInfo> fields() {
        return fields.values();
    }

    public Collection<FieldInfo> compositeFields() {
        List<FieldInfo> fields = new ArrayList<>();
        for (FieldInfo field : fields()) {
            if (field.hasCompositeConverter()) {
                fields.add(field);
            }
        }
        return Collections.unmodifiableList(fields);
    }

    /**
     * Should not be used directly as it doesn't take property fields into account.
     * @param name
     * @return
     * @deprecated since 3.1.1 use {@link ClassInfo#getFieldInfo(String)} instead.
     */
    @Deprecated
    public FieldInfo get(String name) {
        return fields.get(name);
    }

    public void append(FieldsInfo fieldsInfo) {
        for (FieldInfo fieldInfo : fieldsInfo.fields()) {
            if (!fields.containsKey(fieldInfo.getName())) {
                fields.put(fieldInfo.getName(), fieldInfo);
            }
        }
    }
}
