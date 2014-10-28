package org.neo4j.ogm.metadata.dictionary;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.info.DomainInfo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class FieldDictionary implements MappingResolver {

    protected final DomainInfo domainInfo;

    public FieldDictionary(DomainInfo domainInfo) {
        this.domainInfo = domainInfo;
    }

    /** owning type => field name => java.lang.reflect.Field */
    private final Map<Class, Map<String, Field>> fieldCache = new HashMap<>();

    /**
     * Retrieves a field whose name is the given property, or that has an @Property
     * or @Relationship or @NodeId... seems like we need multiple findFields....
     *
     * @param property
     * @param parameter
     * @param instance
     * @return
     * @throws MappingException
     */
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

    // for use when reading...
    // returns the identity field of the class represented by the classInfo. either id, or field with @NodeId
    //protected abstract Field identity(ClassInfo classInfo);
    // returns the field on the class represented by classInfo that has name 'name' or property annotation 'name'
    //protected abstract Field property(ClassInfo classInfo, String name);
    // returns the field on the class represented by classInfo that has name 'name' or relationship annotation 'name'
    //protected abstract Field relationship(ClassInfo classInfo, String name);

    // when we write an object from the domain to cypher, we need to know its persistable fields.
    // classInfo.getFieldInfos() will tell us this.
    //
    // having done that, we need to know - for each FieldInfo returned how to map it.
    // for example
    //      the identity field.
    //      simple property fields (that go on nodes)
    //      relationships
    // this implies that each fieldInfo class should indicate if its a property or not.
    // (if its underlying type is java.lang... or any of the primitives, then it is)
    // if its not a property, then its a relationship...
    // our metadata class should give us this information...


    protected abstract Field findScalarField(Object instance, Object parameter, String property);
    protected abstract Field findCollectionField(Object instance, Object parameter, Class elementType, String property);

}
