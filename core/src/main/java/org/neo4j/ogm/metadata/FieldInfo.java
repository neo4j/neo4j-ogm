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
package org.neo4j.ogm.metadata;

import static org.neo4j.ogm.metadata.reflect.GenericUtils.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;
import org.neo4j.ogm.typeconversion.MapCompositeConverter;
import org.neo4j.ogm.utils.ClassUtils;
import org.neo4j.ogm.utils.RelationshipUtils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class FieldInfo {

    private static final String PRIMITIVES = "char,byte,short,int,long,float,double,boolean,char[],byte[],short[],int[],long[],float[],double[],boolean[]";
    private static final String AUTOBOXERS =
        "java.lang.Object" +
            "java.lang.Character" +
            "java.lang.Byte" +
            "java.lang.Short" +
            "java.lang.Integer" +
            "java.lang.Long" +
            "java.lang.Float" +
            "java.lang.Double" +
            "java.lang.Boolean" +
            "java.lang.String" +
            "java.lang.Object[]" +
            "java.lang.Character[]" +
            "java.lang.Byte[]" +
            "java.lang.Short[]" +
            "java.lang.Integer[]" +
            "java.lang.Long[]" +
            "java.lang.Float[]" +
            "java.lang.Double[]" +
            "java.lang.Boolean[]" +
            "java.lang.String[]";

    private final String name;
    private final String descriptor;
    private final String typeParameterDescriptor;
    private final ObjectAnnotations annotations;
    private final boolean isArray;
    private final ClassInfo containingClassInfo;
    private final Field field;
    private final Class<?> fieldType;
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
     * @param typeParameterDescriptor The descriptor that expresses the generic type parameter, which may be <code>null</code>
     *                                if that's not appropriate
     * @param annotations             The {@link ObjectAnnotations} applied to the field
     */
    public FieldInfo(ClassInfo classInfo, Field field, String typeParameterDescriptor, ObjectAnnotations annotations) {
        this.containingClassInfo = classInfo;
        this.field = field;
        this.fieldType = isGenericField(field) ? findFieldType(field, classInfo.getUnderlyingClass()) : field.getType();
        this.isArray = fieldType.isArray();
        this.name = field.getName();
        this.descriptor = field.getType().getTypeName();
        this.typeParameterDescriptor = typeParameterDescriptor;
        this.annotations = annotations;
        if (!this.annotations.isEmpty()) {
            Object converter = getAnnotations().getConverter(this.fieldType);
            if (converter instanceof AttributeConverter) {
                setPropertyConverter((AttributeConverter<?, ?>) converter);
            } else if (converter instanceof CompositeAttributeConverter) {
                setCompositeConverter((CompositeAttributeConverter<?>) converter);
            } else if (converter != null) {
                throw new IllegalStateException(String.format(
                    "The converter for field %s is neither an instance of AttributeConverter or CompositeAttributeConverter",
                    this.name));
            } else {
                AnnotationInfo properties = getAnnotations().get(Properties.class);
                if (properties != null) {
                    if (fieldType.equals(Map.class)) {
                        Type fieldGenericType = field.getGenericType();
                        MapCompositeConverter mapCompositeConverter = new MapCompositeConverter(
                            properties.get("prefix", field.getName()),
                            properties.get("delimiter"),
                            Boolean.valueOf(properties.get("allowCast")),
                            (ParameterizedType) fieldGenericType);
                        setCompositeConverter(mapCompositeConverter);
                    } else {
                        throw new MappingException(
                            "@Properties annotation is allowed only on fields of type java.util.Map");
                    }
                }
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
                AnnotationInfo propertyAnnotation = annotations.get(Property.class);
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
                AnnotationInfo relationshipAnnotation = annotations.get(Relationship.class);
                if (relationshipAnnotation != null) {
                    return relationshipAnnotation
                        .get(Relationship.TYPE, RelationshipUtils.inferRelationshipType(getName()));
                }
            }
            return RelationshipUtils.inferRelationshipType(getName());
        }
        return null;
    }

    public String relationshipTypeAnnotation() {
        if (!persistableAsProperty()) {
            if (annotations != null) {
                AnnotationInfo relationshipAnnotation = annotations.get(Relationship.class);
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

    // should be improved, as unmanaged types (like ZonedDateTime) are not detected as property but as relationship
    // see #347
    public boolean persistableAsProperty() {

        return PRIMITIVES.contains(descriptor)
            || (AUTOBOXERS.contains(descriptor) && typeParameterDescriptor == null)
            || (typeParameterDescriptor != null && AUTOBOXERS.contains(typeParameterDescriptor))
            || propertyConverter != null
            || compositeConverter != null;
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
            AnnotationInfo annotationInfo = getAnnotations().get(Relationship.class);
            if (annotationInfo == null) {
                return defaultDirection;
            }
            return annotationInfo.get(Relationship.DIRECTION, defaultDirection);
        }
        throw new RuntimeException("relationship direction call invalid");
    }

    public boolean isIterable() {
        return Iterable.class.isAssignableFrom(fieldType);
    }

    public boolean isTypeOf(Class<?> type) {
        return doesDescriptorMatchType(descriptor, type);
    }

    public boolean isParameterisedTypeOf(Class<?> type) {
        return doesDescriptorMatchType(typeParameterDescriptor, type);
    }

    public boolean isArrayOf(Class<?> type) {
        while (type != null) {
            String typeSignature = type.getName();
            if (descriptor != null && descriptor.equals(typeSignature + "[]")) {
                return true;
            }
            // #issue 42: check interfaces when types are defined using generics as interface extensions
            for (Class<?> iface : type.getInterfaces()) {
                typeSignature = iface.getName();
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
        return descriptor;
    }

    public boolean isScalar() {
        return !isIterable() && !isArray();
    }

    public boolean isLabelField() {
        return this.getAnnotations().get(Labels.class) != null;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean hasAnnotation(String annotationName) {
        return getAnnotations().get(annotationName) != null;
    }

    public boolean hasAnnotation(Class<?> annotationNameClass) {
        return getAnnotations().get(annotationNameClass.getName()) != null;
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
        AnnotationInfo idAnnotation = this.getAnnotations().get(Id.class.getName());
        if (idAnnotation != null) {
            return true;
        }
        AnnotationInfo indexAnnotation = this.getAnnotations().get(Index.class.getName());
        return indexAnnotation != null && indexAnnotation.get("unique", "false").equals("true");
    }

    // =================================================================================================================
    // From FieldAccessor
    // =================================================================================================================

    public static void write(Field field, Object instance, final Object value) {

        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                field.setAccessible(true);
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public static Object read(Field field, Object instance) {

        return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                field.setAccessible(true);
                return field.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void write(Object instance, Object value) {

        if (hasPropertyConverter()) {
            value = getPropertyConverter().toEntityAttribute(value);
            write(field, instance, value);
        } else {
            if (isScalar()) {
                String descriptor = getTypeDescriptor();
                value = Utils.coerceTypes(ClassUtils.getType(descriptor), value);
            }
            write(field, instance, value);
        }
    }

    /**
     * Write the value of the field directly to the instance, bypassing the converters
     *
     * @param instance class instance
     * @param value    field value to be written
     */
    public void writeDirect(Object instance, Object value) {
        write(field, instance, value);
    }

    public Class<?> type() {
        Class convertedType = convertedType();
        if (convertedType != null) {
            return convertedType;
        }
        return fieldType;
    }

    public String relationshipName() {
        return this.relationship();
    }

    public boolean forScalar() {
        return !Iterable.class.isAssignableFrom(type()) && !type().isArray();
    }

    public String typeParameterDescriptor() {
        return getTypeDescriptor();
    }

    public Object read(Object instance) {
        return read(containingClassInfo.getField(this), instance);
    }

    public Object readProperty(Object instance) {
        if (hasCompositeConverter()) {
            throw new IllegalStateException(
                "The readComposite method should be used for fields with a CompositeAttributeConverter");
        }
        Object value = read(containingClassInfo.getField(this), instance);
        if (hasPropertyConverter()) {
            value = getPropertyConverter().toGraphProperty(value);
        }
        return value;
    }

    public Map<String, ?> readComposite(Object instance) {
        if (!hasCompositeConverter()) {
            throw new IllegalStateException(
                "readComposite should only be used when a field is annotated with a CompositeAttributeConverter");
        }
        Object value = read(containingClassInfo.getField(this), instance);
        return getCompositeConverter().toGraphProperties(value);
    }

    public String relationshipType() {
        return relationship();
    }

    public String propertyName() {
        return property();
    }

    public boolean isComposite() {
        return hasCompositeConverter();
    }

    public String relationshipDirection() {
        ObjectAnnotations annotations = getAnnotations();
        if (annotations != null) {
            AnnotationInfo relationshipAnnotation = annotations.get(Relationship.class);
            if (relationshipAnnotation != null) {
                return relationshipAnnotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED);
            }
        }
        return Relationship.UNDIRECTED;
    }

    public String typeDescriptor() {
        return getTypeDescriptor();
    }

    public Field getField() {
        return field;
    }

    /**
     * ClassInfo for the class this field is defined in
     *
     * @return ClassInfo of containing
     */
    public ClassInfo containingClassInfo() {
        return containingClassInfo;
    }

    public boolean isVersionField() {
        return field.getAnnotation(Version.class) != null;
    }

    private static boolean doesDescriptorMatchType(String descriptor, Class<?> type) {

        while (type != null) {

            if (doesDescriptorMatchTypeOrInterface(descriptor, type)) {
                return true;
            }
            type = type.getSuperclass();
        }

        return false;
    }

    private static boolean doesDescriptorMatchTypeOrInterface(String descriptorToMatch, Class<?> type) {

        String typeSignature = type.getName();
        if (descriptorToMatch != null && descriptorToMatch.equals(typeSignature)) {
            return true;
        }

        if (doesAnyInterfaceMatch(descriptorToMatch, type)) {
            return true;
        }

        return false;
    }

    private static boolean doesAnyInterfaceMatch(String descriptor, Class<?> type) {

        for (Class<?> interfaceClass : type.getInterfaces()) {
            if (doesDescriptorMatchTypeOrInterface(descriptor, interfaceClass)) {
                return true;
            }
        }

        return false;
    }
}
