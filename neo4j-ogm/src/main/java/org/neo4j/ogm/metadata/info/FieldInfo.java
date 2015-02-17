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
     * @param name The name of the field
     * @param descriptor The field descriptor that expresses the type of the field using Java signature string notation
     * @param typeParameterDescriptor The descriptor that expresses the generic type parameter, which may be <code>null</code>
     *        if that's not appropriate
     * @param annotations The {@link ObjectAnnotations} applied to the field
     */
    public FieldInfo(String name, String descriptor, String typeParameterDescriptor, ObjectAnnotations annotations) {
        this.name = name;
        this.descriptor = descriptor;
        this.typeParameterDescriptor = typeParameterDescriptor;
        this.annotations = annotations;
        if (!this.annotations.isEmpty()) {
            setConverter(getAnnotatedTypeConverter());
        }
    }

    public String getName() {
        return name;
    }

    public boolean isTypeOf(Class<?> type) {
        String fieldSignature = "L" + type.getName().replace(".", "/") + ";";
        return descriptor.equals(fieldSignature);
    }

    // should these two methods be on PropertyReader, RelationshipReader respectively?
    public String property() {
        if (isSimple()) {
            try {
                return getAnnotations().get(Property.CLASS).get(Property.NAME, getName());
            } catch (NullPointerException npe) {
                return getName();
            }
        }
        return null;
    }

    public String relationship() {
        if (!isSimple()) {
            try {
                return getAnnotations().get(Relationship.CLASS).get(Relationship.TYPE, RelationshipUtils.inferRelationshipType(getName()));
            } catch (NullPointerException npe) {
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

    public boolean isSimple() {
        return primitives.contains(descriptor)
                || converter != null
                || (descriptor.contains("java/lang/") && typeParameterDescriptor == null)
                || (typeParameterDescriptor != null && typeParameterDescriptor.contains("java/lang/"));
    }

    public AttributeConverter converter() {
        return converter;
    }

    public void setConverter( AttributeConverter<?, ?> converter ) {
        if (this.converter == null && converter != null) {
            this.converter = converter;
        } // we maybe set an annotated converter when object was constructed, so don't override with a default one
    }

    public boolean hasConverter() {
        return converter != null;
    }

    private AttributeConverter<?, ?> getAnnotatedTypeConverter() {
        if (typeParameterDescriptor == null) {
            return getAnnotations().getConverter(descriptor);
        } else {
            return getAnnotations().getConverter(typeParameterDescriptor);
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
