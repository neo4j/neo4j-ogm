package org.neo4j.ogm.session.request;

import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.RowModel;

import java.util.List;

public interface RequestHandler {

    Neo4jResponse<GraphModel> execute(GraphModelQuery query, String url);
    Neo4jResponse<RowModel> execute(RowModelQuery query, String url);
    Neo4jResponse<String> execute(ParameterisedStatement statement, String url);
    Neo4jResponse<String> execute(List<ParameterisedStatement> statementList, String url);

}
