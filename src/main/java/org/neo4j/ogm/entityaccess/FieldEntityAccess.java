package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.dictionary.FieldDictionary;
import org.neo4j.ogm.strategy.simple.SimpleFieldDictionary;

import java.lang.reflect.Field;

public class FieldEntityAccess extends AbstractEntityAccess {

    // todo: don't hardwire this in. Use injection to inject what you need.
    private static final FieldDictionary fieldDictionary = new SimpleFieldDictionary();

    private String fieldName;

    private FieldEntityAccess(String graphProperty) {
        this.fieldName = graphProperty;
    }

    public static FieldEntityAccess forProperty(String name) {
        StringBuilder sb = new StringBuilder();
        if (name != null && name.length() > 0) {
            sb.append(name.substring(0, 1).toLowerCase());
            sb.append(name.substring(1));
            return new FieldEntityAccess(sb.toString());
        } else {
            return null;
        }
    }

    private void writeToObject(Field field, Object target, Object value) throws Exception {
        if (target != null && field != null) {
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    private Object readFromField(Field field, Object instance) throws Exception {
        field.setAccessible(true);
        return field.get(instance);
    }

    @Override
    public void setValue(Object instance, Object parameter) throws Exception {
        Field field = fieldDictionary.findField(fieldName, parameter, instance);
        writeToObject(field, instance, parameter);
    }

    @Override
    public void setIterable(Object instance, Iterable<?> parameter) throws Exception {

        if (parameter.iterator().hasNext()) {
            Field field = fieldDictionary.findField(fieldName, parameter, instance);

            if (!field.getName().equals(fieldName)) {
                fieldName = field.getName();
            }

            Iterable<?> hydrated = (Iterable<?>) readFromField(field, instance);
            writeToObject(field, instance, merge(field.getType(), parameter, hydrated));
        }

    }
}
