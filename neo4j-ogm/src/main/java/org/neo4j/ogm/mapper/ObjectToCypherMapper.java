package org.neo4j.ogm.mapper;

import org.neo4j.ogm.cypher.compiler.CypherContext;

/**
 * Specification for an object-graph mapper, which can map arbitrary Java objects onto Cypher data manipulation queries.
 */
public interface ObjectToCypherMapper {

    /**
     * Processes the given object and any of its composite persistent objects and produces Cypher queries to persist their state
     * in Neo4j.
     *
     * @param toPersist The "root" node of the object graph to persist
     * @return A {@link CypherContext} object containing the statements required to persist the given object to Neo4j, along
     *         with a representation of the changes to be made by the Cypher statements never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CypherContext mapToCypher(Object toPersist);

    /**
     * Processes the given object and any of its composite persistent objects to the specified depth and produces Cypher queries
     * to persist their state in Neo4j.
     *
     * @param toPersist The "root" node of the object graph to persist
     * @param depth The number of objects away from the "root" to traverse when looking for objects to map
     * @return A {@link CypherContext} object containing the statements required to persist the given object to Neo4j, along
     *         with a representation of the changes to be made by the Cypher statements never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CypherContext mapToCypher(Object toPersist, int depth);

}
