package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

public class FieldPropertyReader implements PropertyReader {

    private final ClassInfo classInfo;
    private final FieldInfo fieldInfo;

    FieldPropertyReader(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.classInfo = classInfo;
        this.fieldInfo = fieldInfo;
    }

    @Override
    public Object read(Object instance) {
        return FieldAccess.read(classInfo.getField(fieldInfo), instance);
    }

}
