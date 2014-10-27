package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;

import java.lang.reflect.Field;

public class FieldEntityAccess extends AbstractEntityAccess {

    private String fieldName;
    private final FieldDictionary fieldDictionary;

    private FieldEntityAccess(FieldDictionary fieldDictionary, String graphProperty) {
        this.fieldDictionary = fieldDictionary;
        this.fieldName = fieldDictionary.resolveGraphAttribute(graphProperty);
    }


    /*
      * Returns a field-entity access based on constructing a field name from a graph property
      * e.g. a property "Name" would map to a field "name". A property like "PrimarySchool" would
      * be mapped to an attribute "primarySchool" (but see note below)
      *
      * We do the same for relationship names as well, but we have to a agree on a
      * convention. For example a relationship "john-[:likes]->pizza".
      * We assume that the node john has a label "Person" while pizza has a label "Food".
      *
      * SimpleFieldMapping then expects an object of type Person to have a field "likes" that
      * is scalar or vector of the type Food. All the below would map correctly:
      *
      *    Food likes;
      *    Food[] likes;
      *    Collection<Food> likes; (and any subclasses/subinterfaces of Collection)
      *
      * TODO: looks redundant now. just create a new one when we need it.
     */
    public static FieldEntityAccess forProperty(FieldDictionary fieldDictionary, String name) {
        return new FieldEntityAccess(fieldDictionary, name);
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

    @Override
    public Object readValue(Object instance) throws MappingException {
        try {
            // XXX: can't use fieldDictionary at the moment because we don't know the field type
            Field field = findField(fieldName, instance.getClass());
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new MappingException("Failed to read value of field: " + fieldName + " on " + instance.getClass(), e);
        }
    }

    // sorry if this is duplicate code from fieldDictionary, I'm awaiting Vince's changes before I refactor it
    private Field findField(String fieldName, Class<?> clarse) throws NoSuchFieldException {
        try {
            return clarse.getDeclaredField(fieldName);
        } catch (NoSuchFieldException nsfe) {
            if (clarse.getSuperclass() == null) {
                throw nsfe;
            }
            return findField(fieldName, clarse.getSuperclass());
        }
    }

}
