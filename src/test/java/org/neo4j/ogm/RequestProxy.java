package org.neo4j.ogm;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.session.GraphModelBuilder;
import org.neo4j.ogm.session.Neo4jRequestHandler;
import org.neo4j.ogm.session.Neo4jResponseHandler;

public abstract class RequestProxy implements Neo4jRequestHandler<GraphModel> {

    protected abstract String[] getResponse();

    public Neo4jResponseHandler<GraphModel> execute(String url, String request) {
        return new Response(getResponse());
    }

    static class Response implements Neo4jResponseHandler<GraphModel> {

        private final String[] jsonModel;
        private int count = 0;

        public Response(String[] jsonModel) {
            this.jsonModel = jsonModel;
        }

        public GraphModel next()  {
            if (count < jsonModel.length) {
                String json = jsonModel[count];
                count++;
                try {
                    return GraphModelBuilder.build(json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

    }
}
