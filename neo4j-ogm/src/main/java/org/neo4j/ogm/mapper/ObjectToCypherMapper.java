package org.neo4j.ogm.mapper;

import org.neo4j.ogm.mapper.cypher.compiler.CypherContext;
import org.neo4j.ogm.mapper.cypher.statements.ParameterisedStatements;

/**
 * Specification for an object-graph mapper, which can map arbitrary Java objects onto Cypher data manipulation queries.
 */
public interface ObjectToCypherMapper {

    /**
     * Processes the given object and any of its composite persistent objects and produces Cypher queries to persist their state
     * in Neo4j.
     *
     * @param toPersist The "root" node of the object graph to persist
     * @return A {@link ParameterisedStatements} object containing the statements required to persist the given object to Neo4j, never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CypherContext mapToCypher(Object toPersist);

    CypherContext mapToCypher(Object toPersist, int depth);

}
