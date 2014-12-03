package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

public class FieldReader implements RelationalReader {

    private final ClassInfo classInfo;
    private final FieldInfo fieldInfo;
    private final String relationshipType;

    FieldReader(ClassInfo classInfo, FieldInfo fieldInfo) {
        this(classInfo, fieldInfo, fieldInfo.relationship());
    }

    FieldReader(ClassInfo classInfo, FieldInfo fieldInfo, String relationshipType) {
        this.classInfo = classInfo;
        this.fieldInfo = fieldInfo;
        this.relationshipType = relationshipType;
    }

    @Override
    public Object read(Object instance) {
        return FieldAccess.read(classInfo.getField(fieldInfo), instance);
    }

    @Override
    public String relationshipType() {
        return relationshipType;
    }

    @Override
    public String propertyName() {
        return fieldInfo.property();
    }

}
