package org.neo4j.ogm.metadata.dictionary;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.metadata.PersistentField;
import org.neo4j.ogm.metadata.RegularPersistentField;

/**
 * Super-simple implementation of {@link org.neo4j.ogm.metadata.dictionary.PersistentFieldDictionary} that blindly assumes the
 * instance variable name will always match the name of the property read from the graph.
 */
public class SimplePersistentFieldDictionary implements PersistentFieldDictionary {

    @Override
    public PersistentField lookUpPersistentFieldForProperty(String property) {
        return new RegularPersistentField(String.valueOf(property));
    }

    @Override
    public Collection<PersistentField> lookUpPersistentFieldsOfType(Class<?> typeOfObjectToPersist) {
        Set<PersistentField> persistentFields = new HashSet<>();
        for (Field declaredField : typeOfObjectToPersist.getDeclaredFields()) {
            if (isPersistent(declaredField)) {
                persistentFields.add(new RegularPersistentField(declaredField.getName()));
            }
        }
        return persistentFields;
    }

    private boolean isPersistent(Field field) {
        return (field.getModifiers() & Modifier.TRANSIENT) == 0;
    }

    @Override
    public boolean isNodeEntity(Class<?> typeOfObjectToPersist) {
        return true; // let's assume it is, for now - there's even an argument this method shouldn't be on here anyway
    }

}
