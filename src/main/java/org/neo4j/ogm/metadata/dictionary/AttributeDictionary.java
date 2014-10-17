package org.neo4j.ogm.metadata.dictionary;

import java.util.Set;

/**
 * Interface through which the correspondence between a persistent object attribute (i.e., field / instance variable) and a
 * node or relationship property can be retrieved.
 */
public interface AttributeDictionary {

    Set<String> lookUpCompositeEntityAttributesFromType(Class<?> typeToPersist);

    Set<String> lookUpValueAttributesFromType(Class<?> typeToPersist);

    String lookUpRelationshipTypeForAtrribute(String attributeName);

    String lookUpPropertyNameForAttribute(String attributeName);

}
