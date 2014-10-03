package org.neo4j.ogm.strategy.simple;

import org.graphaware.graphmodel.neo4j.Property;
import org.neo4j.ogm.metadata.PersistentField;
import org.neo4j.ogm.metadata.PersistentFieldDictionary;
import org.neo4j.ogm.metadata.RegularPersistentField;

/**
 * Super-simple implementation of {@link org.neo4j.ogm.metadata.PersistentFieldDictionary} that blindly assumes the instance variable name will always
 * match the name of the property read from the graph.
 */
public class SimplePersistentFieldDictionary implements PersistentFieldDictionary {

    @Override
    public PersistentField lookUpPersistentFieldForProperty(Property<?, ?> property) {
        return new RegularPersistentField(String.valueOf(property.getKey()));
    }

}
