package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.Property;

/**
 * Encapsulates information about the way in which a particular type of object is mapped to a part of a graph.
 */
public class MappingMetadata {

    public PropertyMapping getPropertyMapping(Property property) {
        // this should return a no-op property mapping if the given property is unsupported
        throw new UnsupportedOperationException("Adam hasn't written this method yet");
    }

}
