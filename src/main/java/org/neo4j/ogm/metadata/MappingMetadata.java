package org.neo4j.ogm.metadata;

import java.util.HashMap;
import java.util.Map;

import org.graphaware.graphmodel.Property;

/**
 * Encapsulates information about the way in which a particular type of object is mapped to a part of a graph.
 */
public class MappingMetadata {

    private final Class<?> mappedType;
    private final Map<String, PersistentField> persistentFields = new HashMap<>();

    /**
     * Constructs a new {@link MappingMetadata} that stores mapping information about the given type.
     *
     * @param mappedType The {@link Class} representing the type about which to store mapping information
     * @param persistentFields A mapping between node property names and their corresponding persistent fields on the specified
     *        mapped type
     */
    public MappingMetadata(Class<?> mappedType, Map<String, PersistentField> persistentFields) {
        this.mappedType = mappedType;
        this.persistentFields.putAll(persistentFields);
    }

    public PropertyMapping getPropertyMapping(Property<?, ? extends Object> property) {
        if (this.persistentFields.containsKey(property.getKey())) {
            // now, what do we need to know about this property, what field it refers to and enough for a setter to set it!
            return new PropertyMapping(String.valueOf(property.getKey()), property.getValue());
        }
        return new NoOpPropertyMapping();
    }

    static final class NoOpPropertyMapping extends PropertyMapping {

        NoOpPropertyMapping() {
            super(null, null);
        }

        @Override
        public void writeToObject(Object target) {
            // do nothing by design
        }

    }

}
