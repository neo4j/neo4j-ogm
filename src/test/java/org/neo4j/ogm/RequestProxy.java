package org.neo4j.ogm;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.mapper.model.GraphBuilder;
import org.neo4j.ogm.session.RequestHandler;
import org.neo4j.ogm.session.ResponseStream;

public abstract class RequestProxy implements RequestHandler<GraphModel> {

    protected abstract String[] getResponse();

    public ResponseStream<GraphModel> execute(String string) {
        return new Response(getResponse());
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
