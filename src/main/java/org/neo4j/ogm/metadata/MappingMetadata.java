package org.neo4j.ogm.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.graphaware.graphmodel.Property;

/**
 * Encapsulates information about the way in which a particular type of object is mapped to a part of a graph.
 */
public class MappingMetadata {

    private final Class<?> mappedType;

    // because there's not necessarily an exact match between the a node property name and its corresponding Java bean field name
    private final Map<String, String> propertyToFieldMappings = new HashMap<>();

    /**
     * Constructs a new {@link MappingMetadata} that stores mapping information about the given type.
     *
     * @param mappedType The {@link Class} representing the type about which to store mapping information
     * @param persistentFields Some {@link PersistentField}s that provide the link between node property names and their
     *        corresponding persistent fields on the specified mapped type
     */
    public MappingMetadata(Class<?> mappedType, Collection<? extends PersistentField> persistentFields) {
        this.mappedType = mappedType;
        for (PersistentField pf : persistentFields) {
            this.propertyToFieldMappings.put(pf.getGraphElementPropertyName(), pf.getJavaObjectFieldName());
        }
    }

    public PropertyMapper getPropertyMapper(Property<?, ? extends Object> property) {
        if (this.propertyToFieldMappings.containsKey(property.getKey())) {
            return determinePropertyMapper(property);
        }
        return new NoOpPropertyMapper();
    }

    static final class NoOpPropertyMapper extends PropertyMapper {

        NoOpPropertyMapper() {
            super("", null);
        }

        @Override
        public void writeToObject(Object target) {
            // do nothing by design
        }

    }

    // ALL THIS BELOW WILL GO INTO SOME KIND OF FACTORY/STRATEGY CLASS TO MAKE PropertyMappings AND DECIDE HOW FIELDS ARE SET

    /**
     * Determines which instance of PropertyMapping to use for the given property
     */
    private PropertyMapper determinePropertyMapper(Property<?, ? extends Object> property) {
        if (weAreDoingSetterDrivenPropertyMapping()) {
            return new NoOpPropertyMapper();
        }
        if (weAreDoingFieldDrivenPropertyMapping()) {
            return new FieldBasedPropertyMapper(this.propertyToFieldMappings.get(property.getKey()), property.getValue());
        }
        return new NoOpPropertyMapper();
    }
    private boolean weAreDoingSetterDrivenPropertyMapping() {
        return false;
    }
    private boolean weAreDoingFieldDrivenPropertyMapping() {
        return true;
    }

}
