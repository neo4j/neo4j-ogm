package org.neo4j.ogm.mapper.cypher;

import java.util.Map;

public class GraphModelQuery extends ParameterisedStatement {

    public GraphModelQuery(String cypher, Map<String, ? extends Object> parameters) {
        super(cypher, parameters, "graph");
    }
}
