package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SimpleFieldDictionary implements FieldDictionary {

    private final Map<Class, Map<String, Field>> fieldCache = new HashMap<>();

    @Override
    public Field findField(String property, Object parameter, Object instance) throws MappingException {

        //System.out.println("looking for property: " + property + ", of type " + parameter.getClass());
        if (parameter instanceof Collection) {
            Class elementType = ((Collection) parameter).iterator().next().getClass();
            return findCollectionField(instance, parameter, elementType, property);
        } else {
            return findScalarField(instance, parameter, property);
        }
    }

    private Field lookup(Class clazz, String fieldName) {
        Map<String, Field> fields = fieldCache.get(clazz);
        if (fields != null) {
            return fields.get(fieldName);
        }
        return null;
    }

    private Field insert(Class clazz, String fieldName, Field field) {
        Map<String, Field> fields = fieldCache.get(clazz);
        if (fields == null) {
            fields = new HashMap<>();
            fieldCache.put(clazz, fields);
        }
        fields.put(fieldName, field);
        return field;
    }

    private Field findScalarField(Object instance, Object parameter, String property) throws MappingException {

        Field f = lookup(instance.getClass(), property);
        if (f != null) return f;

        for (Field field: instance.getClass().getDeclaredFields()) {
            if (field.getName().equals(property)) {
                Type type = field.getGenericType();
                Class clazz = parameter.getClass();
                if (type.equals(clazz) || type.equals(ClassUtils.unbox(clazz))) {
                    return insert(clazz, field.getName(), field);
                }
            }
        }
        throw new MappingException("Could not find field: " + property);
    }

    private Field findCollectionField(Object instance, Object parameter, Class elementType, String property) throws MappingException {

        Class<?> clazz = instance.getClass();
        Field f = lookup(clazz, property + "?"); // ? indicates a collection property

        if (f != null) return f;

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.getName().startsWith(property)) {

                if (field.getType().isArray()) {
                    Object arrayType = ((Iterable)parameter).iterator().next();
                    if ((arrayType.getClass().getSimpleName() + "[]").equals(field.getType().getSimpleName())) {
                        return insert(clazz, field.getName() + "?", field);
                    }
                    if (ClassUtils.primitiveArrayName(elementType).equals(field.getType().getName())) {
                        return insert(clazz, field.getName() + "?", field);
                    }

                }
                else if (field.getType().getTypeParameters().length > 0) {
                    Class returnType;
                    try {
                        returnType = Class.forName(field.getType().getName());
                    } catch (Exception e) {
                        throw new MappingException(e.getLocalizedMessage());
                    }
                    if (returnType.isAssignableFrom(parameter.getClass())) {  // the best we can do with type erasure
                        return insert(clazz, field.getName() + "?", field);
                    }
                }
            }
        }

        throw new MappingException("Could not find collection or array field: " + property);
    }

}
