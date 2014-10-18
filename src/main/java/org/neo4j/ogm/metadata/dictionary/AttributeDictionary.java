package org.neo4j.ogm.metadata.dictionary;

import java.util.Set;

/**
 * Interface through which the correspondence between a persistent object attribute (i.e., field / instance variable) and a
 * node or relationship property can be retrieved.
 */
public interface AttributeDictionary {
/*
 * XXX I'm really not sure whether we should have lots of methods returning strings or have a rich PersistentAttribute object.
 */

    Set<String> lookUpCompositeEntityAttributesFromType(Class<?> typeToPersist);

    Set<String> lookUpValueAttributesFromType(Class<?> typeToPersist);

    String lookUpRelationshipTypeForAtrribute(String attributeName);

    String lookUpPropertyNameForAttribute(String attributeName);

}
