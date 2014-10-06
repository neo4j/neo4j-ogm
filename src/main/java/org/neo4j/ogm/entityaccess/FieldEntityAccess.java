package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.MappingException;

import java.lang.reflect.Field;

public class FieldEntityAccess extends AbstractEntityAccess {

    private String fieldName;

    public FieldEntityAccess(String javaBeanFieldName) {
        this.fieldName = javaBeanFieldName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    private void writeToObject(Object target, Object value) {
        if (target == null) {
            return;
        }

        Class<?> clarse = target.getClass();
        try {
            Field field = clarse.getDeclaredField(this.fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
            throw new MappingException("Unable to set " + this.fieldName + " to " + value + " on " + target, e);
        }
    }

    private Object readFromField(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new MappingException("Unable to get " + this.fieldName + " for " + instance, e);
        }
    }

    @Override
    public void setValue(Object instance, Object scalar) throws Exception {
        writeToObject(instance, scalar);
    }

    @Override
    public void setIterable(Object instance, Iterable<?> iterable) throws Exception {
        Field field = instance.getClass().getDeclaredField(this.fieldName);
        Iterable<?> hydrated = (Iterable<?>) readFromField(field, instance);
        writeToObject(instance, merge(field.getType(), iterable, hydrated));
    }
}
