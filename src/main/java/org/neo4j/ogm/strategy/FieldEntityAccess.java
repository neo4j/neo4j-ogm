package org.neo4j.ogm.strategy;

import java.lang.reflect.Field;

import org.neo4j.ogm.metadata.MappingException;

public class FieldEntityAccess implements EntityAccess {

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

    @Override
    public void setValue(Object instance, Object scalar) throws Exception {
        writeToObject(instance, scalar);
    }

    @Override
    public void set(Object instance, Object any) throws Exception {
        // TODO: refactor, this is the same as the code in Setter.java
        if (Iterable.class.isAssignableFrom(any.getClass())) {
            setIterable(instance, (Iterable<?>) any);
        } else {
            setValue(instance, any);
        }
    }

    @Override
    public void setIterable(Object instance, Iterable<?> iterable) throws Exception {
        Field field = instance.getClass().getDeclaredField(this.fieldName);
        writeToObject(instance, doTypeConversion(field.getType(), iterable));
    }

    private Object doTypeConversion(Class<?> type, Iterable<?> iterable) {
        return iterable; // TODO: this will also be the same as for Setter
    }

}
