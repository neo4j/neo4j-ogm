package org.neo4j.ogm.metadata;

import java.lang.reflect.Field;

import org.neo4j.ogm.strategy.EntityAccess;

public class FieldEntityAccess implements EntityAccess {

    protected String fieldName;

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
    public void set(Object instance, Object any) throws Exception {
        writeToObject(instance, any);
    }

    @Override
    public void setValue(Object instance, Object scalar) throws Exception {
        writeToObject(instance, scalar);
    }

    @Override
    public void setIterable(Object instance, Iterable<?> iterable) throws Exception {
        writeToObject(instance, iterable);
    }

}
