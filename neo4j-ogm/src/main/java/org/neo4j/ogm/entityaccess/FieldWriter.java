package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

import java.lang.reflect.Field;

public class FieldWriter extends EntityAccess {

    private final FieldInfo fieldInfo;
    private final Field field;
    private final Class<?> fieldType;

    public FieldWriter(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
        this.field = classInfo.getField(fieldInfo);
        this.fieldType = this.field.getType();
    }

    public static void write(Field field, Object instance, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object read(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Object instance, Object value) {
        if (fieldInfo.hasConverter()) {
            value = fieldInfo.converter().toEntityAttribute(value);
        }
        FieldWriter.write(field, instance, value);
    }

    @Override
    public Class<?> type() {
        return fieldType;
    }

    @Override
    public String relationshipName() {
        return this.fieldInfo.relationship();
    }

}
