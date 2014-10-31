package org.neo4j.ogm.entityaccess;

import java.lang.reflect.Field;

public class FieldAccess extends ObjectAccess {

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

}
