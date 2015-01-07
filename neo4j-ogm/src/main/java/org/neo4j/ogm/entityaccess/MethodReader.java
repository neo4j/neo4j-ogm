package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

public class MethodReader implements RelationalReader, PropertyReader {

    private final ClassInfo classInfo;
    private final MethodInfo methodInfo;

    MethodReader(ClassInfo classInfo, MethodInfo methodInfo) {
        this.classInfo = classInfo;
        this.methodInfo = methodInfo;
    }

    @Override
    public Object read(Object instance) {
        Object value = MethodWriter.read(classInfo.getMethod(methodInfo), instance);
        if (methodInfo.hasConverter()) {
            value = methodInfo.converter().toGraphProperty(value);
        }
        return value;
    }

    @Override
    public String relationshipType() {
        return methodInfo.relationship();
    }

    @Override
    public String relationshipDirection() {
        try {
            return methodInfo.getAnnotations().get(Relationship.CLASS).get(Relationship.DIRECTION, Relationship.OUTGOING);
        } catch (NullPointerException npe) {
            return Relationship.OUTGOING;
        }
    }

    @Override
    public String propertyName() {
        return methodInfo.property();
    }

}
