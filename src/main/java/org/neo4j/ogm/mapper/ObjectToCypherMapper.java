package org.neo4j.ogm.mapper;

/**
 * Specification for an object-graph mapper, which can arbitrary Java objects onto Cypher data manipulation queries.
 */
public interface ObjectToCypherMapper {

    /**
     * Processes the given object and any of its composite persistent objects and produces a Cypher query to
     * update their state in Neo4j.
     *
     * @param toPersist The "root" node of the object graph to persist
     * @return A Cypher query to write the data contained within the given object to Neo4j
     * @throws NullPointerException if invoked with <code>null</code>
     */
    String mapToCypher(Object toPersist);

}
