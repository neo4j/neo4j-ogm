package org.neo4j.ogm.mapper.cypher;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains contextual information throughout the process of building Cypher statements to persist a graph of objects.
 */
public class CypherBuildingContext {

    private final Map<Object, NodeBuilder> visitedObjects = new HashMap<>();

    public boolean containsObject(Object obj) {
        return this.visitedObjects.containsKey(obj);
    }

    public NodeBuilder retrieveNodeBuilderForObject(Object obj) {
        return this.visitedObjects.get(obj);
    }

    public void add(Object toPersist, NodeBuilder nodeBuilder) {
        this.visitedObjects.put(toPersist, nodeBuilder);
    }

}