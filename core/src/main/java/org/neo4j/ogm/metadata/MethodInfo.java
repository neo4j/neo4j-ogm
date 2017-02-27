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

import org.neo4j.ogm.metadata.classloader.MetaDataClassLoader;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodInfo {

    private final String className;
    private final String name;
    private final String descriptor;
    private final ObjectAnnotations annotations;
    private final String typeParameterDescriptor;

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
    public MethodInfo(String className, String name, String descriptor, String typeParameterDescriptor, ObjectAnnotations annotations) {
        this.className = className;
        this.name = name;
        this.descriptor = descriptor;
        this.typeParameterDescriptor = typeParameterDescriptor;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public ObjectAnnotations getAnnotations() {
        return annotations;
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
     * @return a Method, if it exists on the corresponding class.
     */
    public Method getMethod() {
        try {
            return MetaDataClassLoader.loadClass(className).getMethod(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
