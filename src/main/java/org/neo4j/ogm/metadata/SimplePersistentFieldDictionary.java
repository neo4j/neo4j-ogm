package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of {@link PersistentFieldDictionary} based on pre-determined information about the way in which a
 * particular type of object is mapped to a part of a graph.
 */
public class SimplePersistentFieldDictionary implements PersistentFieldDictionary {

    private final Map<String, PersistentField> propertyToFieldMappings = new HashMap<>();

    /**
     * Constructs a new {@link SimplePersistentFieldDictionary} that stores mapping information about the given type.
     *
     * @param persistentFields Some {@link PersistentField}s that provide the link between node property names and their
     *        corresponding persistent fields on the specified mapped type
     */
    public SimplePersistentFieldDictionary(Collection<? extends PersistentField> persistentFields) {
        for (PersistentField pf : persistentFields) {
            this.propertyToFieldMappings.put(pf.getGraphElementPropertyName(), pf);
        }
    }

    @Override
    public PersistentField lookUpPersistentFieldForProperty(Property<?, ?> property) {
        return this.propertyToFieldMappings.get(property.getKey());
    }

}
