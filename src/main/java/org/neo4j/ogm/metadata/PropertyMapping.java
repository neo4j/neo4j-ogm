package org.neo4j.ogm.metadata;

import java.lang.reflect.Field;

public class PropertyMapping {

    private String propertyName;
    private Object value;

    public PropertyMapping(String propertyName, Object value) {
        this.propertyName = propertyName;
        this.value = value;
    }

    public void writeToObject(Object target) {
        if (target == null) {
            return;
        }

        // TODO: should add setter support and/or consider pulling this out into a class in its own right
        Class<?> clarse = target.getClass();
        try {
            Field field = clarse.getDeclaredField(this.propertyName);
            field.setAccessible(true);
            field.set(target, this.value);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
            //TODO consider logging strategy
            System.err.println("Unable to set " + this.propertyName + " to " + this.value + " on " + target);
            e.printStackTrace(System.err);
        }
    }

    public String getPropertyName() {
        return this.propertyName;
    }

}
