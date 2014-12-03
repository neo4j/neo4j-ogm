package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

public class MethodReader implements RelationalReader {

    private final ClassInfo classInfo;
    private final MethodInfo methodInfo;

    MethodReader(ClassInfo classInfo, MethodInfo methodInfo) {
        this.classInfo = classInfo;
        this.methodInfo = methodInfo;
    }

    @Override
    public Object read(Object instance) {
        return MethodAccess.read(classInfo.getMethod(methodInfo), instance);
    }

    @Override
    public String relationshipType() {
        return methodInfo.relationship();
    }

    @Override
    public String propertyName() {
        return methodInfo.property();
    }

}
