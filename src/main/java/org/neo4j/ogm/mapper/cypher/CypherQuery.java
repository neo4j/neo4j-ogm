package org.neo4j.ogm.mapper.cypher;

import org.graphaware.graphmodel.neo4j.GraphModel;

public interface CypherQuery {

    /**
     * fetch one or more objects with the specified ids and their immediate neighbours
     * @param ids
     * @return MATCH p=(n)-[:r]->(m) where { id(n) in [....] | id(n) = ...}
     */
    GraphModel fetchById(Long... ids);

    /**
     * fetch all objects with the specified label and their immediate neighbours
     * @param label
     * @return MATCH p=(n:{LABEL})-[:r]->(m)
     */
    GraphModel fetchByLabel(String label);

    /**
     * Fetch all objects with the specified ids also having the specified label
     *
     * @param label
     * @param ids
     * @return MATCH p=(n:{LABEL})-[:r]->(m) where { id(n) in [....] | id(n) = ...}
     */
    GraphModel fetchByLabelAndId(String label, Long... ids);

}
