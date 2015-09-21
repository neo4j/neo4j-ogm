package org.neo4j.ogm.session.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.GraphRowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQueryWithStatistics;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.session.response.*;
import org.neo4j.ogm.session.result.GraphRowModel;
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.result.RowQueryStatisticsResult;

/**
 * @author vince
 */
public abstract class AbstractRequest implements RequestHandler {

    protected static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Neo4jResponse<GraphModel> execute(GraphModelQuery query) {
        return new GraphModelResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public Neo4jResponse<RowModel> execute(RowModelQuery query) {
        return new RowModelResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public Neo4jResponse<GraphRowModel> execute(GraphRowModelQuery query) {
        return new GraphRowModelResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public Neo4jResponse<RowQueryStatisticsResult> execute(RowModelQueryWithStatistics query) {
        return new RowStatisticsResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public abstract Neo4jResponse<String> execute(ParameterisedStatement statement);

}
