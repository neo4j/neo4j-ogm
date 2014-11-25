package org.neo4j.ogm.session.request.strategy;

import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.session.Utils;

import java.util.Collection;

public class DeleteStatements {

    public ParameterisedStatement delete(Long id) {
        return new ParameterisedStatement("MATCH (n) WHERE id(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map("id",id));
    }

    public ParameterisedStatement deleteAll(Collection<Long> ids) {
        return new ParameterisedStatement("MATCH (n) WHERE id(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map("ids", ids));
    }

    public ParameterisedStatement purge() {
        return new ParameterisedStatement("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map());
    }

    public ParameterisedStatement deleteByLabel(String label) {
        return new ParameterisedStatement(String.format("MATCH (n:%s) OPTIONAL MATCH (n)-[r]-() DELETE r, n", label), Utils.map());
    }
}
