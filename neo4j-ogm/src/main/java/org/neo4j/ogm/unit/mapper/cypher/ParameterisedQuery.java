package org.neo4j.ogm.unit.mapper.cypher;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple encapsulation of a Cypher query and its parameters.
 */
public class ParameterisedQuery {

    private final String cypher;
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * Constructs a new {@link ParameterisedQuery} based on the given Cypher query string and query parameters.
     *
     * @param cypher The parameterised Cypher query string
     * @param parameters The name-value pairs that satisfy the parameters in the given query
     */
    public ParameterisedQuery(String cypher, Map<String, ? extends Object> parameters) {
        this.cypher = cypher;
        this.parameters.putAll(parameters);
    }

    public String getCypher() {
        return cypher;
    }

    public Map<String, Object> getParameterMap() {
        return parameters;
    }

}
