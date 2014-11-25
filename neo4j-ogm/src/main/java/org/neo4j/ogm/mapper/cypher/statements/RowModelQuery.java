package org.neo4j.ogm.mapper.cypher.statements;

import java.util.Map;

public class RowModelQuery extends ParameterisedStatement {

    public RowModelQuery(String cypher, Map<String, ? extends Object> parameters) {
        super(cypher, parameters, "row");
    }
}
