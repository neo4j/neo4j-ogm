package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

public class MethodInfo {

    private static final String simpleGetters="()I,()J,()S,()B,()C,()F,()D,()Z,()[I,()[J,()[S,()[B,()[C,()[F,()[D,()[Z";
    private static final String simpleSetters="(I)V,(J)V,(S)V,(B)V,(C)V,(F)V,(D)V,(Z)V,([I)V,([J)V,([S)V,([B)V,([C)V,([F)V,([D)V,([Z)V";

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
       if (isSimpleSetter() || isSimpleGetter()) {
            try {
                return getAnnotations().get(Property.CLASS).get(Property.NAME, getName());
            } catch (NullPointerException npe) {
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

    public boolean isSimpleGetter() {
        return simpleGetters.contains(descriptor) || descriptor.contains("java/lang/");
    }

    public boolean isSimpleSetter() {
        return simpleSetters.contains(descriptor) || descriptor.contains("java/lang/");
    }
}
