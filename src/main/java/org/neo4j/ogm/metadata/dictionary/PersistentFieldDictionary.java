package org.neo4j.ogm.metadata.dictionary;

import java.util.Collection;

import org.neo4j.ogm.metadata.PersistentField;

public interface PersistentFieldDictionary {

    PersistentField lookUpPersistentFieldForProperty(String propertyName);

    Collection<PersistentField> lookUpPersistentFieldsOfType(Class<?> typeOfObjectToPersist);

    boolean isNodeEntity(Class<?> typeOfObjectToPersist);

}
