package org.neo4j.ogm.mapper.cypher.statements;

import java.util.List;

public class ParameterisedStatements {

    private final List<ParameterisedStatement> statements;

    public ParameterisedStatements(List<ParameterisedStatement> statements) {
        this.statements = statements;
    }

    public List<ParameterisedStatement> getStatements() {
        return statements;
    }

}
