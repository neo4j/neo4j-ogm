package org.neo4j.ogm.metadata.dictionary;

import org.neo4j.ogm.metadata.PersistentField;

public interface PersistentFieldDictionary {

    PersistentField lookUpPersistentFieldForProperty(String propertyName);

}
