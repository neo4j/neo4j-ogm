package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class SimpleFieldDictionary extends FieldDictionary {

    protected Field findScalarField(Object instance, Object parameter, String property) throws MappingException {

        for (Field field: instance.getClass().getDeclaredFields()) {
            if (field.getName().equals(property)) {
                Type type = field.getGenericType();
                Class clazz = parameter.getClass();
                if (type.equals(clazz) || type.equals(ClassUtils.unbox(clazz))) {
                    return field;
                }
            }
        }
        throw new MappingException("Could not find field: " + property);
    }

    protected Field findCollectionField(Object instance, Object parameter, Class elementType, String property) throws MappingException {

        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().startsWith(property)) {

                if (field.getType().isArray()) {
                    Object arrayType = ((Iterable)parameter).iterator().next();
                    if ((arrayType.getClass().getSimpleName() + "[]").equals(field.getType().getSimpleName())) {
                        return field;
                    }
                    if (ClassUtils.primitiveArrayName(elementType).equals(field.getType().getName())) {
                        return field;
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
                        return field;
                    }
                }
            }
        }

        throw new MappingException("Could not find collection or array field: " + property);
    }

}
