package org.neo4j.ogm.metadata.dictionary;

import org.neo4j.ogm.metadata.PersistentField;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of {@link PersistentFieldDictionary} based on pre-determined information about the way in which a
 * particular type of object is mapped to a part of a graph. This will be used when we have annotations?
 *
 * In any case, it will have to implement FieldDictionary, as the things we look up are not just "properties" but also
 * graph labels (e.g. corresponding to Java types) Note that the only use of this class is now in the playground,
 * as ObjectGraphMapper does not use these directly, instead the EntityAccessStrategy is given a Dictionary to use.
 * So lets revisit the design soon - Vince.
 */
public class DefaultPersistentFieldDictionary implements PersistentFieldDictionary {

    private final Map<String, PersistentField> propertyToFieldMappings = new HashMap<>();

    /**
     * Constructs a new {@link DefaultPersistentFieldDictionary} that stores mapping information about the given type.
     *
     * @param persistentFields Some {@link PersistentField}s that provide the link between node property names and their
     *        corresponding persistent fields on the specified mapped type
     */
    public DefaultPersistentFieldDictionary(Collection<? extends PersistentField> persistentFields) {
        for (PersistentField pf : persistentFields) {
            this.propertyToFieldMappings.put(pf.getGraphElementPropertyName(), pf);
        }
    }

    @Override
    public PersistentField lookUpPersistentFieldForProperty(String propertyName) {
        return this.propertyToFieldMappings.get(propertyName);
    }

}
