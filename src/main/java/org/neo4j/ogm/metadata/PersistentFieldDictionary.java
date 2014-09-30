package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.Property;

public interface PersistentFieldDictionary {

    PersistentField lookUpPersistentFieldForProperty(Property<?, ?> property);

}
