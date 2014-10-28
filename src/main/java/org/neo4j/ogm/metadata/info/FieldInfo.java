package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

public class FieldInfo {

    private static final String primitives =
        "I,J,S,B,C,F,D,Z,[I,[J,[S,[B,[C,[F,[D,[Z";

    private String name;
    private String descriptor;
    private ObjectAnnotations annotations;

    public FieldInfo(String name, String descriptor, ObjectAnnotations annotations) {
        this.name = name;
        this.descriptor = descriptor;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public String property() {
        if (isSimple()) {
            try {
                return getAnnotations().get(Property.class.getName()).get("name", getName());
            } catch (NullPointerException npe) {
                return getName();
            }
        }
        return null;
    }

    public String relationship() {
        if (!isSimple()) {
            try {
                return getAnnotations().get(Relationship.class.getName()).get("name", getName());
            } catch (NullPointerException npe) {
                return getName();
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
