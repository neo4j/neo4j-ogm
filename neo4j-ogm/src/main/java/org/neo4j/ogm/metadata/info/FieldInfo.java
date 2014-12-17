package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.RelationshipUtils;

public class FieldInfo {

    private static final String primitives = "I,J,S,B,C,F,D,Z,[I,[J,[S,[B,[C,[F,[D,[Z";

    private final String name;
    private final String descriptor;
    private final String typeParameterDescriptor;
    private final ObjectAnnotations annotations;

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
        // XXX: don't we have a bug here if someone's class has a field of type java.lang.Iterable?
        return primitives.contains(descriptor) || descriptor.contains("java/lang/")
                || (typeParameterDescriptor != null && typeParameterDescriptor.contains("java/lang/"));
    }

}
