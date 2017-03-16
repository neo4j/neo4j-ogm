/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import org.neo4j.ogm.annotation.Transient;

/**
 * @author Vince Bickers
 */
public class FieldsInfo {

    private final Map<String, FieldInfo> fields;

    FieldsInfo() {
        this.fields = new HashMap<>();
    }

    public FieldsInfo(Class<?> cls) {
        this.fields = new HashMap<>();

        for (Field field : cls.getDeclaredFields()) {
            final int modifiers = field.getModifiers();
            if (!Modifier.isTransient(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                ObjectAnnotations objectAnnotations = new ObjectAnnotations();
                final Annotation[] declaredAnnotations = field.getDeclaredAnnotations();
                for (Annotation annotation : declaredAnnotations) {
                    AnnotationInfo info = new AnnotationInfo(annotation);
                    objectAnnotations.put(info.getName(), info);
                }
                if (objectAnnotations.get(Transient.class) == null) {
                    String typeParameterDescriptor = null;
                    final Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType)genericType;
                        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length > 0) {
                            for (Type typeArgument: actualTypeArguments) {
                                if (typeArgument instanceof ParameterizedType) {
                                    ParameterizedType parameterizedTypeArgument = (ParameterizedType)typeArgument;
                                    typeParameterDescriptor = parameterizedTypeArgument.getRawType().getTypeName();
                                    break;
                                }
                                else if (typeArgument instanceof TypeVariable || typeArgument instanceof WildcardType) {
                                    typeParameterDescriptor = Object.class.getName();
                                    break;
                                }
                                else if (typeArgument instanceof Class) {
                                    typeParameterDescriptor = ((Class)typeArgument).getName();
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
                    fields.put(field.getName(), new FieldInfo(field.getName(), field.getType().getTypeName(),typeParameterDescriptor, objectAnnotations));
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
