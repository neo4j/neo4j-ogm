package org.neo4j.ogm.mapper.cypher.statements;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple encapsulation of a Cypher query and its parameters.
 */
public class ParameterisedStatement {

    private final String statement;
    private final Map<String, Object> parameters = new HashMap<>();
    private final String[] resultDataContents;

    /**
     * Constructs a new {@link ParameterisedStatement} based on the given Cypher query string and query parameters.
     *
     * @param cypher The parameterised Cypher query string
     * @param parameters The name-value pairs that satisfy the parameters in the given query
     */
    public ParameterisedStatement(String cypher, Map<String, ? extends Object> parameters) {
        this(cypher, parameters, "row");
    }

    public ParameterisedStatement(String cypher, Map<String, ? extends Object> parameters, String... resultDataContents) {
        this.statement = cypher;
        this.parameters.putAll(parameters);
        this.resultDataContents = resultDataContents;
    }

    public String getStatement() {
        return statement.trim();
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String[] getResultDataContents() {
        return resultDataContents;
    }

}

