package org.neo4j.ogm.cypher.query;

import org.neo4j.ogm.cypher.statement.ParameterisedStatement;

import java.util.Map;

public class GraphModelQuery extends ParameterisedStatement {

    public GraphModelQuery(String cypher, Map<String, ? extends Object> parameters) {
        super(cypher, parameters, "graph");
    }
}
