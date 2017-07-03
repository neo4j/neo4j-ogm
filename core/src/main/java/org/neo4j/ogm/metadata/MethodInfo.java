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

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;
import org.neo4j.ogm.utils.ClassUtils;
import org.neo4j.ogm.utils.RelationshipUtils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodInfo {

    private static final String primitiveGetters = "()I,()J,()S,()B,()C,()F,()D,()Z,()[I,()[J,()[S,()[B,()[C,()[F,()[D,()[Z";
    private static final String primitiveSetters = "(I)V,(J)V,(S)V,(B)V,(C)V,(F)V,(D)V,(Z)V,([I)V,([J)V,([S)V,([B)V,([C)V,([F)V,([D)V,([Z)V";

    private static final String simpleJavaObjectGetters =
                    "()Ljava/lang/Object;" +
                    "()Ljava/lang/Character;" +
                    "()Ljava/lang/Byte;" +
                    "()Ljava/lang/Short;" +
                    "()Ljava/lang/Integer;" +
                    "()Ljava/lang/Long;" +
                    "()Ljava/lang/Float;" +
                    "()Ljava/lang/Double;" +
                    "()Ljava/lang/Boolean;" +
                    "()Ljava/lang/String;" +
                    "()[Ljava/lang/Object;" +
                    "()[Ljava/lang/Character;" +
                    "()[Ljava/lang/Byte;" +
                    "()[Ljava/lang/Short;" +
                    "()[Ljava/lang/Integer;" +
                    "()[Ljava/lang/Long;" +
                    "()[Ljava/lang/Float;" +
                    "()[Ljava/lang/Double;" +
                    "()[Ljava/lang/Boolean;" +
                    "()[Ljava/lang/String;";

    private static final String simpleJavaObjectSetters =
                    "(Ljava/lang/Object;)V" +
                    "(Ljava/lang/Character;)V" +
                    "(Ljava/lang/Byte;)V" +
                    "(Ljava/lang/Short;)V" +
                    "(Ljava/lang/Integer;)V" +
                    "(Ljava/lang/Long;)V)" +
                    "(Ljava/lang/Float;)V" +
                    "(Ljava/lang/Double;)V" +
                    "(Ljava/lang/Boolean;)V" +
                    "(Ljava/lang/String;)V" +
                    "([Ljava/lang/Object;)V" +
                    "([Ljava/lang/Character;)V" +
                    "([Ljava/lang/Byte;)V" +
                    "([Ljava/lang/Short;)V" +
                    "([Ljava/lang/Integer;)V" +
                    "([Ljava/lang/Long;)V" +
                    "([Ljava/lang/Float;)V" +
                    "([Ljava/lang/Double;)V" +
                    "([Ljava/lang/Boolean;)V" +
                    "([Ljava/lang/String;)V";


    private final String name;
    private final String descriptor;
    private final ObjectAnnotations annotations;
    private final String typeParameterDescriptor;

    /**
     * Cached method to avoid repeated lookups
     */
    private Method method;

    /**
     * The associated attribute converter for this field, if applicable, otherwise null.
     */
    private AttributeConverter<?, ?> propertyConverter;

    /**
     * The associated composite attribute converter for this field, if applicable, otherwise null.
     */
    private CompositeAttributeConverter<?> compositeConverter;


    /**
     * Constructs a new {@link MethodInfo} based on the given arguments.
     *
     * @param name                    The name of the method
     * @param descriptor              The method descriptor that expresses the parameters and return type using Java signature string
     *                                notation
     * @param typeParameterDescriptor If the method parameter or return type is parameterised, this is the descriptor that
     *                                expresses its generic type, or <code>null</code> if that's not appropriate
     * @param annotations             The {@link ObjectAnnotations} applied to the field
     */
    public MethodInfo(String name, String descriptor, String typeParameterDescriptor, ObjectAnnotations annotations) {
        this.name = name;
        this.descriptor = descriptor;
        this.typeParameterDescriptor = typeParameterDescriptor;
        this.annotations = annotations;
        if (!this.getAnnotations().isEmpty()) {
            // TODO would like to pass in the CustomAttributeConverterIndex here but I've got no idea where it should come from
            /*
             * I can't really do it here, this is called during classpath scanning and I don't want to pass converter-specific
             * stuff into the classpath scanner code.
             *
             * maybe I go if converter is proxy then set index?
             * so in DomainInfo '...else { methodInfo.getPropertyConverter() instanceof proxy then register this repository }
             *  - also a bit shit, really
             * I also don't really want to add public methods to MethodInfo if they're not called for meta-data use
             * - i.e., don't like the idea of methodInfo.hasProxyConverter() or methodInfo.needsProxyConverter()
             *
             * would a static CustomAttributeConverterIndex really be that bad?
             *
             * hang on, what if we had no annotation and just handled it all in DomainInfo?
             * - ...probably, it's a case of null vs non-null converters when we run through DomainInfo
             * - have to remember that the converter makes it a simple field, which is important
             *
             * my reservation is that if we add a converter to everything then it's unnecessarily complicated
             * - this is true, but it's probably less filthy than 'instanceof Proxy'
             *   or 'method has no converter but is annotated with @Convert therefore give it a proxy'
             * - it's not even an option because a non-null converter means everything's a "simple" attribute!
             *
             * Therefore, I genuinely don't think we have a choice other than to ask for @Convert in DomainInfo,
             * since we cannot magically get the proxy in here any other way
             */

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

    public String property() {
        if (isSimpleSetter() || isSimpleGetter()) {
            if (annotations != null) {
                AnnotationInfo propertyAnnotation = annotations.get(Property.CLASS);
                if (propertyAnnotation != null) {
                    return propertyAnnotation.get(Property.NAME, getName());
                }
            }
            if (name.startsWith("get") || name.startsWith("set")) {
                StringBuilder sb = new StringBuilder(name.substring(3));
                sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
                return sb.toString();
            }
            return getName();
        }
        return null;
    }

    public String relationship() {
        if (!isSimpleSetter() && !isSimpleGetter()) {
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
        if (!isSimpleSetter() && !isSimpleGetter()) {
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

    public boolean isEquallyNamed(MethodInfo other) {
        return other != null && getName().equals(other.getName());
    }

    public boolean isGetter() {
        return getName().startsWith("get") && descriptor.startsWith("()");
    }

    public boolean isSetter() {
        return getName().startsWith("set") && descriptor.endsWith(")V");
    }

    public boolean isSimpleGetter() {
        return primitiveGetters.contains(descriptor)
                || hasPropertyConverter()
                || hasCompositeConverter()
                || returnsSimpleJavaType();
    }

    public boolean isSimpleSetter() {
        return primitiveSetters.contains(descriptor)
                || hasPropertyConverter()
                || hasCompositeConverter()
                || acceptsSimpleJavaType();
    }

    private boolean returnsSimpleJavaType() {
        return (simpleJavaObjectGetters.contains(descriptor) && typeParameterDescriptor == null)
                || (typeParameterDescriptor != null && simpleJavaObjectGetters.contains(typeParameterDescriptor));
    }

    private boolean acceptsSimpleJavaType() {
        return (simpleJavaObjectSetters.contains(descriptor) && typeParameterDescriptor == null)
                || (typeParameterDescriptor != null && simpleJavaObjectSetters.contains(typeParameterDescriptor));
    }

    public boolean hasPropertyConverter() {
        return propertyConverter != null;
    }

    public AttributeConverter getPropertyConverter() {
        return propertyConverter;
    }

    void setPropertyConverter(AttributeConverter<?, ?> propertyConverter) {
        if (this.propertyConverter == null && this.compositeConverter == null && propertyConverter != null) {
            this.propertyConverter = propertyConverter;
        } // we maybe set an annotated converter when object was constructed, so don't override with a default one
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
            String typeSignature = "(L" + type.getName().replace(".", "/") + ";)V";
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
            String typeSignature = "([L" + type.getName().replace(".", "/") + ";)V";
            if (descriptor != null && descriptor.equals(typeSignature)) {
                return true;
            }
            // #issue 42: check interfaces when types are defined using generics as interface extensions
            for (Class<?> iface : type.getInterfaces()) {
                typeSignature = "([L" + iface.getName().replace(".", "/") + ";)V";
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
            return false;
        }
        return false;
    }

    /**
     * Get the collection class name for the method
     *
     * @return collection class name
     */
    public String getCollectionClassname() {
        String descriptorClass = descriptor.replace("/", ".");
        if (descriptorClass.startsWith("(L")) {
            descriptorClass = descriptorClass.substring(2, descriptorClass.length() - 3); //remove the leading (L and trailing ;)V
        }
        if (descriptorClass.startsWith("()L")) {
            descriptorClass = descriptorClass.substring(3, descriptorClass.length() - 1); //remove the leading ()L and trailing ;
        }
        return descriptorClass;
    }

    public boolean isScalar() {
        return typeParameterDescriptor == null && !isArray();
    }

    public boolean hasAnnotation(String annotationName) {
        return getAnnotations().get(annotationName) != null;
    }

    public boolean isArray() {
        return descriptor.startsWith("()[") || descriptor.startsWith("([");
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

    /**
     * Returns an instance of the Method represented by this MethodInfo
     *
     * The expectation here is that only java bean getter and setter methods will be called
     *
     * @param className The class declaring this method. TODO: methodInfo should know this?
     * @return a Method, if it exists on the corresponding class.
     */
    public Method getMethod(String className) {
        if (method != null) {
            return method;
        }

        try {
            if (isSetter()) {
                method = MetaDataClassLoader.loadClass(className).getMethod(name, ClassUtils.getType(descriptor));
                return method;
            }
            if (isGetter()) {
                method = MetaDataClassLoader.loadClass(className).getMethod(name);
                return method;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Only JavaBean-style getter and setter methods can be invoked");
    }
}
