package org.neo4j.ogm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.session.GraphModelResult;
import org.neo4j.ogm.session.Neo4jRequestHandler;
import org.neo4j.ogm.session.Neo4jResponseHandler;

public abstract class RequestProxy implements Neo4jRequestHandler<GraphModel> {

    protected abstract String[] getResponse();

    public Neo4jResponseHandler<GraphModel> execute(String url, String... request) {
        return new Response(getResponse());
    }

    static class Response implements Neo4jResponseHandler<GraphModel> {

        private static final ObjectMapper objectMapper = new ObjectMapper();

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
                    return build(json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        private GraphModel build(String json) {
            try {
                GraphModelResult instance = objectMapper.readValue(json, GraphModelResult.class);
                return instance.getGraph();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
