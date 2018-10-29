/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.metadata;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class FieldsInfo {

    private final Map<String, FieldInfo> fields;

    FieldsInfo() {
        this.fields = new HashMap<>();
    }


    private List<Field> fields(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        do {
            Field[] sFields = cls.getDeclaredFields();
            fields.addAll(Arrays.asList(sFields));
            cls = cls.getSuperclass();
        } while (cls != null);
        return fields;
    }


    public FieldsInfo(ClassInfo classInfo, Class<?> cls) {
        this.fields = new HashMap<>();

        for (Field field : this.fields(cls)) {
            final int modifiers = field.getModifiers();
            if (!Modifier.isTransient(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                ObjectAnnotations objectAnnotations = ObjectAnnotations.of(field.getDeclaredAnnotations());
                if (!objectAnnotations.has(Transient.class)) {
                    String typeParameterDescriptor = null;
                    final Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericType;
                        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length > 0) {
                            for (Type typeArgument : actualTypeArguments) {
                                if (typeArgument instanceof ParameterizedType) {
                                    ParameterizedType parameterizedTypeArgument = (ParameterizedType) typeArgument;
                                    typeParameterDescriptor = parameterizedTypeArgument.getRawType().getTypeName();
                                    break;
                                } else if ((typeArgument instanceof TypeVariable || typeArgument instanceof WildcardType)
                                    // The type parameter descriptor doesn't matter if we're dealing with an explicit relationship
                                    // We must not try to persist it as a property if the user explicitly asked for storing it as a node.
                                    && !objectAnnotations.has(Relationship.class)) {
                                    typeParameterDescriptor = Object.class.getName();
                                    break;
                                } else if (typeArgument instanceof Class) {
                                    typeParameterDescriptor = ((Class) typeArgument).getName();
                                }
                            }
                        }
                        if (typeParameterDescriptor == null) {
                            typeParameterDescriptor = parameterizedType.getRawType().getTypeName();
                        }
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
     *
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
