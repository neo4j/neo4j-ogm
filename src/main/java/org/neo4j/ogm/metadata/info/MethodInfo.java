/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.RelationshipUtils;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.util.Collection;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodInfo {

    private static final String primitiveGetters="()I,()J,()S,()B,()C,()F,()D,()Z,()[I,()[J,()[S,()[B,()[C,()[F,()[D,()[Z";
    private static final String primitiveSetters="(I)V,(J)V,(S)V,(B)V,(C)V,(F)V,(D)V,(Z)V,([I)V,([J)V,([S)V,([B)V,([C)V,([F)V,([D)V,([Z)V";

    private final String name;
    private final String descriptor;
    private final ObjectAnnotations annotations;
    private final String typeParameterDescriptor;

    private AttributeConverter<?, ?> converter;

    /**
     * Constructs a new {@link MethodInfo} based on the given arguments.
     *
     * @param name The name of the method
     * @param descriptor The method descriptor that expresses the parameters and return type using Java signature string
     *        notation
     * @param typeParameterDescriptor If the method parameter or return type is parameterised, this is the descriptor that
     *        expresses its generic type, or <code>null</code> if that's not appropriate
     * @param annotations The {@link ObjectAnnotations} applied to the field
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
             * so in DomainInfo '...else { methodInfo.getConverter() instanceof proxy then register this repository }
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
            setConverter(getAnnotations().getConverter());
        }
    }

    public String getName() {
        return name;
    }

    public String property() {
       if (isSimpleSetter() || isSimpleGetter()) {
            try {
                return getAnnotations().get(Property.CLASS).get(Property.NAME, getName());
            } catch (NullPointerException npe) {
                if (name.startsWith("get") || name.startsWith("set")) {
                    StringBuilder sb = new StringBuilder(name.substring(3));
                    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
                    return sb.toString();
                }
                return getName();
            }
        }
        return null;
    }

    public String relationship() {
        if (!isSimpleSetter() && !isSimpleGetter()) {
            try {
                return getAnnotations().get(Relationship.CLASS).get(Relationship.TYPE, RelationshipUtils.inferRelationshipType(getName()));
            } catch (NullPointerException npe) {
                // TODO: consider whether to check parameter/return type here for an @RelationshipEntity annotation
                return RelationshipUtils.inferRelationshipType(getName());
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

    public boolean isSimpleGetter() {
        return primitiveGetters.contains(descriptor)
                || hasConverter()
                || usesSimpleJavaTypes();
    }

    public boolean isSimpleSetter() {
        return primitiveSetters.contains(descriptor)
                || hasConverter()
                || usesSimpleJavaTypes();
    }

    private boolean usesSimpleJavaTypes() {
        return (descriptor.contains("java/lang/") && typeParameterDescriptor == null)
                || (typeParameterDescriptor != null && typeParameterDescriptor.contains("java/lang/"));
    }

    public AttributeConverter converter() {
        return converter;
    }

    public boolean hasConverter() {
        return converter != null;
    }

    public void setConverter(AttributeConverter<?, ?> converter) {
        if (this.converter == null && converter != null) {
            this.converter = converter;
        }
    }

    public String relationshipDirection() {
        if (relationship() != null) {
            AnnotationInfo annotationInfo = getAnnotations().get(Relationship.CLASS);
            if (annotationInfo == null) {
                return Relationship.UNDIRECTED;
            }
            return annotationInfo.get(Relationship.DIRECTION, Relationship.UNDIRECTED);
        }
        throw new RuntimeException("relationship direction call invalid");
    }

    public boolean isTypeOf(Class<?> type) {
        while (type != null) {
            String typeSignature = "(L" + type.getName().replace(".", "/") + ";)V";
            if (descriptor != null && descriptor.equals(typeSignature)) {
                return true;
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
            type = type.getSuperclass();
        }
        return false;
    }

    public boolean isCollection() {
        String descriptorClass =getCollectionClassname();
        try {
            Class descriptorClazz = Class.forName(descriptorClass);
            if (Collection.class.isAssignableFrom(descriptorClazz)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * Get the collection class name for the method
     * @return collection class name
     */
    public String getCollectionClassname() {
        String descriptorClass = descriptor.replace("/", ".");
        if (descriptorClass.startsWith("(L")) {
            descriptorClass = descriptorClass.substring(2,descriptorClass.length()-3); //remove the leading (L and trailing ;)V
        }
        if(descriptorClass.startsWith("()L")) {
            descriptorClass = descriptorClass.substring(3,descriptorClass.length()-1); //remove the leading ()L and trailing ;
        }
        return descriptorClass;
    }

    public boolean isScalar() {

        if (typeParameterDescriptor != null) return false;
        if (descriptor.contains("[")) return false;

        return true;
    }

}
