package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.info.FieldInfo;

import java.lang.reflect.Field;

public class FieldAccess extends ObjectAccess {

    public static void write(FieldInfo fieldInfo, Object instance, Object value) {
        Class clazz = instance.getClass();
        try {

            if (Iterable.class.isAssignableFrom(value.getClass())) {
                value = merge(value.getClass(), (Iterable<?>) read(fieldInfo, instance), (Iterable<?>) value);
            }

            Field field = clazz.getDeclaredField(fieldInfo.getName());
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new MappingException(e.getLocalizedMessage());
        }
    }

    public static Object read(FieldInfo fieldInfo, Object instance) {
        Class clazz = instance.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldInfo.getName());
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            throw new MappingException(e.getLocalizedMessage());
        }
    }

}
