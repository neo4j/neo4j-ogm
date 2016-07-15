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


import org.neo4j.ogm.utils.RelationshipUtils;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotations.Labels;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.util.Collection;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class FieldInfo {

    private static final String primitives = "I,J,S,B,C,F,D,Z,[I,[J,[S,[B,[C,[F,[D,[Z";

    private final String name;
    private final String descriptor;
    private final String typeParameterDescriptor;
    private final ObjectAnnotations annotations;

    private AttributeConverter<?, ?> converter;

    /**
     * Constructs a new {@link FieldInfo} based on the given arguments.
     *
     * @param name                    The name of the field
     * @param descriptor              The field descriptor that expresses the type of the field using Java signature string notation
     * @param typeParameterDescriptor The descriptor that expresses the generic type parameter, which may be <code>null</code>
     *                                if that's not appropriate
     * @param annotations             The {@link ObjectAnnotations} applied to the field
     */
    public FieldInfo(String name, String descriptor, String typeParameterDescriptor, ObjectAnnotations annotations) {
        this.name = name;
        this.descriptor = descriptor;
        this.typeParameterDescriptor = typeParameterDescriptor;
        this.annotations = annotations;
        if (!this.annotations.isEmpty()) {
            setConverter(getAnnotations().getConverter());
        }
    }

    public String getName() {
        return name;
    }


    // should these two methods be on PropertyReader, RelationshipReader respectively?
    public String property() {
        if (isSimple()) {
            if (annotations != null) {
                AnnotationInfo propertyAnnotation = annotations.get(Property.CLASS);
                if (propertyAnnotation != null) {
                    return propertyAnnotation.get(Property.NAME, getName());
                }
            }
            return getName();
        }
        return null;
    }

    public String relationship() {
        if (!isSimple()) {
            if (annotations != null) {
                AnnotationInfo relationshipAnnotation = annotations.get(Relationship.CLASS);
                if (relationshipAnnotation != null) {
                    return relationshipAnnotation.get(Relationship.TYPE, RelationshipUtils.inferRelationshipType(getName()));
                }
            }
            return RelationshipUtils.inferRelationshipType(getName());
        }
        return null;
    }

    public String relationshipTypeAnnotation() {
        if (!isSimple()) {
            if (annotations != null) {
                AnnotationInfo relationshipAnnotation = annotations.get(Relationship.CLASS);
                if (relationshipAnnotation != null) {
                    return relationshipAnnotation.get(Relationship.TYPE, null);
                }
            }
        }
        return null;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getTypeParameterDescriptor() {
        return typeParameterDescriptor;
    }

    public ObjectAnnotations getAnnotations() {
        return annotations;
    }

    public boolean isSimple() {
        return primitives.contains(descriptor)
                || converter != null
                || (descriptor.contains("java/lang/") && typeParameterDescriptor == null)
                || (typeParameterDescriptor != null && typeParameterDescriptor.contains("java/lang/"));
    }

    public AttributeConverter converter() {
        return converter;
    }

    void setConverter(AttributeConverter<?, ?> converter) {
        if (this.converter == null && converter != null) {
            this.converter = converter;
        } // we maybe set an annotated converter when object was constructed, so don't override with a default one
    }

    public boolean hasConverter() {
        return converter != null;
    }

    private AttributeConverter<?, ?> getAnnotatedTypeConverter() {
        return getAnnotations().getConverter();
    }

    public String relationshipDirection(String defaultDirection) {
        if (relationship() != null) {
            AnnotationInfo annotationInfo = getAnnotations().get(Relationship.CLASS);
            if (annotationInfo == null) {
                return defaultDirection;
            }
            return annotationInfo.get(Relationship.DIRECTION, defaultDirection);
        }
        throw new RuntimeException("relationship direction call invalid");
    }

    public boolean isTypeOf(Class<?> type) {

        while (type != null) {
            String typeSignature = "L" + type.getName().replace(".", "/") + ";";
            if (descriptor != null && descriptor.equals(typeSignature)) {
                return true;
            }
            // #issue 42: check interfaces when types are defined using generics as interface extensions
            for (Class<?> iface : type.getInterfaces()) {
                typeSignature = "L" + iface.getName().replace(".", "/") + ";";
                if (descriptor != null && descriptor.equals(typeSignature)) {
                    return true;
                }
            }
            type = type.getSuperclass();
        }
        return false;
    }

    public boolean isCollection() {
        String descriptorClass = getCollectionClassname();
        try {
            Class descriptorClazz = MetaDataClassLoader.loadClass(descriptorClass);//Class.forName(descriptorClass);
            if (Collection.class.isAssignableFrom(descriptorClazz)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return false;
    }

    public boolean isParameterisedTypeOf(Class<?> type) {
        while (type != null) {
            String typeSignature = "L" + type.getName().replace(".", "/") + ";";
            if (typeParameterDescriptor != null && typeParameterDescriptor.equals(typeSignature)) {
                return true;
            }
            // #issue 42: check interfaces when types are defined using generics as interface extensions
            for (Class<?> iface : type.getInterfaces()) {
                typeSignature = "L" + iface.getName().replace(".", "/") + ";";
                if (typeParameterDescriptor != null && typeParameterDescriptor.equals(typeSignature)) {
                    return true;
                }
            }
            type = type.getSuperclass();
        }
        return false;
    }

    public boolean isArrayOf(Class<?> type) {
        while (type != null) {
            String typeSignature = "[L" + type.getName().replace(".", "/") + ";";
            if (descriptor != null && descriptor.equals(typeSignature)) {
                return true;
            }
            // #issue 42: check interfaces when types are defined using generics as interface extensions
            for (Class<?> iface : type.getInterfaces()) {
                typeSignature = "[L" + iface.getName().replace(".", "/") + ";";
                if (descriptor != null && descriptor.equals(typeSignature)) {
                    return true;
                }
            }
            type = type.getSuperclass();
        }
        return false;
    }

    /**
     * Get the collection class name for the field
     *
     * @return collection class name
     */
    public String getCollectionClassname() {
        String descriptorClass = descriptor.replace("/", ".");
        if (descriptorClass.startsWith("L")) {
            descriptorClass = descriptorClass.substring(1, descriptorClass.length() - 1); //remove the leading L and trailing ;
        }
        return descriptorClass;
    }

    public boolean isScalar() {

        if (typeParameterDescriptor != null) return false;
        if (descriptor.startsWith("[")) return false;

        return true;
    }

    public boolean isLabelField() {
        return this.getAnnotations().get(Labels.CLASS) != null;
    }

    public boolean isArray() {
        return descriptor.startsWith("[");
    }

    public boolean hasAnnotation(String annotationName) {
        return getAnnotations().get(annotationName) != null;
    }

    /**
     * Get the type descriptor
     *
     * @return the descriptor if the field is scalar or an array, otherwise the type parameter descriptor.
     */
    public String getTypeDescriptor() {
        if (isScalar() || isArray()) {
            return descriptor;
        }
        return typeParameterDescriptor;
    }
}
