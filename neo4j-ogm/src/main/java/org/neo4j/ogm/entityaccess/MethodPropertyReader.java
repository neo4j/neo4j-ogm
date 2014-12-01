package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

public class MethodPropertyReader implements PropertyReader {

    private final ClassInfo classInfo;
    private final MethodInfo methodInfo;

    MethodPropertyReader(ClassInfo classInfo, MethodInfo methodInfo) {
        this.classInfo = classInfo;
        this.methodInfo = methodInfo;
    }

    @Override
    public Object read(Object instance) {
        return MethodAccess.read(classInfo.getMethod(methodInfo), instance);
    }

}
