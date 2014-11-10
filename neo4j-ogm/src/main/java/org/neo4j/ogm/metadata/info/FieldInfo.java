package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

public class FieldInfo {

    private static final String primitives = "I,J,S,B,C,F,D,Z,[I,[J,[S,[B,[C,[F,[D,[Z";
    private static final String scalars = "I,J,S,B,C,F,D,Z";

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
                // TODO: this is the "simple" strategy, but here the logic is different from the reading code
                // in that a field called changedPlacesWith won't end up as "CHANGED_PLACES_WITH"
                // see ObjectGraphMapper#setterNameFromRelationshipType
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
