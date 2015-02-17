/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.RelationshipUtils;
import org.neo4j.ogm.typeconversion.AttributeConverter;

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
            setConverter(getAnnotatedTypeConverter());
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

    private AttributeConverter getAnnotatedTypeConverter() {
        if (typeParameterDescriptor == null) {
            return getAnnotations().getConverter(descriptor);
        } else {
            return getAnnotations().getConverter(typeParameterDescriptor);
        }
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
                return Relationship.OUTGOING;
            }
            return annotationInfo.get(Relationship.DIRECTION, Relationship.OUTGOING);
        }
        throw new RuntimeException("relationship direction call invalid");
    }

}
