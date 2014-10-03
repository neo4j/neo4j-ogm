package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.neo4j.Property;

public interface PersistentFieldDictionary {

    PersistentField lookUpPersistentFieldForProperty(Property<?, ?> property);

}
