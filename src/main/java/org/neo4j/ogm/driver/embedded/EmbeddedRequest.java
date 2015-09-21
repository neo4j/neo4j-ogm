package org.neo4j.ogm.driver.embedded;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.session.request.AbstractRequest;
import org.neo4j.ogm.session.response.Neo4jResponse;

/**
 * @author vince
 */
public class EmbeddedRequest extends AbstractRequest {

    private final GraphDatabaseService graphDatabaseService;

    public EmbeddedRequest(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;

    }

    @Override
    public Neo4jResponse<String> execute(ParameterisedStatement statement) {
        return new EmbeddedResponse(graphDatabaseService.execute(statement.getStatement(), statement.getParameters()));
    }


}
