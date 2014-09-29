package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.Property;

// TODO: would PersistentFieldDictionary perhaps be a better name for this or is there a view to enhance it?
public interface MappingMetadata {

    PersistentField lookUpPersistentFieldForProperty(Property<?, ?> property);

}
