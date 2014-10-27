package org.neo4j.ogm.metadata.dictionary;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.info.DomainInfo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class FieldDictionary implements MappingResolver {

    private final DomainInfo domainInfo;

    public FieldDictionary(DomainInfo domainInfo) {
        this.domainInfo = domainInfo;
    }

    /** owning type => field name => java.lang.reflect.Field */
    private final Map<Class, Map<String, Field>> fieldCache = new HashMap<>();

    public Field findField(String property, Object parameter, Object instance) throws MappingException {

        Field f = lookup(instance.getClass(), property);
        if (f != null) return f;

        if (parameter instanceof Collection) {
            Class elementType = ((Collection) parameter).iterator().next().getClass();
            f= findCollectionField(instance, parameter, elementType, property);
        } else {
            f= findScalarField(instance, parameter, property);
        }
        return insert(instance.getClass(), f.getName(), f);
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

    protected abstract Field findScalarField(Object instance, Object parameter, String property);
    protected abstract Field findCollectionField(Object instance, Object parameter, Class elementType, String property);

}
