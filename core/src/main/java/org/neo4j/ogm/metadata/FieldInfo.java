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


import java.lang.reflect.Method;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;
import org.neo4j.ogm.utils.RelationshipUtils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class FieldInfo {

    private static final String primitives = "I,J,S,B,C,F,D,Z,[I,[J,[S,[B,[C,[F,[D,[Z";
    private static final String autoboxers =
            "Ljava/lang/Object;" +
            "Ljava/lang/Character;" +
            "Ljava/lang/Byte;" +
            "Ljava/lang/Short;" +
            "Ljava/lang/Integer;" +
            "Ljava/lang/Long;" +
            "Ljava/lang/Float;" +
            "Ljava/lang/Double;" +
            "Ljava/lang/Boolean;" +
            "Ljava/lang/String;" +
            "[Ljava/lang/Object;" +
            "[Ljava/lang/Character;" +
            "[Ljava/lang/Byte;" +
            "[Ljava/lang/Short;" +
            "[Ljava/lang/Integer;" +
            "[Ljava/lang/Long;" +
            "[Ljava/lang/Float;" +
            "[Ljava/lang/Double;" +
            "[Ljava/lang/Boolean;" +
            "[Ljava/lang/String;";



    private final String name;
    private final String descriptor;
    private final String typeParameterDescriptor;
    private final ObjectAnnotations annotations;

    /**
     * The associated attribute converter for this field, if applicable, otherwise null.
     */
    private AttributeConverter<?, ?> propertyConverter;

    /**
     * The associated composite attribute converter for this field, if applicable, otherwise null.
     */
    private CompositeAttributeConverter<?> compositeConverter;


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
            Object converter = getAnnotations().getConverter();
            if (converter instanceof AttributeConverter) {
                setPropertyConverter((AttributeConverter<?, ?>) converter);
            } else if (converter instanceof CompositeAttributeConverter) {
                setCompositeConverter((CompositeAttributeConverter<?>) converter);
            } else if (converter != null) {
                throw new IllegalStateException(String.format(
                        "The converter for field %s is neither an instance of AttributeConverter or CompositeAttributeConverter",
                        this.name));
            }

        }
    }

    public String getName() {
        return name;
    }


    // should these two methods be on PropertyReader, RelationshipReader respectively?
    public String property() {
        if (persistableAsProperty()) {
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
        if (!persistableAsProperty()) {
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
        if (!persistableAsProperty()) {
            if (annotations != null) {
                AnnotationInfo relationshipAnnotation = annotations.get(Relationship.CLASS);
                if (relationshipAnnotation != null) {
                    return relationshipAnnotation.get(Relationship.TYPE, null);
                }
            }
        }
        return null;
    }

    public ObjectAnnotations getAnnotations() {
        return annotations;
    }

    public boolean persistableAsProperty() {
        boolean simple = primitives.contains(descriptor)
                || (autoboxers.contains(descriptor) && typeParameterDescriptor == null)
                || (typeParameterDescriptor != null && autoboxers.contains(typeParameterDescriptor))
                || propertyConverter != null
                || compositeConverter != null;

        return simple;
    }

    public AttributeConverter getPropertyConverter() {
        return propertyConverter;
    }

    void setPropertyConverter(AttributeConverter<?, ?> propertyConverter) {
        if (this.propertyConverter == null && this.compositeConverter == null && propertyConverter != null) {
            this.propertyConverter = propertyConverter;
        } // we maybe set an annotated converter when object was constructed, so don't override with a default one
    }

    public boolean hasPropertyConverter() {
        return propertyConverter != null;
    }

    public CompositeAttributeConverter getCompositeConverter() {
        return compositeConverter;
    }

    public void setCompositeConverter(CompositeAttributeConverter<?> converter) {
        if (this.propertyConverter == null && this.compositeConverter == null && converter != null) {
            this.compositeConverter = converter;
        }
    }

    public boolean hasCompositeConverter() {
        return compositeConverter != null;
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

    public boolean isIterable() {
        String descriptorClass = getCollectionClassname();
        try {
            Class descriptorClazz = MetaDataClassLoader.loadClass(descriptorClass);
            if (Iterable.class.isAssignableFrom(descriptorClazz)) {
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
        return !isIterable() && !isArray();
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

        if (!isIterable() || isArray()) {
            return descriptor;
        }
        return typeParameterDescriptor;
    }

    public Class<?> convertedType() {
        if (hasPropertyConverter() || hasCompositeConverter()) {
            Class converterClass = hasPropertyConverter() ?
                    getPropertyConverter().getClass() : getCompositeConverter().getClass();
            String methodName = hasPropertyConverter() ? "toGraphProperty" : "toGraphProperties";

            try {
                for (Method method : converterClass.getDeclaredMethods()) {
                    //we don't want the method on the AttributeConverter interface
                    if (method.getName().equals(methodName) && !method.isSynthetic()) {
                        return method.getReturnType();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * @return <code>true</code> is this field is a constraint rather than just a plain index.
     */
    public boolean isConstraint() {
        AnnotationInfo indexAnnotation = this.getAnnotations().get(Index.class.getCanonicalName());
        return indexAnnotation != null && indexAnnotation.get("unique", "false").equals("true");
    }
}
