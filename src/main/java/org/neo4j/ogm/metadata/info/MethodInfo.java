package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

public class MethodInfo {

    private String name;
    private String descriptor;
    private ObjectAnnotations annotations;

    public MethodInfo(String name, String descriptor, ObjectAnnotations annotations) {
        this.name = name;
        this.descriptor = descriptor;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public String property() {
//        if (isSimple()) {
            try {
                return getAnnotations().get(Property.class.getName()).get("name", getName());
            } catch (NullPointerException npe) {
                return getName();
            }
//        }
//        return null;
    }

    public String relationship() {
//        if (!isSimple()) {
            try {
                return getAnnotations().get(Relationship.class.getName()).get("name", getName());
            } catch (NullPointerException npe) {
                return getName();
            }
//        }
//        return null;
    }


    public String getDescriptor() {
        return descriptor;
    }

    public ObjectAnnotations getAnnotations() {
        return annotations;
    }
}
