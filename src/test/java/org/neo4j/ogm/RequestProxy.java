package org.neo4j.ogm;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.mapper.cypher.Request;
import org.neo4j.ogm.mapper.cypher.ResponseStream;
import org.neo4j.ogm.mapper.model.GraphBuilder;

public abstract class RequestProxy implements Request<GraphModel> {

    protected String[] response;

    protected void setResponse(String[] response) {
        this.response = response;
    }

    public ResponseStream<GraphModel> execute() {
        return new Response(response);
    }

    public class Response implements ResponseStream<GraphModel> {

        private final String[] jsonModel;
        private int count = 0;

        public Response(String[] jsonModel) {
            this.jsonModel = jsonModel;
        }

        public GraphModel next()  {
            if (hasNext()) {
                String json = jsonModel[count];
                count++;
                try {
                    return GraphBuilder.build(json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        public boolean hasNext() {
            return count < jsonModel.length;
        }

    }
}
