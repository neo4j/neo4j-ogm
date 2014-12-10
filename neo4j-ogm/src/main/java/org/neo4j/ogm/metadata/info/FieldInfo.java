package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.RelationshipUtils;

public class FieldInfo {

    private static final String primitives = "I,J,S,B,C,F,D,Z,[I,[J,[S,[B,[C,[F,[D,[Z";
    private static final String scalars = "I,J,S,B,C,F,D,Z";

    private final String name;
    private final String descriptor;
    private final ObjectAnnotations annotations;

    public FieldInfo(String name, String descriptor, ObjectAnnotations annotations) {
        this.name = name;
        this.descriptor = descriptor;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public boolean isTypeOf(Class<?> type) {
        String fieldSignature = "L" + type.getName().replace(".", "/") + ";";
        return descriptor.equals(fieldSignature);
    }

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
                return getAnnotations().get(Relationship.CLASS).get(Relationship.TYPE, getName());
            } catch (NullPointerException npe) {
                return RelationshipUtils.inferRelationshipType(getName());
            }
        }
        return null;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public ObjectAnnotations getAnnotations() {
        return annotations;
    }

    public boolean isSimple() {
        return primitives.contains(descriptor) || descriptor.contains("java/lang/");
    }

}
