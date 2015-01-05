package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

public class FieldReader implements RelationalReader {

    private final ClassInfo classInfo;
    private final FieldInfo fieldInfo;

    FieldReader(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.classInfo = classInfo;
        this.fieldInfo = fieldInfo;
    }

    @Override
    public Object read(Object instance) {
        Object value = FieldAccess.read(classInfo.getField(fieldInfo), instance);
        if (fieldInfo.hasConverter()) {
            value = fieldInfo.converter().toGraphProperty(value);
        }
        return value;
    }

    @Override
    public String relationshipType() {
        return fieldInfo.relationship();
    }

    @Override
    public String propertyName() {
        return fieldInfo.property();
    }

}
