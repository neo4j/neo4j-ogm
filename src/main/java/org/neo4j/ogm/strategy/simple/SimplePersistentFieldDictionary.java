package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.PersistentField;
import org.neo4j.ogm.metadata.RegularPersistentField;
import org.neo4j.ogm.metadata.dictionary.PersistentFieldDictionary;

/**
 * Super-simple implementation of {@link org.neo4j.ogm.metadata.dictionary.PersistentFieldDictionary} that blindly assumes the instance variable name will always
 * match the name of the property read from the graph.
 */
public class SimplePersistentFieldDictionary implements PersistentFieldDictionary {

    @Override
    public PersistentField lookUpPersistentFieldForProperty(String property) {
        return new RegularPersistentField(String.valueOf(property));
    }

}
