package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

import java.lang.reflect.Field;

public class FieldAccess extends ObjectAccess {

    private final FieldInfo fieldInfo;
    private final ClassInfo classInfo;

    public FieldAccess(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.classInfo = classInfo;
        this.fieldInfo = fieldInfo;
    }

    public static void write(Field field, Object instance, Object value) {
        try {
            if (Iterable.class.isAssignableFrom(field.getType()) || field.getType().isArray()) {
                value = merge(field.getType(),  (Iterable<?>) value, (Iterable<?>) read(field, instance));
            }
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
        FieldAccess.write(classInfo.getField(fieldInfo), instance, value);
    }

    @Override
    public String relationshipType() {
        return this.fieldInfo.relationship();
    }

}
