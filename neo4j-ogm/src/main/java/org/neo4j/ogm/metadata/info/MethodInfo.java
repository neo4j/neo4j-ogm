package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.RelationshipUtils;

public class MethodInfo {

    private static final String primitiveGetters="()I,()J,()S,()B,()C,()F,()D,()Z,()[I,()[J,()[S,()[B,()[C,()[F,()[D,()[Z";
    private static final String primitiveSetters="(I)V,(J)V,(S)V,(B)V,(C)V,(F)V,(D)V,(Z)V,([I)V,([J)V,([S)V,([B)V,([C)V,([F)V,([D)V,([Z)V";

    private final String name;
    private final String descriptor;
    private final ObjectAnnotations annotations;
    private final String typeParameterDescriptor;

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

    public boolean isSimpleGetter() {
        return primitiveGetters.contains(descriptor) || usesSimpleJavaTypes();
    }

    public boolean isSimpleSetter() {
        return primitiveSetters.contains(descriptor) || usesSimpleJavaTypes();
    }

    private boolean usesSimpleJavaTypes() {
        return (descriptor.contains("java/lang/") && typeParameterDescriptor == null)
                || (typeParameterDescriptor != null && typeParameterDescriptor.contains("java/lang/"));
    }

}
